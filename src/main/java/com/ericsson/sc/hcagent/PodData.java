package com.ericsson.sc.hcagent;

import io.vertx.core.json.JsonObject;

public class PodData
{
    private String pod;
    private String phase;
    private String controller;
    private long creationTimestamp;

    public enum FaultIndicationStatus
    {
        PENDING,
        ACTIVE
    }

    private FaultIndicationStatus faultIndicationStatus;

    public PodData()
    {
        // empty constructor
    }

    public PodData(String pod,
                   String controller,
                   String phase)
    {
        this.pod = pod;
        this.phase = phase;
        this.controller = controller;
        this.creationTimestamp = System.currentTimeMillis();
        this.faultIndicationStatus = FaultIndicationStatus.PENDING;
    }

    public void setPodName(String pod)
    {
        this.pod = pod;
    }

    public void setPodPhase(String phase)
    {
        this.phase = phase;
    }

    public void setFaultIndicationStatus(FaultIndicationStatus faultIndicationStatus)
    {
        this.faultIndicationStatus = faultIndicationStatus;
    }

    public void setPodController(String controller)
    {
        this.controller = controller;
    }

    public String getControllerName()
    {
        return this.controller;
    }

    public String getPhase()
    {
        return this.phase;
    }

    public String getPodName()
    {
        return this.pod;
    }

    public boolean checkFaultDuration()
    {
        return this.calculateAlarmTimeout() >= 30000;
    }

    public FaultIndicationStatus getFaultIndicationStatus()
    {
        return this.faultIndicationStatus;
    }

    @Override
    public String toString()
    {
        return this.prettyJsonPrint();
    }

    public String prettyJsonPrint()
    {
        return this.toJson().toString();
    }

    public JsonObject toJson()
    {
        var data = new JsonObject();
        data.put("pod-name", this.pod);
        data.put("pod.controller", this.controller);
        data.put("pod.phase", this.phase);
        return data;
    }

    private long calculateAlarmTimeout()
    {
        return (System.currentTimeMillis() - this.creationTimestamp);
    }

}
