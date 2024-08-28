package com.ericsson.sc.hcagent;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.utilities.file.RxFileWatch;
import com.ericsson.utilities.file.RxFileWatch.Event;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Health Check Agent Severities Tracker
 * <p>
 * Used for updating Cache Alarm Severities
 */
/**
 * 
 */
public class SeveritiesTracker
{
    private static final Logger log = LoggerFactory.getLogger(SeveritiesTracker.class);
    private Disposable severitiesWatchDisposable = null;
    private Flowable<Event> severitiesWatchFloable;
    private final ObjectMapper om = Jackson.om();
    private ConcurrentMap<AlarmSeverityKey, AlarmSeverity> alarmSeverities;
    private static final String INVALID = "invalid";
    private String severitiesPath;
    private boolean running = false;

    /**
     * Severities Tracker for alarm severities according to changes identified in
     * severity files.
     * 
     * @param severitiesPath
     * @param timeout
     */
    public SeveritiesTracker(String severitiesPath)
    {
        this.alarmSeverities = this.initializeSeverities(severitiesPath);
        this.severitiesPath = severitiesPath;
    }

    /**
     * Stop monitoring alarm severity files
     * 
     */
    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            // If SeverityTracker is stopped, no need to stop it again
            if (!this.isRunning())
            {
                log.debug("Severity tracker is already stopped");
                return;
            }

            this.severitiesWatchDisposable.dispose();

