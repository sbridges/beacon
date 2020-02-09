package com.github.sbridges.beacon.jmx.inspector;

import java.util.Objects;

/**
 * Configuration for {@link Inspector} 
 */
public class InspectorConfig {

    private final int size;

    public InspectorConfig(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "InspectorConfig [size=" + size + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(size);
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
        InspectorConfig other = (InspectorConfig) obj;
        return size == other.size;
    }
    
    
}
