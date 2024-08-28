package com.ericsson.sc.hcagent;

import static org.testng.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Completable;

public class SeveritiesTrackerTest
{

    private static final Logger log = LoggerFactory.getLogger(SeveritiesTrackerTest.class);
    private final ObjectMapper om = Jackson.om();
    private final String path1 = "severities_test/severities_test1.json";
    private final String path2 = "severities_test/severities_test2.json";
    private static final String INVALID = "invalid";
    private static final String OK = "ok";
    private static final String FOUND = "found";
    private static final String NOT_FOUND = "not_found";
    private static final String DIFFERENCE = "difference";
    private File file1;
    private File file2;
    private SeveritiesTracker SeveritiesTracker;
    private ConcurrentMap<AlarmSeverityKey, AlarmSeverity> initialAlarmSeverities1 = new ConcurrentHashMap<>();
    private ConcurrentMap<AlarmSeverityKey, AlarmSeverity> initialAlarmSeverities2 = new ConcurrentHashMap<>();

    @BeforeClass
    public void beforeClass()
    {
        log.info("Setup environment prior execution of any method in this test class.");

        try
        {

            var res1 = this.getClass().getResource(path1);
            var res2 = this.getClass().getResource(path2);

            assertTrue(res1 != null, "The file " + path1 + " does not exist.");
            assertTrue(res2 != null, "The file " + path2 + " does not exist.");

            this.file1 = new File(res1.getPath());
            this.file2 = new File(res2.getPath());

        }
        catch (Exception e)
        {
            assertTrue(false, "Testcase was unable to find the needed resources.");
        }

        this.initialAlarmSeverities1 = readAlarmSeverityList(this.file1);
        this.initialAlarmSeverities2 = readAlarmSeverityList(this.file2);
    }

    @BeforeMethod
    public void beforeMethod()
    {
        log.info("---------------- Start Unit Test -------------------");
        var severitiesPath = "";

        try
        {
            severitiesPath = this.getClass().getResource("severities_test/").getPath();

            log.debug("Severities folder path: {}", severitiesPath);
        }
        catch (Exception e)
        {
            log.error(e.toString());
            assertTrue(false, "Testcase was unable to find severities_test folder.");
        }

        // Start severitiesTracker
        this.SeveritiesTracker = new SeveritiesTracker(severitiesPath);
    }

    @AfterMethod
    public void afterMethod()
    {
        log.info("Stop SeverityTracker");

        Completable.complete().andThen(this.SeveritiesTracker.stop()).blockingAwait();

        log.info("Restore file alarm severities");
        writeToFile(initialAlarmSeverities1, this.file1);
        writeToFile(initialAlarmSeverities2, this.file2);

        log.info("---------------- End Unit Test -------------------");
    }

    @Test(enabled = true, priority = 1)
    public void SeverityFolderIsNotAFolderTest()
    {
        SeveritiesTracker tracker = new SeveritiesTracker(this.file1.getAbsolutePath());

        assertTrue(tracker.getAlarmSeverities().isEmpty(), "Alarm Severity list is not empty");
    }

    @Test(enabled = true, priority = 2)
    public void EmptySeverityFileTest()
    {
        var severitiesPath = "";
        try
        {
            severitiesPath = this.getClass().getResource("severities_empty/").getPath();

            log.debug("Severities folder path: {}", severitiesPath);
        }
        catch (Exception e)
        {
            log.error(e.toString());
            assertTrue(false, "Testcase was unable to find severities_test folder.");
        }

        SeveritiesTracker tracker = new SeveritiesTracker(severitiesPath);

        assertTrue(tracker.getAlarmSeverities().isEmpty(), "Alarm Severity list is not empty");
    }

