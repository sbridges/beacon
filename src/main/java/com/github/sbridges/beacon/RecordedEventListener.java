package com.github.sbridges.beacon;

import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordedEvent;

/**
 * A listener of RecordedEvents 
 */
public interface RecordedEventListener {
    
    /**
     * A recorded event has occurred that this {@link RecordedEventListener} has registered for.
     */
    void hear(RecordedEvent e);

    /**
     * Signal to the Event Listener that a batch
     * of events have been processed, and aggregate
     * stats should be recorded
     * 
     * @see EventStream#onFlush(Runnable)
     */
    void flush();
}
