/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 5, 2024
 *     Author: zengpav
 */

package com.ericsson.sc.hcagent;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;

/**
 * 
 */
public class AlarmSeverityTest
{

    @Test(enabled = true)
    public void AlarmSeverityEqualsTest()
    {
        AlarmSeverity severityToCheck = new AlarmSeverity("mySeverity", Severity.MAJOR, "deployment", 1, 1);
        AlarmSeverity equalSeverity = new AlarmSeverity("mySeverity", Severity.MAJOR, "deployment", 1, 1);

        AlarmSeverity differentNameSeverity = new AlarmSeverity("otherSeverity", Severity.MAJOR, "deployment", 1, 1);
        AlarmSeverity differentSeverityLevelSeverity = new AlarmSeverity("mySeverity", Severity.CRITICAL, "deployment", 1, 1);
        AlarmSeverity differentTypeSeverity = new AlarmSeverity("mySeverity", Severity.MAJOR, "deamonset", 1, 1);
        AlarmSeverity differentMinumumSeverity = new AlarmSeverity("mySeverity", Severity.MAJOR, "deployment", 0, 1);
        AlarmSeverity differentHASeverity = new AlarmSeverity("mySeverity", Severity.MAJOR, "deployment", 1, 2);

        AlarmSeverity nullSeverity = null;
        String differentObject = "";

        assertTrue(severityToCheck.equals(equalSeverity));

        assertFalse(severityToCheck.equals(differentNameSeverity));
        assertFalse(severityToCheck.equals(differentSeverityLevelSeverity));
        assertFalse(severityToCheck.equals(differentTypeSeverity));
        assertFalse(severityToCheck.equals(differentMinumumSeverity));
        assertFalse(severityToCheck.equals(differentHASeverity));

        assertFalse(severityToCheck.equals(nullSeverity));
        assertFalse(severityToCheck.equals(differentObject));
    }

}