    @Test(enabled = true, priority = 3)
    public void SimpleSeveritiesTest()
    {
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMapAll = new ConcurrentHashMap<>();

        // Start severitiesTracker
        startSeverityTracker(this.SeveritiesTracker);
        waitForSeverityTrackerToStart(this.SeveritiesTracker);

        // Read AlarmSeverity List from both files
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMap1 = readAlarmSeverityList(this.file1);
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMap2 = readAlarmSeverityList(this.file2);

        // Merge severities
        expectedAlarmSeverityMapAll.putAll(expectedAlarmSeverityMap1);
        expectedAlarmSeverityMapAll.putAll(expectedAlarmSeverityMap2);

        log.debug("Expected Map: {}", expectedAlarmSeverityMapAll);

        // Check all severities in actualAlarmSeverityList
        for (Map.Entry<AlarmSeverityKey, AlarmSeverity> severityMapToCheck : expectedAlarmSeverityMapAll.entrySet())
        {
            var actualAlarmSeverity = this.SeveritiesTracker.getAlarmSeverity(severityMapToCheck.getKey().getServiceName());
            assertTrue(!actualAlarmSeverity.getName().equals(INVALID),
                       "Alarm severity for " + severityMapToCheck.getKey().getServiceName() + " does not exist in actual Alarm Severities");

            assertTrue(actualAlarmSeverity.equals(severityMapToCheck.getValue()),
                       "Alarm severity for " + severityMapToCheck.getKey().getServiceName() + " is different to the epected Alarm Severities");
        }
    }

    @Test(enabled = true, priority = 4)
    public void StartSeveritiesWhenAlreadyRunningTest()
    {
        // Start severitiesTracker
        startSeverityTracker(this.SeveritiesTracker);
        waitForSeverityTrackerToStart(this.SeveritiesTracker);

        Completable.complete().andThen(this.SeveritiesTracker.run()).blockingAwait();

        assertTrue(this.SeveritiesTracker.isRunning(), "Severities Tracker not running after second run");
    }

    @Test(enabled = true, priority = 5)
    public void StopSeveritiesTrackerWhenNotStartedTest()
    {
        Completable.complete().andThen(this.SeveritiesTracker.stop()).blockingAwait();

        Assert.assertFalse(this.SeveritiesTracker.isRunning(), "Severities Tracker is running after second stop");
    }

    @Test(enabled = true, priority = 6)
    public void EditSeveritiesTest()
    {
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMapAll = new ConcurrentHashMap<>();

        // Start severitiesTracker
        startSeverityTracker(this.SeveritiesTracker);

        // Get init event from RxFileWatch
        this.SeveritiesTracker.severitiesFileWatchFloable().test().awaitCount(1);

        // Read AlarmSeverity List from both files
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMap1 = readAlarmSeverityList(this.file1);
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMap2 = readAlarmSeverityList(this.file2);

        log.debug("Map1: {}", expectedAlarmSeverityMap1);
        log.debug("Map2: {}", expectedAlarmSeverityMap2);

        // Change "service1" severity from critical to minor
        AlarmSeverity updatedSeverity = new AlarmSeverity("service1", Severity.MINOR, "deployment", 1, 2);
        expectedAlarmSeverityMap1.replace(new AlarmSeverityKey("service1", file1.getAbsolutePath()), updatedSeverity);

        // Change "service3" severity from critical to minor
        updatedSeverity = new AlarmSeverity("service3", Severity.MAJOR, "deployment", 2, 3);
        expectedAlarmSeverityMap2.replace(new AlarmSeverityKey("service3", file2.getAbsolutePath()), updatedSeverity);

        log.debug("Updated Map1: {}", expectedAlarmSeverityMap1);
        log.debug("Updated Map2: {}", expectedAlarmSeverityMap2);

        // Write Alarm changes to files
        writeToFile(expectedAlarmSeverityMap1, this.file1);
        writeToFile(expectedAlarmSeverityMap2, this.file2);

        // Wait to get file change event
        this.SeveritiesTracker.severitiesFileWatchFloable().test().awaitCount(1);

        // Merge severities
        expectedAlarmSeverityMapAll.putAll(expectedAlarmSeverityMap1);
        expectedAlarmSeverityMapAll.putAll(expectedAlarmSeverityMap2);

        log.debug("Expected Map: {}", expectedAlarmSeverityMapAll);

        // Check all severities in actualAlarmSeverityList
        assertSeveritiesList(expectedAlarmSeverityMapAll);
    }

