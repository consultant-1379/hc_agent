/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Sep 27, 2019
 *     Author: eaoknkr
 */

package com.ericsson.sc.hcagent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.BatchV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Watch.Response;
import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * 
 */
public abstract class ResourceChecker<T>
{
    protected final CoreV1Api coreV1Api;
    protected final ApiClient apiClient;
    protected final AppsV1Api appsV1Api;
    protected final BatchV1Api batchV1Api;
    protected final CacheAlarmHandler cacheAlarmHandler;
    protected final ResourceCheckerParameters params;
    protected final SeveritiesTracker severitiesTracker;

    private static final Logger log = LoggerFactory.getLogger(ResourceChecker.class);

    protected ResourceChecker(ResourceCheckerParameters params,
                              FmAlarmService alarmService,
                              ClusterCheck clusterCheck,
                              SeveritiesTracker severitiesTracker) throws IOException, URISyntaxException
    {
        // keep parameters needed for resource checker
        this.params = params;

        // initialize k8s api client
        this.apiClient = Config.fromCluster();
        this.apiClient.setReadTimeout(0); // infinite timeout
        this.apiClient.setBasePath(this.normalize(this.apiClient.getBasePath()));
        log.info("Client's base path set to: {}", this.apiClient.getBasePath());

        // set confifuration for the api client
        Configuration.setDefaultApiClient(this.apiClient);

        // initialize coreApi
        this.coreV1Api = new CoreV1Api(this.apiClient);

        // initialize appsApi
        this.appsV1Api = new AppsV1Api(this.apiClient);

        // initialize batchapi
        this.batchV1Api = new BatchV1Api(this.apiClient);

        this.severitiesTracker = severitiesTracker;

        var faultIndication = new FaultIndicationBuilder().withFaultName(this.params.getFaultName())
                                                          .withFaultyResource(this.params.getFaultResource())
                                                          .withServiceName(this.params.getFaultServiceName())
                                                          .build();

        // initialize cache alarm handler
        this.cacheAlarmHandler = new CacheAlarmHandler(new CacheAlarmData(severitiesTracker, faultIndication),
                                                       alarmService,
                                                       this.coreV1Api,
                                                       clusterCheck,
                                                       this.params.getACExpiration());

    }

    private String normalize(String basePath) throws UnknownHostException, URISyntaxException
    {
        var oldUri = new URI(basePath);

        var normalizedAddress = InetAddress.getByName(oldUri.getHost()).getHostAddress();

        var normalizedUri = new URI(oldUri.getScheme(),
                                    oldUri.getUserInfo(),
                                    normalizedAddress,
                                    oldUri.getPort(),
                                    oldUri.getPath(),
                                    oldUri.getQuery(),
                                    oldUri.getFragment());

        log.info("Normalized URI: {}", normalizedUri);

        return normalizedUri.toString();
    }

    protected interface ClusterCheck
    {
        public List<String> fetchResources(CoreV1Api api);
    }

    /**
     * Makes the appropriate kubernetes request to start watching the resource
     * changes.
     * 
     * @return
     * @throws ApiException
     * @throws IOException
     */
    protected abstract Flowable<Response<T>> createWatch();

    /**
     * Processes each event produced by a resource change.
     * 
     * @param resource
     */
    protected abstract Completable processEvent(T resource);

    public Completable run()
    {
        return Completable.defer(() -> Completable.ambArray(this.cacheAlarmHandler.run().onErrorComplete(),
                                                            this.createWatch()
                                                                .onBackpressureBuffer()
                                                                .concatMapCompletable(p -> this.processEvent(p.object).retry(1))
                                                                .doOnSubscribe(l -> log.info("Starting watch operation!"))
                                                                .doOnError(e -> log.error("An error occured while processing events. ", e))
                                                                .doOnComplete(() -> log.info("Watch completed. Resubscribing"))
                                                                .onErrorComplete()));

    }

    public Completable stop()
    {
        return Completable.complete().andThen(cacheAlarmHandler.stop());
    }

}
