package com.github.sbridges.beacon.jmx.topsum;


/**
 * Stat for {@link TopSumMXBean}
 */
public interface TopSumStatMBean {
    
    /**
     * @return the key for this stat 
     */
    String getKey();
    
    /**
     * @return the value for this stat
     */
    double getValue();
    
    /**
     * @return the duration for all events summed over the last reporting period
     */
    long getTotalDuration();
    
    /**
     * @return the number of events seen over the last reporting period
     */
    int events();
}
