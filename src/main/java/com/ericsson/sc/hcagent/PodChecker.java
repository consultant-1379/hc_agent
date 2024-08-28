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
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.hcagent.CacheAlarmHandler.CacheAction;
import com.ericsson.sc.hcagent.PodData.FaultIndicationStatus;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.google.common.reflect.TypeToken;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ContainerState;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.util.Watch;
import io.kubernetes.client.util.Watch.Response;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * 
 */
public class PodChecker extends ResourceChecker<V1Pod>
{
    public enum ContainerStatus
    {
        RUNNING_OR_SUCCESS,
        WAITING,
        FAILURE // terminated with exitcode != 0
    }

    private static final String STATEFULSET = "statefulset";
    private static final String DEPLOYMENT = "deployment";
    private static final String REPLICASET = "replicaset";
    private static final String DAEMONSET = "daemonset";
    private static final String ERIC_DATA_WIDE_COLUMN_DATABASE_CD = "eric-data-wide-column-database-cd";

    private static final String TLS_RESTARTER = "tls-restarter";
    private static final String CONFIGURE_KEYSPACES = "configure-keyspaces";

    private static final Integer WATCH_RETRY_DELAY_MS = 30000;
    private static final Integer WATCH_RETRIES = 10;

    private static final Logger log = LoggerFactory.getLogger(PodChecker.class);

