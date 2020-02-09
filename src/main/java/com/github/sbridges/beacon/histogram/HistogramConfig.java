package com.github.sbridges.beacon.histogram;

import java.util.Objects;

/**
 * Histogram Configuration
 */
public class HistogramConfig {

    private final String eventField;
    
    public HistogramConfig(String eventField) {
        this.eventField = eventField;
    }

    public String getEventField() {
        return eventField;
    }

    @Override
    public String toString() {
        return "HistogramConfig [eventField=" + eventField + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventField);
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
        HistogramConfig other = (HistogramConfig) obj;
        return Objects.equals(eventField, other.eventField);
    }
    
    
}
