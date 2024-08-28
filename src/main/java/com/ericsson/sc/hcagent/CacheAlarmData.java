package com.ericsson.sc.hcagent;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.model.fi.AdditionalInformation;
import com.ericsson.sc.fm.model.fi.AdditionalInformation.AdditionalInformationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.sc.hcagent.PodData.FaultIndicationStatus;

public class CacheAlarmData
{
    private static final Logger log = LoggerFactory.getLogger(CacheAlarmData.class);
    private static final String FAILEDRESOURCES = "failedResources";
    private FaultIndication faultIndication;
    private ConcurrentHashMap<String, Integer> controllers = new ConcurrentHashMap<>();
    private Severity alarmSeverity = Severity.CLEAR;
    protected ConcurrentHashMap<String, PodData> alarmCache = new ConcurrentHashMap<>();
    private AdditionalInformation additionalInformation;
    private SeveritiesTracker severitiesTracker;

    public CacheAlarmData(SeveritiesTracker severitiesTracker,
                          FaultIndication faultIndication)
    {
        this.faultIndication = faultIndication;
        this.controllers = new ConcurrentHashMap<>();
        this.severitiesTracker = severitiesTracker;

    }

    /**
     * Adds the provided alarm in the alarm cache
     * 
     * @param id    Key attribute for cache
     * @param alarm
     */
    public synchronized void addInAlarmCache(PodData pd)
    {
        this.alarmCache.put(pd.getPodName(), pd);
        log.debug("Pod {} added in Alarm Cache with phase {}", pd.getPodName(), pd.getFaultIndicationStatus());
        if (pd.getFaultIndicationStatus().equals(FaultIndicationStatus.ACTIVE))
        {
            log.debug("Alarm phase of pod {} changed to ACTIVE", pd.getPodName());
            this.additionalInformation = new AdditionalInformationBuilder().withAdditionalProperty(FAILEDRESOURCES,
                                                                                                   this.alarmCache.entrySet()
                                                                                                                  .stream()
                                                                                                                  .filter(entry -> entry.getValue()
                                                                                                                                        .getFaultIndicationStatus()
                                                                                                                                        .equals(FaultIndicationStatus.ACTIVE))
                                                                                                                  .toList())
                                                                           .build();
        }
    }

    /**
     * Removes an alarm from the alarm cache based on its resource id.
     * 
     * @param id
     */
    public synchronized void removeFromAlarmCache(String pd)
    {
        this.alarmCache.remove(pd);
        log.debug("Pod {} removed from Alarm Cache", pd);
        var failedResources = this.alarmCache.entrySet()
                                             .stream()
                                             .filter(entry -> entry.getValue().getFaultIndicationStatus().equals(FaultIndicationStatus.ACTIVE))
                                             .toList();
        this.additionalInformation = failedResources.isEmpty() ? new AdditionalInformationBuilder().build()
                                                               : new AdditionalInformationBuilder().withAdditionalProperty(FAILEDRESOURCES, failedResources)
                                                                                                   .build();
    }