    /**
     * @throws ApiException
     * @throws IOException
     * @throws URISyntaxException
     */
    public PodChecker(ResourceCheckerParameters params,
                      FmAlarmService alarmService,
                      SeveritiesTracker severitiesTracker) throws IOException, URISyntaxException
    {
        super(params, alarmService, api ->
        {
            try
            {
                return api.listNamespacedPod(params.getNamespace(), null, null, null, null, null, 0, null, null, 10, false)
                          .getItems()
                          .stream()
                          .map(pod -> pod.getMetadata().getName())
                          .toList();
            }
            catch (ApiException e)
            {
                throw new PodsNotFoundException("Fetching of the list of namespaced pods returned error", e);
            }

        }, severitiesTracker);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.hcagent.ResourceChecker#startWatching()
     */
    @Override
    protected Flowable<Response<V1Pod>> createWatch()
    {
        return Flowable.defer(() -> Single.fromCallable(() -> Watch.<V1Pod>createWatch(this.apiClient,
                                                                                       this.coreV1Api.listNamespacedPodCall(this.params.getNamespace(),
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            null,
                                                                                                                            true,
                                                                                                                            null),
                                                                                       new TypeToken<Watch.Response<V1Pod>>()
                                                                                       {
                                                                                           private static final long serialVersionUID = 1L;
                                                                                       }.getType()))
                                          .subscribeOn(Schedulers.newThread())
                                          .doOnError(e -> log.error("Error when trying to create Kubernetes watch on pods.", e))
                                          .flatMapPublisher(Flowable::fromIterable)
                                          .doOnError(e -> log.error("Error occured while watching for pod changes. Will try to resubscribe."))
                                          .retryWhen(new RetryFunction().withDelay(WATCH_RETRY_DELAY_MS).withRetries(WATCH_RETRIES).create())
                                          .doOnError(e -> log.error("Error occured while watching for pod changes.", e)));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.hcagent.ResourceChecker#processEvent(java.lang.Object)
     */
    @Override
    protected Completable processEvent(V1Pod pod)
    {
        return Completable.fromAction(() ->
        {
            // Extract pod configuration and gather all pod data in one place
            var podData = this.getPodConfig(pod);

            /*
             * five possible phase values
             * 
             * Pending: The pod has been accepted by the Kubernetes system, but one or more
             * of the container images has not been created. This includes time before being
             * scheduled as well as time spent downloading images over the network, which
             * could take a while.
             * 
             * Running: The pod has been bound to a node, and all of the containers have
             * been created. At least one container is still running, or is in the process
             * of starting or restarting.
             * 
             * Succeeded: All containers in the pod have terminated in success, and will not
             * be restarted.
             * 
             * Failed: All containers in the pod have terminated, and at least one container
             * has terminated in failure. The container either exited with non-zero status
             * or was terminated by the system.
             * 
             * Unknown: For some reason the state of the pod could not be obtained,
             * typically due to an error in communicating with the host of the pod.
             * 
             */

            switch (podData.getPhase())
            {
                case "PENDING":
                case "UNKNOWN":
                    this.processEventPendingUnknownPhase(podData);
                    break;

                case "RUNNING":
                    this.processEventRunningPhase(pod, podData);
                    break;

                case "SUCCEEDED":
                    this.processEventSucceededPhase(podData);
                    break;

                case "FAILED":
                    this.processEventFailedPhase(podData);
                    break;

                default:
                    log.error("Unexpected pod phase {} identified for POD:{}", podData.getPodName(), podData.getPhase());
            }
        }).doOnError(e -> log.error("An error occured while processing incoming event ", e));

    }

    private void processEventPendingUnknownPhase(PodData podData)
    {
        if (!this.cacheAlarmHandler.existsInAlarmCache(podData.getPodName()))
        {
            this.cacheAlarmHandler.propagate(new CacheItem(podData.getPodName(), CacheAction.ADD, Optional.of(podData)));
            log.debug("State PENDING/UNKNOWN. Pod {} added in alarm cache", podData.getPodName());
        }
    }

    private void processEventRunningPhase(V1Pod pod,
                                          PodData podData)
    {
        var cs = checkContainersStatuses(pod);

        // check if pod exists in waiting cache
        if (this.cacheAlarmHandler.existsInAlarmCache(podData.getPodName()))
        {
            if (cs == ContainerStatus.RUNNING_OR_SUCCESS)
            {
                // phase: RUNNING, containers: RUNNING.
                // Remove it because now it is working again
                this.cacheAlarmHandler.propagate(new CacheItem(podData.getPodName(), CacheAction.REMOVE, Optional.of(podData)));
                log.debug("State RUNNING. Containers Running. Pod {} removed from pending alarm cache", podData.getPodName());
            }
        }
        else
        {
            // there is no alarm for this pod and it does not exist in alarm
            // cache
            if (cs == ContainerStatus.FAILURE || cs == ContainerStatus.WAITING)
            {
                // at least one container of the pod
                // has terminated with code != 0
                // create alarm
                podData.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
                this.cacheAlarmHandler.propagate(new CacheItem(podData.getPodName(), CacheAction.ADD, Optional.of(podData)));
            }
        }
    }

    private void processEventSucceededPhase(PodData podData)
    {
        if (this.cacheAlarmHandler.existsInAlarmCache(podData.getPodName()))
        {
            // alarm exists for this pod, remove it from alarm cache
            this.cacheAlarmHandler.propagate(new CacheItem(podData.getPodName(), CacheAction.REMOVE, Optional.of(podData)));
            log.debug("State SUCCEEDED. Alarm exists in alarm cache. Pod {} removed from alarm cache", podData.getPodName());
        }
    }

    private void processEventFailedPhase(PodData podData)
    {
        // create/update alarm in alarm cache
        podData.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
        this.cacheAlarmHandler.propagate(new CacheItem(podData.getPodName(), CacheAction.ADD, Optional.of(podData)));
        log.debug("State FAILED. Alarm added/updated in alarm cache. Pod {} updated in alarm cache", podData.getPodName());
    }

    private PodData getPodConfig(V1Pod pod)
    {
        String podName = pod.getMetadata().getName();
        log.debug("Updating contoller and replicas for pod {}", podName);
        Integer replicas;
        var controllerName = "";
        List<V1OwnerReference> ownerReferenceList = pod.getMetadata().getOwnerReferences();
        if (ownerReferenceList == null)
        {
            log.error("POD {} has no ownerReferences", podName);
            return new PodData(podName, "", pod.getStatus().getPhase().toUpperCase());
        }

        for (V1OwnerReference ownerRef : pod.getMetadata().getOwnerReferences())
        {
            replicas = 0;
            if (Boolean.TRUE.equals(ownerRef.getController()))
            {
                switch (ownerRef.getKind().toLowerCase())
                {
                    case REPLICASET:
                        Pair<String, Integer> replicaSetPair = this.getDeploymentConfig(ownerRef.getName());
                        controllerName = replicaSetPair.getFirst();
                        replicas = replicaSetPair.getSecond();
                        break;

                    case STATEFULSET:
                        Pair<String, Integer> statefulSetPair = this.getStatefulSetConfig(ownerRef.getName());
                        controllerName = statefulSetPair.getFirst();
                        replicas = statefulSetPair.getSecond();
                        break;

                    case DAEMONSET:
                        controllerName = ownerRef.getName();
                        break;

                    default:
                        // unknown controller
                        controllerName = "";
                        break;
                }
                log.debug("POD {} has controller {} with kind: {} and replicas: {}", podName, controllerName, ownerRef.getKind(), replicas);
            }

            if (controllerName.isBlank())
            {
                log.debug("POD {} has no valid controller.", podName);
                continue;
            }

            controllerName = controllerName.contains(ERIC_DATA_WIDE_COLUMN_DATABASE_CD) ? this.alignControllers(controllerName) : controllerName;

            log.debug("Updating CONTROLLER {} of pod {} replicas to {}.", controllerName, podName, replicas);
            this.cacheAlarmHandler.addController(controllerName, replicas);
        }

        return new PodData(podName, controllerName, pod.getStatus().getPhase().toUpperCase());
    }

    private String alignControllers(String crtlName)
    {

        log.debug("Aligning controller of WCDB!");
        if (crtlName.contains(TLS_RESTARTER))
        {
            return crtlName.replaceAll(ERIC_DATA_WIDE_COLUMN_DATABASE_CD + ".*?" + TLS_RESTARTER, ERIC_DATA_WIDE_COLUMN_DATABASE_CD + "-" + TLS_RESTARTER);
        }
        else if (crtlName.contains(CONFIGURE_KEYSPACES))
        {
            return crtlName.replaceAll(ERIC_DATA_WIDE_COLUMN_DATABASE_CD + ".*?" + CONFIGURE_KEYSPACES,
                                       ERIC_DATA_WIDE_COLUMN_DATABASE_CD + "-" + CONFIGURE_KEYSPACES);
        }
        else
        {
            return ERIC_DATA_WIDE_COLUMN_DATABASE_CD;

        }
    }

    /**
     * Help function to stip pod or pod-ownerReference names and extract directly
     * the controller name
     * 
     * @param resource the name of pod or pod-ownerReference
     * @return the possible controller name
     */
    private String stripResource(String resource)
    {
        return resource.replaceAll("-\\w*\\d.*", "");
    }

    private Pair<String, Integer> getDeploymentConfig(String name)
    {
        log.debug("Extracting Deployment controller and replicas from ReplicaSet: {}", name);

        Pair<String, Integer> deploymentInfo = this.getDeploymentOwnerReferences(name);

        var deploymentName = deploymentInfo.getFirst();
        var replicas = deploymentInfo.getSecond();

        if (deploymentInfo.getFirst().isEmpty())
            return Pair.of("", 0);

        if (this.verifyController(DEPLOYMENT, deploymentName))
            return Pair.of(deploymentName, replicas);

        log.error("Failed to verify deployment controller {} in deployment list", deploymentName);
        return Pair.of("", replicas);
    }

    private Pair<String, Integer> getDeploymentOwnerReferences(String name)
    {
        var deploymentName = stripResource(name);
        List<V1OwnerReference> ownerReferences = null;
        Integer replicas = 0;

        try
        {
            ownerReferences = this.appsV1Api.readNamespacedReplicaSet(name, this.params.getNamespace(), null) // get replicaset with name
                                            .getMetadata() // get metadata
                                            .getOwnerReferences();  // get owner references

            if (ownerReferences != null)
            {
                Optional<String> tmpDeployment = ownerReferences.stream() // stream the list of ownerReferences identified
                                                                .filter(o -> Boolean.TRUE.equals(o.getController())) // get owner references that correspond to
                                                                                                                     // controllers
                                                                .map(V1OwnerReference::getName) // swap from controller ownerReference objects to controller
                                                                                                // names
                                                                .findFirst(); // get the first controller (impossible to have multiple controllers)
                if (tmpDeployment.isPresent())
                {
                    deploymentName = tmpDeployment.get();
                    replicas = this.appsV1Api.readNamespacedDeployment(deploymentName, this.params.getNamespace(), null).getSpec().getReplicas();

                    log.debug("Set controller {} and replicas {} for replicaset {}", deploymentName, replicas, name);
                    return Pair.of(deploymentName, replicas);
                }
            }

            return Pair.of("", 0);
        }
        catch (ApiException e)
        {
            if (e.getCode() == 404)
                log.debug("Deployment {} not found, using default controller {}.", name, deploymentName);
            else
                log.debug("Failed to extract deployment from {}.\nApiException-> Status Code: {}\nHeaders: {}\nResponse Body: {}",
                          name,
                          e.getCode(),
                          e.getResponseHeaders(),
                          e.getResponseBody());
        }

        return Pair.of("", 0);
    }

    private Pair<String, Integer> getStatefulSetConfig(String name)
    {
        Integer replicas = 0;
        log.debug("Extracting replicas from StatefulSet: {}", name);
        try
        {
            replicas = this.appsV1Api.readNamespacedStatefulSet(name, this.params.getNamespace(), null).getSpec().getReplicas();
            log.debug("Set replicas {} for statefulset {}", replicas, name);
        }
        catch (ApiException e)
        {
            log.warn("Failed to extract statefulset from {}.\nApiException-> Status Code: {}\nHeaders: {}\nResponse Body: {}",
                     name,
                     e.getCode(),
                     e.getResponseHeaders(),
                     e.getResponseBody());
        }

        if (this.verifyController(STATEFULSET, name))
            return Pair.of(name, replicas);

        log.error("Failed to verify statefulset controller {} in statefulset list", name);
        return Pair.of("", replicas);
    }

    private boolean verifyController(String kind,
                                     String controller)
    {
        switch (kind)
        {
            case DEPLOYMENT:
                return this.verifyDeploymentController(controller);
            case STATEFULSET:
                return this.verifyStatefulsetController(controller);

            default:
                log.debug("Unable to check controller {} of kind {}", controller, kind);
        }
        return false;
    }

    private boolean verifyDeploymentController(String deployment)
    {
        var result = false;

        try
        {
            result = this.appsV1Api // get Kubernetes api appsV1Api
                                   .listNamespacedDeployment(this.params.getNamespace(), null, false, null, null, null, null, null, null, null, false) // get
                                   // V1DeploymentList
                                   // from kubernetes
                                   // api
                                   .getItems() // get list of V1Deployment
                                   .stream() // stream the list of deployments
                                   .map(V1Deployment::getMetadata) // change the
                                                                   // deployment
                                                                   // to
                                                                   // metadata
                                   .map(V1ObjectMeta::getName) // change the
                                                               // metadata to
                                                               // deployment
                                                               // name
                                   .anyMatch(n -> n.equals(deployment)); // check
                                                                         // if
                                                                         // extracted
                                                                         // controller
                                                                         // exists
        }
        catch (ApiException e)
        {
            log.warn("Failed to identify expected controller {} of deployment kind.\nApiException-> Status Code: {}\nHeaders: {}\nResponse Body: {}",
                     deployment,
                     e.getCode(),
                     e.getResponseHeaders(),
                     e.getResponseBody());
        }

        return result;
    }

    private boolean verifyStatefulsetController(String statefulset)
    {
        var result = false;

        try
        {
            result = this.appsV1Api // get Kubernetes api appsV1Api
                                   .listNamespacedStatefulSet(this.params.getNamespace(), null, false, null, null, null, null, null, null, null, false) // get
                                   // V1StatefulSetList
                                   // from
                                   // kubernetes api
                                   .getItems() // get list of V1StatefulSet
                                   .stream() // stream the list of V1StatefulSet
                                   .map(V1StatefulSet::getMetadata) // change
                                                                    // V1StatefulSet
                                                                    // to
                                                                    // V1ObjectMeta
                                   .map(V1ObjectMeta::getName) // change
                                                               // V1ObjectMeta
                                                               // to statefulset
                                                               // name
                                   .anyMatch(n -> n.equals(statefulset)); // check
                                                                          // if
                                                                          // extracted
                                                                          // controller
                                                                          // exists
        }
        catch (ApiException e)
        {
            log.warn("Failed to identify expected controller {} of statefulset kind.\nApiException-> Status Code: {}\nHeaders: {}\nResponse Body: {}",
                     statefulset,
                     e.getCode(),
                     e.getResponseHeaders(),
                     e.getResponseBody());
        }

        return result;
    }

    /**
     * Checks the containers' statuses of the provided pod
     * 
     * @param pod
     * @return The container status
     */
    private ContainerStatus checkContainersStatuses(V1Pod pod)
    {
        if (pod.getStatus().getContainerStatuses() == null)
        {
            log.debug("All containers of pod {} in RUNNING or SUCCESS state.", pod.getMetadata().getName());
            return ContainerStatus.RUNNING_OR_SUCCESS;
        }

        for (V1ContainerStatus status : pod.getStatus().getContainerStatuses())
        {
            V1ContainerState state = status.getState();

            if (state.getTerminated() != null)
            {
                if (state.getTerminated().getExitCode() != 0)
                {
                    log.debug("Container {} of pod {} in FAILURE state.", status.getName(), pod.getMetadata().getName());
                    // at least one container has status terminated with
                    // exitcode != 0
                    return ContainerStatus.FAILURE;
                }
                log.debug("Container {} of pod {} in TERMINATED state, ready state {} and exit code {}.",
                          status.getName(),
                          pod.getMetadata().getName(),
                          status.getReady(),
                          state.getTerminated().getExitCode());
            }
            else if (state.getWaiting() != null)
            {
                log.debug("Container {} of pod {} in WAITING state.", status.getName(), pod.getMetadata().getName());
                // at least one container has status waiting
                return ContainerStatus.WAITING;
            }
            else if (state.getRunning() != null)
            {
                log.debug("Container {} of pod {} in RUNNING state and with ready state {}.", status.getName(), pod.getMetadata().getName(), status.getReady());
            }
        }

        log.debug("All containers of pod {} in RUNNING or SUCCESS state.", pod.getMetadata().getName());
        return ContainerStatus.RUNNING_OR_SUCCESS;
    }
}
