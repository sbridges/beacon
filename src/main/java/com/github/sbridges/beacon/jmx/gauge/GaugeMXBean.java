package com.github.sbridges.beacon.jmx.gauge;

import java.io.IOException;

import javax.management.MXBean;

/**
 * MX Bean for a gauge.
 */ 
@MXBean
public interface GaugeMXBean {
    
    /**
     * @return the latest value of the gauge, or 0 if there was no value recorded
     */
    double getValue() throws IOException;
    
    /**
     * @return a description of the first failure to for this JMX bean
     * or null if no failures occurred.
     */
    String[] getFirstException() throws IOException;

}
