package com.ericsson.sc.hcagent;

import java.util.Objects;

import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.vertx.core.json.JsonObject;

public class AlarmSeverity
{
    /*
     * Name of the pod
     */
    @JsonProperty("service-name")
    private String name;

    /*
     * Highest severity to be reported
     */
    @JsonProperty("highest-severity-reported")
    private Severity hs;

    /*
     * Type of controller Available values: - replicaset - statefulset - daemonset -
     * job
     */
    @JsonProperty("replication-controller-type")
    private String type;

    /*
     * Number of minimum pods required for the service to be available
     */
    @JsonProperty("minimum-replicas-required")
    private Integer minimum;

    /*
     * Number of pods required for the service to be highly available
     */
    @JsonProperty("high-availability-replicas-required")
    private Integer ha;

    public AlarmSeverity()
    {
        // empty constructor
    }

    public AlarmSeverity(String name,
                         Severity hs,
                         String type,
                         Integer minimum,
                         Integer ha)
    {
        this.name = name;
        this.hs = hs;
        this.type = type;
        this.minimum = minimum;
        this.ha = ha;
    }

    public String getName()
    {
        return this.name;
    }

    public Severity getHighestSeverity()
    {
        return this.hs;
    }

    public String getType()
    {
        return this.type;
    }

    public boolean minimumRequiredInvalid()
    {
        return this.minimum.equals(0);
    }

    public Integer getMimumRequired()
    {
        return this.minimum;
    }

    public boolean highAvailableRequiredInvalid()
    {
        return this.ha.equals(0);
    }

    public Integer getHighAvailableRequired()
    {
        return this.ha;
    }

    public String prettyJsonPrint()
    {
        return toJson().toString();
    }

    public String toString()
    {
        return prettyJsonPrint();
    }

    public JsonObject toJson()
    {
        var alarmSeverity = new JsonObject();
        alarmSeverity.put("service-name", this.name);
        alarmSeverity.put("highest-severity-reported", this.hs.toString());
        alarmSeverity.put("replication-controller-type", this.type);
        alarmSeverity.put("minimum-replicas-required", this.minimum);
        alarmSeverity.put("high-availability-replicas-required", this.ha);
        return alarmSeverity;
    }

    @Override
    public boolean equals(Object o)
    {
        // self check
        if (this == o)
            return true;
        // null check
        if (o == null)
            return false;
        // type check and cast
        if (getClass() != o.getClass())
            return false;

        AlarmSeverity severity = (AlarmSeverity) o;

        // field comparison
        return Objects.equals(this.getName(), severity.getName()) && Objects.equals(this.getType(), severity.getType())
               && Objects.equals(this.getHighestSeverity().toString(), severity.getHighestSeverity().toString())
               && Objects.equals(this.getMimumRequired(), severity.getMimumRequired())
               && Objects.equals(this.getHighAvailableRequired(), severity.getHighAvailableRequired());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getName() == null) ? 0 : this.getName().hashCode());
        return result;
    }
}