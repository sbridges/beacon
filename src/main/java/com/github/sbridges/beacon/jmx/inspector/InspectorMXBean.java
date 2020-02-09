package com.github.sbridges.beacon.jmx.inspector;

import java.io.IOException;

import javax.management.MXBean;

/**
 * JMX bean that records the last instance of an event as a string 
 */
@MXBean
public interface InspectorMXBean {
    
    /**
     * @return the last instance of the JFR event as a String[], or null if no event has occurred
     */
    String[] getLastEvent() throws IOException;

    /**
     * This method will split an Event into multiple lines if the toString() of the event contains newlines
     * 
     * @return all collected JFR events as a String[], or null if no event has occurred.
     */
    String[] getAllEvents();

    /**
     * This method will not split an Event, each element in the String[] is the toString of an event
     * 
     * @return all collected JFR events as a String[], or null if no event has occurred.
     */
    String[] getAllEventsNotSplit();
}
