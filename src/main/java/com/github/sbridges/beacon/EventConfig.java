package com.github.sbridges.beacon;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Configuration for an event
 */
public final class EventConfig {
    private final String eventName;
    private final Optional<Duration> eventThreshold;
    private final Optional<Duration> eventPeriod;
    private final boolean stackTrace;
    
    public EventConfig(String eventName, Optional<Duration> eventThreshold,
            Optional<Duration> eventPeriod, boolean stackTrace) {
        this.eventName = eventName;
        this.eventThreshold = eventThreshold;
        this.eventPeriod = eventPeriod;
        this.stackTrace = stackTrace;
    }
    
    @Override
    public String toString() {
        return "EventConfig [eventName=" + eventName + ", eventThreshold="
                + eventThreshold + ", eventPeriod=" + eventPeriod
                + ", stackTrace=" + stackTrace + "]";
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventName, eventPeriod, eventThreshold, stackTrace);
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
        EventConfig other = (EventConfig) obj;
        return Objects.equals(eventName, other.eventName)
                && Objects.equals(eventPeriod, other.eventPeriod)
                && Objects.equals(eventThreshold, other.eventThreshold)
                && stackTrace == other.stackTrace;
    }

    public String getEventName() {
        return eventName;
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

    
}
