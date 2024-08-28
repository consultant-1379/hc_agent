/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 25, 2022
 *     Author: ekoteva
 */

package com.ericsson.sc.hcagent;

/**
 * 
 */
public class ResourceCheckerParameters
{
    private final String namespace;
    private final Long alarmCacheExpiration;
    private final Long waitingCacheExpiration;
    private final String faultName;
    private final String faultResource;
    private final String faultServiceName;

    public ResourceCheckerParameters(String namespace,
                                     Long alarmCacheExpiration,
                                     Long waitingCacheExpiration,
                                     String faultName,
                                     String faultResource,
                                     String faultServiceName)
    {
        this.namespace = namespace;
        this.alarmCacheExpiration = alarmCacheExpiration;
        this.waitingCacheExpiration = waitingCacheExpiration;
        this.faultName = faultName;
        this.faultResource = faultResource;
        this.faultServiceName = faultServiceName;
    }

    public String getNamespace()
    {
        return this.namespace;
    }

    public Long getACExpiration()
    {
        return this.alarmCacheExpiration;
    }

    public Long getWCExpiration()
    {
        return this.waitingCacheExpiration;
    }

    public String getFaultName()
    {
        return this.faultName;
    }

    public String getFaultResource()
    {
        return this.faultResource;
    }

    public String getFaultServiceName()
    {
        return this.faultServiceName;
    }
}
