package com.ericsson.sc.hcagent;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.hcagent.CacheAlarmHandler.CacheAction;
import com.ericsson.sc.hcagent.PodData.FaultIndicationStatus;

import io.reactivex.Completable;

public class AlarmPhaseTimer extends AlarmTimerEngine
{
    private static final Logger log = LoggerFactory.getLogger(AlarmPhaseTimer.class);

    public AlarmPhaseTimer(CacheAlarmHandler cah,
                           CacheAlarmData cad)
    {
        super(cad, cah, 5L);
    }

    protected Completable start()
    {
        return Completable.fromAction(() -> this.timer = this.alarmTimer.doOnNext(l -> log.debug("Pending Alarm cache timeout"))
                                                                        .filter(l -> !cad.getAlarmCache().isEmpty())
                                                                        .concatMapCompletable(l ->
                                                                        {
                                                                            var acPods = cad.getAlarmCache().entrySet();
                                                                            acPods.forEach(pod ->
                                                                            {
                                                                                PodData podData = pod.getValue();
                                                                                if (podData.checkFaultDuration()
                                                                                    && podData.getFaultIndicationStatus() == FaultIndicationStatus.PENDING)
                                                                                {
                                                                                    podData.setFaultIndicationStatus(FaultIndicationStatus.ACTIVE);
                                                                                    this.cah.propagate(new CacheItem(podData.getPodName(),
                                                                                                                     CacheAction.ADD,
                                                                                                                     Optional.of(podData)));
                                                                                }
                                                                            });
                                                                            return Completable.complete();
                                                                        })
                                                                        .onErrorComplete(InterruptedException.class::isInstance)
                                                                        .retry(3)
                                                                        .doOnError(e ->
                                                                        {
                                                                            log.error("An error occured when monitoring pending alarm cache", e);
                                                                            this.cah.cacheItemsEmitter.onError(e);
                                                                        })
                                                                        .toFlowable()
                                                                        .doOnSubscribe(s -> log.debug("Started monitoring pending alarm cache"))
                                                                        .doOnTerminate(() -> log.debug("Monitoring pending alarm cache terminated"))
                                                                        .doOnCancel(() -> log.debug("Monitoring pending alarm cache cancelled"))
                                                                        .subscribe(sub ->
                                                                        {
                                                                        }, error -> log.error("An error occurred when monitoring pending alarm cache", error)));
    }
}
