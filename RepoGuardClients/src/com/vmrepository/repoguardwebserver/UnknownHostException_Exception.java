
package com.vmrepository.repoguardwebserver;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebFault(name = "UnknownHostException", targetNamespace = "http://RepoGuardWebServer.vmrepository.com/")
public class UnknownHostException_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private UnknownHostException faultInfo;

    /**
     * 
     * @param faultInfo
     * @param message
     */
    public UnknownHostException_Exception(String message, UnknownHostException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param faultInfo
     * @param cause
     * @param message
     */
    public UnknownHostException_Exception(String message, UnknownHostException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: com.vmrepository.repoguardwebserver.UnknownHostException
     */
    public UnknownHostException getFaultInfo() {
        return faultInfo;
    }

}
