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
 * Created on: Feb 22, 2024
 *     Author: zengpav
 */

package com.ericsson.sc.hcagent;

import java.util.Objects;

/**
 * 
 */
public class AlarmSeverityKey
{
    private String serviceName;
    private String configMapFileName;

    /**
     * @param serviceName
     * @param configMapFileName
     */
    public AlarmSeverityKey(String serviceName,
                            String configMapFileName)
    {
        this.serviceName = serviceName;
        this.configMapFileName = configMapFileName;
    }

    /**
     * @return the serviceName
     */
    public String getServiceName()
    {
        return serviceName;
    }

    /**
     * @return the configMapFileName
     */
    public String getConfigMapFileName()
    {
        return configMapFileName;
    }

    @Override
    public String toString()
    {
        return "AlarmSeverityKey [serviceName=" + serviceName + ", configMapFileName=" + configMapFileName + "]";
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(configMapFileName, serviceName);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AlarmSeverityKey other = (AlarmSeverityKey) obj;
        return Objects.equals(configMapFileName, other.configMapFileName) && Objects.equals(serviceName, other.serviceName);
    }
}
