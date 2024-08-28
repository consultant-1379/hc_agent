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

import java.util.Objects;

import com.ericsson.utilities.common.EnvVars;

/**
 * 
 */
public class HealthCheckAgentParameters
{
    // health check agent hostname
    private final String hostname;

    // Initial delay for starting health check agent
    private final Long initialDelay;

    // parameters to be used for checking pod failures
    private final ResourceCheckerParameters resourceCheckerParameters;

    // parameters to be used for communication with alarm handler service
    private final AlarmHandlerParameters alarmHandlerParameters;

    public HealthCheckAgentParameters(String hostname,
                                      Long initialDelay,
                                      ResourceCheckerParameters resourceCheckerParameters,
                                      AlarmHandlerParameters alarmHandlerParameters)
    {
        Objects.requireNonNull(hostname);
        Objects.requireNonNull(initialDelay);
        Objects.requireNonNull(resourceCheckerParameters);
        Objects.requireNonNull(alarmHandlerParameters);

        this.hostname = hostname;
        this.initialDelay = initialDelay;
        this.resourceCheckerParameters = resourceCheckerParameters;
        this.alarmHandlerParameters = alarmHandlerParameters;
    }

    public String getHostname()
    {
        return this.hostname;
    }

    public ResourceCheckerParameters getResourceCheckerParameters()
    {
        return this.resourceCheckerParameters;
    }

    public AlarmHandlerParameters getAlarmHandlerParameters()
    {
        return this.alarmHandlerParameters;
    }

    public Long getInitDelay()
    {
        return this.initialDelay;
    }

    public static HealthCheckAgentParameters fromEnvironment()
    {
        final var resourceCheckerParameters = new ResourceCheckerParameters(EnvVars.get("NAMESPACE"),
                                                                            Long.parseLong(EnvVars.get("ALARM_EXPIRATION_TIMER")),
                                                                            Long.parseLong(EnvVars.get("PENDING_PHASE_TIMEOUT")),
                                                                            EnvVars.get("FAILED_POD_FAULT_NAME"),
                                                                            EnvVars.get("FAILED_POD_FAULT_RESOURCE"),
                                                                            EnvVars.get("FAILED_POD_SERVICE_NAME"));

        final var alarmHandlerParameters = new AlarmHandlerParameters(EnvVars.get("ALARM_HANDLER_HOST"),
                                                                      Integer.parseInt(EnvVars.get("ALARM_HANDLER_PORT")),
                                                                      Boolean.parseBoolean(EnvVars.get("GLOBAL_TLS_ENABLED", "true")),
                                                                      EnvVars.get("ALARM_HANDLER_CLIENT_CERT_PATH"),
                                                                      EnvVars.get("SIP_TLS_TRUSTED_ROOT_CA_PATH"));

        return new HealthCheckAgentParameters(EnvVars.get("SERVICE_HOSTNAME", "eric-sc-hcagent"),
                                              Long.parseLong(EnvVars.get("INITIAL_DELAY")),
                                              resourceCheckerParameters,
                                              alarmHandlerParameters);
    }
}
