package com.github.sbridges.beacon.jmx.rate;

import java.io.IOException;

import javax.management.MXBean;

/**
 * MXBean for exposing a field of a JFR event as a rate. 
 */
@MXBean
public interface RateMXBean {

    /**
     * @return the rate  
     */
    double getRatePerSecond() throws IOException;

    /**
     * @return the sun ofthe value for all jfr events 
     */
    double getTotal() throws IOException;

    /**
     * @return a description of the first failure to for this JMX bean
     * or null if no failures occurred.
     */
    String[] getFirstException() throws IOException;

}
