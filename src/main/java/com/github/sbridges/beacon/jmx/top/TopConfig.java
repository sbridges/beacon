package com.github.sbridges.beacon.jmx.top;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import com.github.sbridges.beacon.jmx.topsum.TopSum;

/**
 * configuration for {@link Top} and {@link TopSum}
 */
public final class TopConfig {

    private final String valueField;
    private final List<String> keyFields;
    private final Duration period;

    public TopConfig(List<String> keyFields, String valueField, Duration period) {
        this.keyFields = List.copyOf(keyFields);
        this.valueField = valueField;
        this.period = period;
    }

    public String getValueField() {
        return valueField;
    }

    public List<String> getKeyFields() {
        return keyFields;
    }

    public Duration getPeriod() {
        return period;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyFields, period, valueField);
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
        TopConfig other = (TopConfig) obj;
        return Objects.equals(keyFields, other.keyFields)
                && Objects.equals(period, other.period)
                && Objects.equals(valueField, other.valueField);
    }

    @Override
    public String toString() {
        return "TopConfig [valueField=" + valueField + ", keyFields="
                + keyFields + ", period=" + period + "]";
    }


    
}