    @Test(enabled = true, priority = 7)
    public void AddSeveritiesTest()
    {
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMapAll = new ConcurrentHashMap<>();

        // Start severitiesTracker
        startSeverityTracker(this.SeveritiesTracker);

        // Get init event from RxFileWatch
        this.SeveritiesTracker.severitiesFileWatchFloable().test().awaitCount(1);

        // Read AlarmSeverity List from both files
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMap1 = readAlarmSeverityList(this.file1);
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMap2 = readAlarmSeverityList(this.file2);

        // Add new alarm severity: "service5"
        AlarmSeverity updatedSeverity = new AlarmSeverity("service5", Severity.CRITICAL, "deployment", 0, 2);
        expectedAlarmSeverityMap1.put(new AlarmSeverityKey("service5", file1.getAbsolutePath()), updatedSeverity);

        // Add new alarm severity: "service6"
        updatedSeverity = new AlarmSeverity("service6", Severity.MAJOR, "deployment", 1, 3);
        expectedAlarmSeverityMap2.put(new AlarmSeverityKey("service6", file2.getAbsolutePath()), updatedSeverity);

        // Write Alarm changes to files
        writeToFile(expectedAlarmSeverityMap1, this.file1);
        writeToFile(expectedAlarmSeverityMap2, this.file2);

        // Wait to get file change event
        this.SeveritiesTracker.severitiesFileWatchFloable().test().awaitCount(1);

        // Merge severities
        expectedAlarmSeverityMapAll.putAll(expectedAlarmSeverityMap1);
        expectedAlarmSeverityMapAll.putAll(expectedAlarmSeverityMap2);

        log.debug("Map1: {}", expectedAlarmSeverityMap1);
        log.debug("Map2: {}", expectedAlarmSeverityMap2);

        log.debug("Updated Map1: {}", expectedAlarmSeverityMap1);
        log.debug("Updated Map2: {}", expectedAlarmSeverityMap2);

        log.debug("Expected Map: {}", expectedAlarmSeverityMapAll);

        // Check all severities in actualAlarmSeverityList
        assertSeveritiesList(expectedAlarmSeverityMapAll);
    }

    @Test(enabled = true, priority = 8)
    public void DeleteSeveritiesTest()
    {
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMapAll = new ConcurrentHashMap<>();

        // Start severitiesTracker
        startSeverityTracker(this.SeveritiesTracker);

        // Get init event from RxFileWatch
        this.SeveritiesTracker.severitiesFileWatchFloable().test().awaitCount(1);

        // Read AlarmSeverity List from both files
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMap1 = readAlarmSeverityList(this.file1);
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityMap2 = readAlarmSeverityList(this.file2);

        log.debug("Map1: {}", expectedAlarmSeverityMap1);
        log.debug("Map2: {}", expectedAlarmSeverityMap2);

        // Delete alarm severity: "service2"
        var keyToRemove = new AlarmSeverityKey("service2", file1.getAbsolutePath());
        expectedAlarmSeverityMap1.remove(keyToRemove);

        // Delete alarm severity: "service4"
        keyToRemove = new AlarmSeverityKey("service4", file2.getAbsolutePath());
        expectedAlarmSeverityMap2.remove(keyToRemove);

        log.debug("Updated Map1: {}", expectedAlarmSeverityMap1);
        log.debug("Updated Map2: {}", expectedAlarmSeverityMap2);

        // Write Alarm changes to files
        writeToFile(expectedAlarmSeverityMap1, this.file1);
        writeToFile(expectedAlarmSeverityMap2, this.file2);

        // Wait to get file change event
        this.SeveritiesTracker.severitiesFileWatchFloable().test().awaitCount(1);

        // Merge severities
        expectedAlarmSeverityMapAll.putAll(expectedAlarmSeverityMap1);
        expectedAlarmSeverityMapAll.putAll(expectedAlarmSeverityMap2);

        log.debug("Expected Map: {}", expectedAlarmSeverityMapAll);

        // Check all severities in actualAlarmSeverityList
        assertSeveritiesListDelete(expectedAlarmSeverityMapAll);
    }

    public ConcurrentMap<AlarmSeverityKey, AlarmSeverity> readAlarmSeverityList(File file)
    {
        AlarmSeverity[] severities;
        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedAlarmSeverityList = new ConcurrentHashMap<>();

        try
        {
            severities = this.om.readValue(file, AlarmSeverity[].class);
            for (AlarmSeverity severity : severities)
            {
                expectedAlarmSeverityList.put(new AlarmSeverityKey(severity.getName(), file.getAbsolutePath()), severity);
            }

        }
        catch (Exception e)
        {
            log.error("failed to read file with severities");
        }

        return expectedAlarmSeverityList;

    }

    public AlarmSeverity[] convertMapToArray(ConcurrentMap<AlarmSeverityKey, AlarmSeverity> alarmseverities)
    {
        AlarmSeverity array[] = new AlarmSeverity[alarmseverities.size()];
        var index = 0;
        for (AlarmSeverityKey key : alarmseverities.keySet())
        {
            array[index++] = alarmseverities.get(key);
        }

        return array;

    }

