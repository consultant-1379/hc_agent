package com.ericsson.sc.hcagent;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public abstract class AlarmTimerEngine
{
    protected Long alarmExpiration = 60L;
    protected final CacheAlarmData cad;
    protected Disposable timer;
    protected final CacheAlarmHandler cah;
    protected Flowable<Long> alarmTimer;

    protected AlarmTimerEngine(CacheAlarmData cad,
                               CacheAlarmHandler cah,
                               Long timeout)
    {
        this.cad = cad;
        this.cah = cah;
        this.alarmTimer = Flowable.interval(timeout, TimeUnit.SECONDS, Schedulers.io());
    }

    protected abstract Completable start();

    protected Completable stop()
    {
        return Completable.complete() //
                          .andThen(Completable.fromAction(() -> this.timer.dispose()));
    }

    protected Completable reset()
    {
        return Completable.defer(() ->
        {
            if (this.timer != null)
                this.timer.dispose();

            return this.start();
        });
    }

    public Long getTimeout()
    {
        return this.alarmExpiration;
    }
}
