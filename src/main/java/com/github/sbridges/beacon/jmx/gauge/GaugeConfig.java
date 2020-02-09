package com.github.sbridges.beacon.jmx.gauge;

import java.util.Objects;

/**
 * Gauge configuration
 */
public final class GaugeConfig {

    private final String eventField;

    public GaugeConfig(String eventField) {
        this.eventField = eventField;
    }

    public String getEventField() {
        return eventField;
    }

    @Override
    public String toString() {
        return "GaugeConfig [eventField=" + eventField + "]";
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
        GaugeConfig other = (GaugeConfig) obj;
        return Objects.equals(eventField, other.eventField);
    }
    
    
    
}