    public String constructSeveritiesString(ConcurrentMap<AlarmSeverityKey, AlarmSeverity> severities)
    {

        var builder = new StringBuilder();
        builder.append("[");
        severities.forEach((key,
                            severity) -> builder.append(severity.toString() + ","));

        builder.deleteCharAt(builder.lastIndexOf(","));
        builder.append("]");
        return builder.toString();

    }

    public void writeToFile(ConcurrentMap<AlarmSeverityKey, AlarmSeverity> severities,
                            File file)
    {
        BufferedWriter outputWriter;
        AlarmSeverity severitiesArray[] = convertMapToArray(severities);

        try
        {
            outputWriter = new BufferedWriter(new FileWriter(file));
            outputWriter.write(Arrays.toString(severitiesArray));
            outputWriter.close();
        }
        catch (IOException e)
        {
            log.error(e.toString());
            assertTrue(false, "Error writing to file");
        }
    }

    public AlarmSeverity getAlarmSeverity(ConcurrentMap<AlarmSeverityKey, AlarmSeverity> severities,
                                          String service)
    {

        for (AlarmSeverityKey key : severities.keySet())
        {
            if (Objects.equals(key.getServiceName(), service))
                return severities.get(key);
        }

        return new AlarmSeverity(INVALID, Severity.CLEAR, INVALID, 0, 0);
    }

    public void assertSeveritiesList(ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedSeverities)
    {
        String serviceName = "";
        String assertion1 = "";
        String assertion2 = "";

        log.debug("Calculate Assertions");
        // Check all severities in actualAlarmSeverityList
        for (Map.Entry<AlarmSeverityKey, AlarmSeverity> severityMapToCheck : expectedSeverities.entrySet())
        {
            serviceName = severityMapToCheck.getKey().getServiceName();
            var actualAlarmSeverity = this.SeveritiesTracker.getAlarmSeverity(serviceName);

            assertion1 = !actualAlarmSeverity.getName().equals(INVALID) ? OK : NOT_FOUND;
            assertion2 = actualAlarmSeverity.equals(severityMapToCheck.getValue()) ? OK : DIFFERENCE;

            if (!assertion1.equals(OK) || !assertion2.equals(OK))
            {
                break;
            }
        }

        Assert.assertNotEquals(assertion1, NOT_FOUND, "Alarm severity for " + serviceName + " does not exist in actual Alarm Severities");
        Assert.assertNotEquals(assertion1, DIFFERENCE, "Alarm severity for " + serviceName + " is different to the epected Alarm Severities");
    }

    public void assertSeveritiesListDelete(ConcurrentMap<AlarmSeverityKey, AlarmSeverity> expectedSeverities)
    {

        ConcurrentMap<AlarmSeverityKey, AlarmSeverity> actualSeverities;
        String serviceName = "";
        String assertion1 = "";
        String assertion2 = "";

        actualSeverities = this.SeveritiesTracker.getAlarmSeverities();
        log.debug("Calculate Assertions");

        // Check all severities in actualAlarmSeverityList
        for (Map.Entry<AlarmSeverityKey, AlarmSeverity> severityMapToCheck : actualSeverities.entrySet())
        {
            serviceName = severityMapToCheck.getKey().getServiceName();
            var expectedAlarmSeverity = getAlarmSeverity(expectedSeverities, serviceName);
            assertion1 = !expectedAlarmSeverity.getName().equals(INVALID) ? OK : FOUND;
            assertion2 = expectedAlarmSeverity.equals(severityMapToCheck.getValue()) ? OK : DIFFERENCE;

            if (!assertion1.equals(OK) || !assertion2.equals(OK))
            {
                break;
            }
        }

        Assert.assertNotEquals(assertion1, FOUND, "Alarm severity for " + serviceName + " exists in actual Alarm Severities");
        Assert.assertNotEquals(assertion1, DIFFERENCE, "Alarm severity for " + serviceName + " is different to the epected Alarm Severities");
    }

    public void waitForSeverityTrackerToStart(SeveritiesTracker tracker)
    {
        // Get init event from RxFileWatch
        this.SeveritiesTracker.severitiesFileWatchFloable().test().awaitCount(1);
    }

    public void startSeverityTracker(SeveritiesTracker tracker)
    {
        Completable.complete().andThen(tracker.run()).blockingAwait();
    }
}
