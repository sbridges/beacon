package com.github.sbridges.beacon.histogram;

import java.io.IOException;

import javax.management.MXBean;

/**
 * MX Bean for a Histogram.<P>
 * 
 * Histogram records values over all time, and values over the last minute.<P>
 * 
 * Values over the last minute are updated at aproximately 1 minute intervals.<P>
 */ 
@MXBean
public interface HistogramMXBean {

    /**
     * @return a description of the first failure to for this JMX bean
     * or null if no failures occurred.
     */
    String[] getFirstException() throws IOException;
    
    
    double getAllTimeP50();
    double getAllTimeP90();
    double getAllTimeP95();
    double getAllTimeP99();
    double getAllTimeP999();
    double getAllTimeMax();
    double getAllTimeMean();
    long getAllTimeEvents();

    double getLastMinuteP50();
    double getLastMinuteP90();
    double getLastMinuteP95();
    double getLastMinuteP99();
    double getLastMinuteP999();
    double getLastMinuteMax();
    double getLastMinuteMean();
    long getLastMinuteEvents();




}
