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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.FmAlarmHandler;
import com.ericsson.sc.fm.FmAlarmServiceImpl;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.reactivex.Completable;
import io.reactivex.functions.Predicate;
import io.vertx.reactivex.core.Vertx;

/**
 * 
 */
public class HealthCheckAgent
{
    private static final Logger log = LoggerFactory.getLogger(HealthCheckAgent.class);
    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/hcagent/config/logcontrol").getPath();
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME");
    private static final String ALARM_SEVERITIES_PATH = "/hcagent/config/severities/";

    private final Vertx vertx;
    private final PodChecker podChecker;
    private final RxShutdownHook shutdownHook;
    private final HealthCheckAgentParameters params;
    private final WebClientProvider alarmHandlerClient;
    private final SeveritiesTracker severitiesTracker;

    public HealthCheckAgent(HealthCheckAgentParameters params,
                            RxShutdownHook shutdownHook) throws IOException, URISyntaxException
    {
        this.params = params;
        this.vertx = VertxInstance.get();

        // start webclient to be used for the resource check failures
        var tmpClient = WebClientProvider.builder().withHostName(this.params.getHostname());
        if (this.params.getAlarmHandlerParameters().tlsEnabled())
        {
            var tmpDynamicTls = DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(this.params.getAlarmHandlerParameters().getFmClientCertPath()), //
                                                             SipTlsCertWatch.trustedCert(this.params.getAlarmHandlerParameters().getFmClientCaPath()));
            tmpClient = tmpClient.withDynamicTls(tmpDynamicTls);
        }
        this.alarmHandlerClient = tmpClient.build(this.vertx);

        // create alarm handler for requests to alarm handler service
        var fmAlarmHandler = new FmAlarmHandler(this.alarmHandlerClient, // web client to be used for alarm raise/cease
                                                this.params.getAlarmHandlerParameters().getFmAlarmHost(), // alarm handler service server hostname
                                                this.params.getAlarmHandlerParameters().getFmAlarmPort(), // alarm handler service server port
                                                this.params.getAlarmHandlerParameters().tlsEnabled()); // indication if tls is enabled

        // create alarm service for updating the alarm through alarm handler service
        var fmAlarmService = new FmAlarmServiceImpl(fmAlarmHandler);
        this.severitiesTracker = new SeveritiesTracker(ALARM_SEVERITIES_PATH);
        this.podChecker = new PodChecker(params.getResourceCheckerParameters(), fmAlarmService, severitiesTracker);
        this.shutdownHook = shutdownHook;
    }

    public static void main(String[] args)
    {
        var exitStatus = 1;

        try (var shutdownHook = new RxShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(), CONTAINER_NAME))

        {
            final var params = HealthCheckAgentParameters.fromEnvironment();
            final var hcAgent = new HealthCheckAgent(params, shutdownHook);
            hcAgent.start().blockingAwait();
            log.info("Health check agent terminated normally");
            exitStatus = 0;
        }
        catch (Exception e)
        {
            log.error("Health check agent terminated abnormally due to exception", e);
            exitStatus = 1;
        }

        System.exit(exitStatus);
    }

    private Completable start()
    {
        return Completable.complete()
                          .doOnComplete(() -> log.info("Sleeping for {} seconds", this.params.getInitDelay()))
                          .delay(this.params.getInitDelay(), TimeUnit.SECONDS)
                          .andThen(this.severitiesTracker.run())
                          .andThen(podChecker.run()//
                                             .repeat() // watch is not executed infinitely, so we have to restart it
                                             .doOnSubscribe(s -> log.info("Starting pod checker"))
                                             .doOnComplete(() -> log.info("Shutting down pod checker")))
                          .takeUntil(this.shutdownHook.get())
                          .onErrorResumeNext(throwable -> stop().andThen(Completable.error(throwable)))
                          .andThen(this.stop())
                          .andThen(this.severitiesTracker.stop());
    }

    private Completable stop()
    {
        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };
        return Completable.complete() //
                          .andThen(this.podChecker.stop().onErrorComplete(logErr)) //
                          .andThen(this.alarmHandlerClient.close().onErrorComplete(logErr))
                          .andThen(this.vertx.rxClose().onErrorComplete(logErr));
    }
}
