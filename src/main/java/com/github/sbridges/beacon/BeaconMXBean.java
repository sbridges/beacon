package com.github.sbridges.beacon;

import javax.management.MXBean;

/**
 * MXBean for configuring Beacon. 
 */
@MXBean
public interface BeaconMXBean {

    /**
     * @return the current configuration, or null if this instance was created using a list of Beans
     */
    public String[] getConf();
    
    /**
     * Update the configuration for Beacon.<P>
     * 
     * If Beacon is running, this will unregister any existing JMX beans and will register any newly configured JMX beans.<P>
     */
    public void updateConf(String newConf);
    
    /**
     * @return how many jfr events have we received from our RecordingStream 
     */
    public long getRecordedEvents();
    
    /**
     * @return how many jfr flushes have we received from our RecordingStream 
     */
    public long getFlushes();
    
    
}