            this.running = false;
        });
    }

    /**
     * Start SeveritiesTracker to dynamically update the AlarmSeverities
     * 
     * @return
     */
    public Completable run()
    {
        return Completable.fromAction(() ->
        {
            // If SeverityTracker is running, no need to start it again
            if (this.isRunning())
            {
                log.debug("Severity tracker is already running");
                return;
            }

            this.severitiesWatchFloable = severitiesFileWatchFloable();

            this.severitiesWatchDisposable = this.severitiesWatchFloable.subscribe(s ->
            {
            }, err -> log.error("Severities watch operation terminated unexpectedly", err));

            this.running = true;
        });
    }

    /**
     * Watch severity file changes and update severities according to the severities
     * defined
     * 
     * @return
     */
    public Flowable<RxFileWatch.Event> severitiesFileWatchFloable()
    {
        /*
         * Create a flowable that watches for file changes on a given folder. Emittions
         * will be observed on a new thread.
         */
        return RxFileWatch.create(Path.of(this.severitiesPath), false)
                          .debounce(1, TimeUnit.SECONDS) // When a configmap changes multiple events are emitted. We keep only one.
                          .observeOn(Schedulers.newThread()) // Make updates on a new Thread
                          .doOnNext(e -> this.getSeverityFiles(this.severitiesPath).forEach(this::updateSeverities))
                          .doOnError(e -> log.error("Error occured while monitoring severities file changes", e))
                          .doOnSubscribe(sub -> log.info("Starting monitoring of severity configmaps"))
                          .doOnComplete(() -> log.info("Stopping monitoring of severity configmaps"));
    }

    /**
     * Update AlarmSeverities based on changed AlarmSeverities file
     * 
     * @param severitiesFile (The file that will take the updated AlarmSeverities
     *                       from)
     */
    private void updateSeverities(File severitiesFile)
    {
        AlarmSeverity[] updatedSeverities = null;
        var severitiesFilePath = severitiesFile.getAbsolutePath();

        log.debug("Updating alarm severities from {}", severitiesFilePath);

        try
        {
            // Read and encode the AlarmSeverities from the given file
            updatedSeverities = this.om.readValue(severitiesFile, AlarmSeverity[].class);
        }
        catch (Exception e)
        {
            log.error("Failed to load updated alarm severities from json file {}.", severitiesFilePath, e);
            return;
        }

        // Form updated severities map
        var updatedSeveritiesMap = convertArrayToMap(updatedSeverities, severitiesFilePath);

        // List of AlarmSeverityKeys corresponding to the AlarmSeverities of this file
        var keyListToUpdate = this.getAlarmSeverities().keySet().stream().filter(key -> key.getConfigMapFileName().equals(severitiesFilePath)).toList();

        /*
         * Check for AlarmSeverities to delete by iterating though the current list of
         * keys for AlarmSeverities of this file. If the AlarmSeverity is not in the
         * updated AlarmSeverities, remove this AlarmSeverity.
         */
        keyListToUpdate.stream().filter(key -> !updatedSeveritiesMap.containsKey(key)).forEach(key ->
        {
            log.info("Remove alarm severity: {}", this.getAlarmSeverities().get(key));
            this.alarmSeverities.remove(key);
        });

        /*
         * Check for AlarmSeverities to add or update by iterating through the updated
         * AlarmSeverities of this file
         */
        updatedSeveritiesMap.entrySet().stream().forEach(severityMap ->
        {
            var keyToUpdate = severityMap.getKey();
            var severityToUpdate = getAlarmSeverity(keyToUpdate.getServiceName());

            /*
             * Check if AlarmSeverity is in AlarmSeverities. If it is not then add the new
             * AlarmSeverity. If it is then check if update is needed for this AlarmSeverity
             * in the AlarmSeverities
             */
            if (severityToUpdate.getName().equals(INVALID))
            {
                this.alarmSeverities.putIfAbsent(keyToUpdate, severityMap.getValue());
                log.info("New alarm severity: {} ", severityMap.getValue());
            }
            else
            {
                /*
                 * Check if AlarmSeverity needs to be updated. If it does then update the
                 * AlarmSeverity in the AlarmSeverities. If it does not the take no action
                 */
                if (!severityToUpdate.equals(severityMap.getValue()))
                {
                    log.debug("Old alarm severity: {} ", severityToUpdate);
                    this.alarmSeverities.replace(keyToUpdate, severityMap.getValue());
                    log.info("Updated alarm severity: {} ", severityMap.getValue());
                }
                else
                {
                    log.debug("No update for {}", severityMap.getKey().getServiceName());
                }
            }
        });

        log.debug("Severities after the update: {}", this.getAlarmSeverities());
    }

    /**
     * Initialize AlarmSeverities given a folder containing the files of the
     * AlarmSeverities
     * 
     * @param severitiesPath (The path to the folder containing the severities
     *                       files)
     * @return
     */
    private ConcurrentMap<AlarmSeverityKey, AlarmSeverity> initializeSeverities(String severitiesPath)
    {
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> alarmSeverityHashMap = new ConcurrentHashMap<>();

        var severityConfigmaps = getSeverityFiles(severitiesPath);

        // Iterate though the AlarmSeverities files
        severityConfigmaps.stream().forEach(configmap ->
        {
            var configmapPath = configmap.getAbsolutePath();

            log.debug("Initializing severities from file {}", configmapPath);
            if (configmap.length() == 0)
            {
                log.warn("Configmap {} is empty", configmapPath);
                return;
            }

            log.debug("Severity file: {}.", configmap);
            AlarmSeverity[] severities;

            try
            {
                severities = this.om.readValue(configmap, AlarmSeverity[].class);
            }
            catch (Exception e)
            {
                log.error("Failed to load alarm severities from json file: {}.", configmap.getAbsolutePath(), e);
                return;
            }

            // Add each AlarmSeverity in the AlarmSeverities
            alarmSeverityHashMap.putAll(convertArrayToMap(severities, configmapPath));
        });

        log.debug("Alarm severities after initialization:\n{}", alarmSeverityHashMap.values());

        return alarmSeverityHashMap;
    }

    /**
     * Take an AlarmSeverity[] Array and convert it into a
     * ConcurrentMap<AlarmSeverityKey, AlarmSeverity>
     * 
     * @param severities        (AlarmSeverity[] Array to convert)
     * @param congigMapFileName (The name of the file that this set of severities
     *                          come from)
     * @return
     */
    public ConcurrentMap<AlarmSeverityKey, AlarmSeverity> convertArrayToMap(AlarmSeverity[] severities,
                                                                            String congigMapFileName)
    {
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> alarmSeverityHashMap = new ConcurrentHashMap<>();

        for (AlarmSeverity severity : severities)
        {
            alarmSeverityHashMap.put(new AlarmSeverityKey(severity.getName(), congigMapFileName), severity);
        }

        return alarmSeverityHashMap;
    }

    /**
     * Get an AlarmSeverity from the this.alarmSeverities map given the name of the
     * controller
     *
     * @param service (The name of the service whose AlarmSeverity the function will
     *                return)
     * @return
     */
    public AlarmSeverity getAlarmSeverity(String service)
    {

        for (AlarmSeverityKey key : this.getAlarmSeverities().keySet())
        {
            if (service.equals(key.getServiceName()))
                return this.getAlarmSeverities().get(key);
        }

        return new AlarmSeverity(INVALID, Severity.CLEAR, INVALID, 0, 0);
    }

    public ConcurrentMap<AlarmSeverityKey, AlarmSeverity> getAlarmSeverities()
    {
        return this.alarmSeverities;

    }

    public boolean isRunning()
    {
        return this.running;
    }

    public List<File> getSeverityFiles(String severityFolderPath)
    {
        File severityFolder = new File(severityFolderPath);

        // Check if given path is a Directory. If not then return an empty map
        if (!severityFolder.isDirectory())
        {
            log.warn("Path {} is not a directory. Failed to initialize severities", severitiesPath);
            return Collections.emptyList();
        }

        return Stream.of(severityFolder.listFiles()).filter(File::isFile).toList();
    }
}