    public synchronized void calculateAlarmSeverity()
    {
        Integer availablePods = 0;
        var finalAlarmSeverity = Severity.CLEAR;
        Severity tmpAlarmSeverity;

        for (Map.Entry<String, Integer> controller : this.controllers.entrySet())
        {
            String controllerName = controller.getKey();
            Integer replicas = controller.getValue();
            String controllerType = this.severitiesTracker.getAlarmSeverity(controllerName).getType();
            var svcAlarmSeverity = this.severitiesTracker.getAlarmSeverity(controllerName);
            log.debug("Checking controller {} of type {} with replicas: {}, AlarmSeverity:: {}", controllerName, controllerType, replicas, svcAlarmSeverity);
            tmpAlarmSeverity = Severity.CLEAR;
            Integer failedPODs = this.getControllerFailedPods(controller);

            if (failedPODs > 0)
            {

                switch (controllerType)
                {
                    case "deployment":
                    case "statefulset":
                        availablePods = replicas - failedPODs;
                        tmpAlarmSeverity = this.calculateReplicaBasedSeverity(availablePods, svcAlarmSeverity);
                        log.debug("Controller {} has {}/{} replicas available. Severity calculated: {}",
                                  controllerName,
                                  availablePods,
                                  replicas,
                                  tmpAlarmSeverity);
                        break;
                    case "daemonset":
                        tmpAlarmSeverity = svcAlarmSeverity.getHighestSeverity();
                        log.debug("Controller {} has no replicas available, Highest Severity requested: {}.", controllerName, tmpAlarmSeverity);
                        break;

                    default:
                        log.warn("Unexpected or invalid value type {} for controller {}.", controllerType, controllerName);
                        break;
                }

                log.debug("Current severity: {}, try to change severity: {} to {}", this.alarmSeverity, finalAlarmSeverity, tmpAlarmSeverity);

                // Check that new temporary alarm severity is higher that final alarm severity
                if (tmpAlarmSeverity.compareTo(finalAlarmSeverity) > 0)
                {
                    log.debug("Severity update: New severity set to {}.", finalAlarmSeverity);
                    finalAlarmSeverity = tmpAlarmSeverity;
                }
                else
                {
                    log.debug("Skip severity update: Severity {} higher or equal than calculated severity {}.", finalAlarmSeverity, tmpAlarmSeverity);
                }

                if (finalAlarmSeverity == Severity.CRITICAL)
                {
                    log.debug("Severity update complete: Highest severity {} reached.", finalAlarmSeverity);
                    break;
                }
            }
            else
            {
                log.debug("No failed PODs identified for controller {}.", controllerName);
            }

        }

        log.debug("Controllers: {}", this.controllers.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).toList());
        log.debug("AlarmCache: {}", this.alarmCache.entrySet().stream().map(Map.Entry::getKey).toList());
        log.debug("Calculated alarm severity: {}", finalAlarmSeverity);
        this.alarmSeverity = finalAlarmSeverity;
    }

    private Integer getControllerFailedPods(Entry<String, Integer> controller)
    {
        return this.alarmCache.entrySet()
                              .stream()
                              .filter(entry -> entry.getValue().getControllerName().equals(controller.getKey()))
                              .map(Map.Entry::getValue)
                              .toList()
                              .size();
    }

    private Severity calculateReplicaBasedSeverity(Integer availablePods,
                                                   AlarmSeverity alarmSeverity)
    {
        switch (alarmSeverity.getHighestSeverity())
        {
            case CRITICAL:
            case MAJOR:
                return calculateWithThresholds(availablePods, alarmSeverity);

            case MINOR:
                return Severity.MINOR;

            default:
                log.error("Unexpected value for the highest severity using default WARNING severity.");
                return Severity.WARNING;
        }
    }

    /**
     * Get previous severity based on current severity input
     * 
     * @param severity current severity input
     * @return
     */
    private Severity getPrevious(Severity severity)
    {
        switch (severity)
        {
            case CRITICAL:
                return Severity.MAJOR;
            case MAJOR:
                return Severity.MINOR;
            default:
                return Severity.WARNING;
        }

    }

    private Severity calculateWithThresholds(Integer availablePods,
                                             AlarmSeverity alarmSeverity)
    {
        // Get highest alarm severity configured
        var highestSeverity = alarmSeverity.getHighestSeverity();

        // Get previous severity based on fault indication severity levels
        var prevHighest = getPrevious(highestSeverity);

        // compare to MINOR and return MINOR or keep previous severity
        var firstPrevHighest = prevHighest.compareTo(Severity.MINOR) <= 0 ? Severity.MINOR : prevHighest;

        // Get next previous severity based on fault indication severity levels
        var secondPrevHighest = getPrevious(prevHighest);

        // compare to MINOR and return MINOR or keep second previous severity
        secondPrevHighest = secondPrevHighest.compareTo(Severity.MINOR) <= 0 ? Severity.MINOR : secondPrevHighest;

        if (alarmSeverity.minimumRequiredInvalid())
        {
            if ((alarmSeverity.highAvailableRequiredInvalid()) || (availablePods < alarmSeverity.getHighAvailableRequired()))
                return alarmSeverity.getHighestSeverity();
            return firstPrevHighest;
        }

        if (availablePods < alarmSeverity.getMimumRequired())
            return alarmSeverity.getHighestSeverity();

        if ((alarmSeverity.getHighAvailableRequired() == 0) || (availablePods < alarmSeverity.getHighAvailableRequired()))
            return firstPrevHighest;

        return secondPrevHighest;
    }

    public Severity getAlarmSeverity()
    {
        return this.alarmSeverity;
    }

    public synchronized void updateFaultIndication(Long expiration)
    {
        this.calculateAlarmSeverity();
        this.faultIndication = new FaultIndicationBuilder(this.faultIndication).withExpiration(expiration) //
                                                                               .withSeverity(this.alarmSeverity) //
                                                                               .withAdditionalInformation(this.additionalInformation)
                                                                               .build();
    }

    public FaultIndication getFaultIndication()
    {
        return this.faultIndication;
    }

    public ConcurrentMap<String, Integer> getControllers()
    {
        return this.controllers;
    }

    public void addController(String key,
                              Integer value)
    {
        this.controllers.put(key, value);
    }

    public ConcurrentMap<String, PodData> getAlarmCache()
    {
        return this.alarmCache;
    }

    public boolean existsInAlarmCache(String pod)
    {
        return this.alarmCache.containsKey(pod);
    }
}
