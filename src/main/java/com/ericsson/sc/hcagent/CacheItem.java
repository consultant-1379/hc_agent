/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 22, 2021
 *     Author: eaoknkr
 */

package com.ericsson.sc.hcagent;

import java.util.Optional;

import com.ericsson.sc.hcagent.CacheAlarmHandler.CacheAction;

/**
 * 
 */
public class CacheItem
{
    private final CacheAction action;
    private final Optional<PodData> podData;
    private final String podName;

    /**
     * @param action
     * @param podData
     */
    public CacheItem(String podName,
                     CacheAction action,
                     Optional<PodData> podData)
    {
        this.podName = podName;
        this.action = action;
        this.podData = podData;
    }

    /**
     * @return the action
     */
    public CacheAction getAction()
    {
        return action;
    }

    public String getPodName()
    {
        return this.podName;
    }

    /**
     * @return the podData
     */
    public PodData getPodData()
    {
        return podData.orElse(null);
    }

}
