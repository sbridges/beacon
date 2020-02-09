package com.github.sbridges.beacon;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * A Bean that exposes some JFR events over JMX. 
 */
public final class Bean {
    
    private final ObjectName objectName;
    private final String obectType;
    private final String eventName;
    //NOTE EventListener is also an MXBean
    private final RecordedEventListener listener;
    private final Optional<Duration> eventThreshold;
    private final Optional<Duration> eventPeriod;
    private final boolean stackTrace;
   
    public Bean(String objectName, 
            String obectType, 
            String eventName,
            Optional<Duration> eventPeriod, 
            Optional<Duration> eventThreshold, 
            boolean stackTrace,
            RecordedEventListener listener) {
        try {
            this.objectName = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("invalid objectName:" + objectName, e);
        }
        this.obectType = obectType;
        this.eventName = eventName;
        this.listener = listener;
        this.eventPeriod = eventPeriod;
        this.eventThreshold = eventThreshold;
        this.stackTrace = stackTrace;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public String getObectType() {
        return obectType;
    }

    public String getEventName() {
        return eventName;
    }

    public RecordedEventListener getListener() {
        return listener;
    }

    public Optional<Duration> getEventThreshold() {
        return eventThreshold;
    }

    public Optional<Duration> getEventPeriod() {
        return eventPeriod;
    }
    
    public boolean getStackTrace() {
        return stackTrace;
    }

    @Override
    public String toString() {
        return "Bean [objectName=" + objectName + ", obectType=" + obectType
                + ", eventName=" + eventName + ", listener=" + listener
                + ", eventThreshold=" + eventThreshold + ", eventPeriod="
                + eventPeriod + ", stackTrace=" + stackTrace + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventName, eventPeriod, eventThreshold, listener,
                obectType, objectName, stackTrace);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Bean other = (Bean) obj;
        return Objects.equals(eventName, other.eventName)
                && Objects.equals(eventPeriod, other.eventPeriod)
                && Objects.equals(eventThreshold, other.eventThreshold)
                && Objects.equals(listener, other.listener)
                && Objects.equals(obectType, other.obectType)
                && Objects.equals(objectName, other.objectName)
                && stackTrace == other.stackTrace;
    }

   
    
    
    
}
