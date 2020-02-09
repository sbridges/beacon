package com.github.sbridges.beacon.jmx.topsum;

import java.io.IOException;

import javax.management.MXBean;

import com.github.sbridges.beacon.jmx.top.TopMXBean;



/**
 * JMX bean to return top name, value pairs for a JFR event.<p>
 * 
 * {@link TopSumMXBean} differs from {@link TopMXBean} in that {@link TopMXBean} uses the last value of a JFR event for a given
 * period, while {@link TopSumMXBean} sums all values of a JFR event over a period.<P>
 * 
 * For example, you would use TopSumMXBean to sum the number of bytes read for {@link jdk.jfr.events.FileReadEvent} events, while {@link TopMXBean}
 * would be used for jdk.ObjectCount events.<P>
 */
@MXBean
public interface TopSumMXBean {
    
    /**
     * @return stats for the top values of a JFR event. 
     */
    TopSumStatMBean[] getStats() throws IOException;
    
    /**
     * @return a String version of getStats
     */
    String[] getStatsReport() throws IOException;

    /**
     * @return a description of the first failure to for this JMX bean
     * or null if no failures occurred.
     */
    String[] getFirstException() throws IOException;
}
