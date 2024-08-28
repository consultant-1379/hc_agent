package com.ericsson.sc.hcagent;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.hcagent.CacheAlarmHandler.CacheAction;
import com.ericsson.sc.hcagent.ResourceChecker.ClusterCheck;
import com.ericsson.utilities.common.Pair;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.reactivex.Completable;
import io.reactivex.Flowable;

public class AlarmRenewTimer extends AlarmTimerEngine
{
    private static final Logger log = LoggerFactory.getLogger(AlarmRenewTimer.class);
    private ClusterCheck clusterCheck;
    CoreV1Api api;

    public AlarmRenewTimer(ClusterCheck clusterCheck,
                           CacheAlarmHandler cah,
                           CacheAlarmData cad,
                           CoreV1Api api,
                           Long alarmExpiration)
    {
        super(cad, cah, alarmExpiration - 10L);
        this.api = api;
        this.clusterCheck = clusterCheck;
    }

    protected Completable start()
    {
        return Completable.fromAction(() -> this.timer = this.alarmTimer.doOnNext(l -> log.debug("Alarm cache timeout"))
                                                                        .filter(l -> !cad.getAlarmCache().isEmpty())
                                                                        .concatMap(l -> Flowable.fromCallable(() ->
                                                                        {
                                                                            var acPods = cad.getAlarmCache().keySet();
                                                                            var clusterPods = clusterCheck.fetchResources(api);

                                                                            return Pair.of(acPods, clusterPods);
                                                                        }))
                                                                        .concatMapCompletable(pair ->
                                                                        {

                                                                            var acPods = pair.getFirst();
                                                                            var clusterPods = pair.getSecond();
                                                                            var resourceToRemoveFromAc = acPods.stream()
                                                                                                               .filter(acPod -> !clusterPods.contains(acPod))
                                                                                                               .toList();
                                                                            if (resourceToRemoveFromAc.isEmpty()
                                                                                && !cad.getFaultIndication().getFaultyResource().isEmpty())
                                                                            {
                                                                                log.info("The failed pods {} still exist both in cluster and alarm cache. Updating the raised alarm",
                                                                                         acPods);
                                                                                // pods exist in alarm cache
                                                                                // they also exist in the cluster in failed state (Otherwise it
                                                                                // would not exist in alarm cache in the first place)
                                                                                // so update the alarm
                                                                                return this.cah.raiseAlarm();
                                                                            }
                                                                            else
                                                                            {
                                                                                resourceToRemoveFromAc.forEach(pod ->
                                                                                {
                                                                                    log.info("The failed pod {} exists in alarm cache but not in the cluster. Removing it from alarm cache.",
                                                                                             pod);
                                                                                    // pod exists in alarm cache
                                                                                    // but it does not exist anymore in cluster
                                                                                    // remove it from alarm cache
                                                                                    this.cah.propagate(new CacheItem(pod,
                                                                                                                     CacheAction.REMOVE,
                                                                                                                     Optional.empty()));
                                                                                });

                                                                                return Completable.complete();
                                                                            }

                                                                        })
                                                                        .onErrorComplete(InterruptedException.class::isInstance)
                                                                        .retry(3)
                                                                        .doOnError(e ->
                                                                        {
                                                                            log.error("An error occured when monitoring alarm cache", e);
                                                                            this.cah.cacheItemsEmitter.onError(e);
                                                                        })
                                                                        .toFlowable()
                                                                        .doOnSubscribe(s -> log.debug("Started monitoring alarm cache"))
                                                                        .doOnTerminate(() -> log.debug("Monitoring alarm cache terminated"))
                                                                        .doOnCancel(() -> log.debug("Monitoring alarm cache cancelled"))
                                                                        .subscribe());
    }
}
