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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.hcagent.PodData.FaultIndicationStatus;
import com.ericsson.sc.hcagent.ResourceChecker.ClusterCheck;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.reactivex.Completable;
import io.reactivex.subjects.PublishSubject;

public class CacheAlarmHandler
{
    private static final Logger log = LoggerFactory.getLogger(CacheAlarmHandler.class);
    private final FmAlarmService alarmService;
    private boolean alarmRaised = false;
    private final CoreV1Api api;
    private final CacheAlarmData cad;
    protected final AlarmPhaseTimer alarmPhaseTimer;
    protected final AlarmRenewTimer alarmRenewTimer;
    protected final PublishSubject<CacheItem> cacheItemsEmitter = PublishSubject.create();

    public CacheAlarmHandler(CacheAlarmData cacheAlarmData,
                             FmAlarmService alarmService,
                             CoreV1Api api,
                             ClusterCheck clusterCheck,
                             Long alarmCacheExpiration)
    {
        this.alarmService = alarmService;
        this.api = api;
        this.cad = cacheAlarmData;
        this.alarmPhaseTimer = new AlarmPhaseTimer(this, this.cad);
        this.alarmRenewTimer = new AlarmRenewTimer(clusterCheck, this, this.cad, this.api, alarmCacheExpiration);
    }

    public void propagate(CacheItem item)
    {
        this.cacheItemsEmitter.onNext(item);
    }

    /**
     * Processes the alarms in the internal alarm cache. If the alarm cache has at
     * least one element and there is no alarm for this resource, then an alarm is
     * raised. Otherwise if alarm cache is empty and an alarm is raised, the alarm
     * should be ceased.
     */
    private synchronized Completable processAlarmsInCache(CacheAction action)
    {
        var failedResources = this.cad.alarmCache.entrySet()
                                                 .stream()
                                                 .filter(entry -> entry.getValue().getFaultIndicationStatus().equals(FaultIndicationStatus.ACTIVE))
                                                 .toList();
        return Completable.defer(() ->
        {
            if (failedResources.isEmpty())
            {
                if (this.alarmRaised)
                    return this.ceaseAlarm()
                               .andThen(this.alarmRenewTimer.stop())
                               .doOnSubscribe(s -> log.debug("Alarm cache is empty and an alarm is raised. Ceasing the alarm now"))
                               .doOnError(e -> log.error("An error occured while processing alarm cache", e));
            }

            else
            {
                if (action != CacheAction.NONE)
                    return this.raiseAlarm()
                               .andThen(this.alarmRenewTimer.reset())
                               .doOnError(e -> log.error("An error occured while processing alarm cache", e))
                               .doOnSubscribe(s -> log.debug("Alarm cache has changed. Raising an alarm"));

            }

            return Completable.complete();

        }).onErrorComplete();
    }

    public boolean existsInAlarmCache(String pod)
    {
        return this.cad.existsInAlarmCache(pod);
    }

    public void addController(String key,
                              Integer value)
    {
        this.cad.addController(key, value);
    }

    protected Completable raiseAlarm()
    {
        return Completable.complete()
                          .andThen(Completable.fromAction(() -> this.cad.updateFaultIndication(this.alarmPhaseTimer.getTimeout())))
                          .andThen(Completable.defer(() -> this.alarmService.raise(this.cad.getFaultIndication())
                                                                            .retry(1)
                                                                            .doOnError(e -> log.error("Alarm {} could not be updated due to error: {}",
                                                                                                      this.cad.getFaultIndication().getFaultName(),
                                                                                                      e))
                                                                            .doOnComplete(() ->
                                                                            {
                                                                                log.debug("Updated alarm {} in alarm handler for resources {}",
                                                                                          this.cad.getFaultIndication().getFaultName(),
                                                                                          this.cad.getAlarmCache().keySet());
                                                                                this.alarmRaised = true;
                                                                            })));
    }

    private Completable ceaseAlarm() throws JsonProcessingException
    {
        return Completable.complete()
                          .andThen(this.alarmService.cease(this.cad.getFaultIndication())
                                                    .retry(1)
                                                    .doOnError(e -> log.error("Alarm {} could not be ceased: {}", //
                                                                              this.cad.getFaultIndication().getFaultName(),
                                                                              e))
                                                    .doOnComplete(() ->
                                                    {
                                                        log.debug("Ceased alarm {}", this.cad.getFaultIndication().getFaultName());
                                                        this.alarmRaised = false;
                                                    }));
    }

    public Completable run()
    {
        return Completable.complete()
                          .andThen(this.alarmPhaseTimer.start())
                          .andThen(this.cacheItemsEmitter.filter(item -> item.getAction() != CacheAction.NONE)//
                                                         .doOnNext(item ->
                                                         {
                                                             if (item.getAction() == CacheAction.ADD)
                                                             {
                                                                 this.cad.addInAlarmCache(item.getPodData());
                                                             }
                                                             else
                                                             {
                                                                 this.cad.removeFromAlarmCache(item.getPodName());
                                                             }
                                                         })
                                                         .debounce(2, TimeUnit.SECONDS)//
                                                         .concatMapCompletable(item -> this.processAlarmsInCache(item.getAction()))
                                                         .doOnError(e -> log.error("Error occured in cache alarm handler", e))
                                                         .doOnSubscribe(s -> log.debug("Starting cache alarm handler"))
                                                         .doOnTerminate(() -> log.debug("Terminating cache alarm handler")));
    }

    public Completable stop()
    {
        return Completable.complete() //
                          .andThen(this.alarmRenewTimer.stop())
                          .andThen(this.alarmPhaseTimer.stop());
    }

    public enum CacheAction
    {
        ADD,
        REMOVE,
        NONE
    }

}
