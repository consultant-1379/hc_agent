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
public class AlarmHandlerParameters
{
    private final String fmAlarmHost;
    private final int fmAlarmPort;
    private final boolean tlsEnabled;
    private final String fmClientCertificatePath;
    private final String sipTlsTrustedRootPath;

    public AlarmHandlerParameters(String fmAlarmHost,
                                  int fmAlarmPort,
                                  boolean tlsEnabled,
                                  String fmClientCertificatePath,
                                  String sipTlsTrustedRootPath)
    {
        this.fmAlarmHost = fmAlarmHost;
        this.fmAlarmPort = fmAlarmPort;
        this.tlsEnabled = tlsEnabled;
        this.fmClientCertificatePath = fmClientCertificatePath;
        this.sipTlsTrustedRootPath = sipTlsTrustedRootPath;
    }

    public String getFmAlarmHost()
    {
        return this.fmAlarmHost;
    }

    public int getFmAlarmPort()
    {
        return this.fmAlarmPort;
    }

    public boolean tlsEnabled()
    {
        return this.tlsEnabled;
    }

    public String getFmClientCertPath()
    {
        return this.fmClientCertificatePath;
    }

    public String getFmClientCaPath()
    {
        return this.sipTlsTrustedRootPath;
    }
}
