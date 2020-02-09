package com.github.sbridges.beacon.jmx.top;

/**
 * Stat for {@link TopMXBean}
 */
public interface TopStatMBean {
    
    /**
     * @return the key for this stat 
     */
    String getKey();
    
    /**
     * @return the value for this stat
     */
    double getValue();
}
