/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 3, 2023
 *     Author: zavasan
 */

package com.ericsson.sc.hcagent;

/**
 * 
 */
public class PodsNotFoundException extends RuntimeException
{

    private static final long serialVersionUID = 9179120012675014063L;

    public PodsNotFoundException(Throwable cause)
    {
        super(cause);
    }

    public PodsNotFoundException(String message,
                                 Throwable cause)
    {
        super(message, cause);
    }

    public PodsNotFoundException(String message)
    {
        super(message);
    }

}
