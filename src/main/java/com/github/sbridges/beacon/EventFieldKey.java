package com.github.sbridges.beacon;

import com.github.sbridges.beacon.internal.Util;

import java.util.*;

/**
 * List of event key value pairs
 */
public final class EventFieldKey {

    private final String event;
    private final String field;
    private final List<String> keys;

    public EventFieldKey(String event, String field, List<String> keys) {
        this.event = event;
        this.field = field;
        this.keys = Util.immutableCopyOf(keys);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EventFieldKey.class.getSimpleName() + "[", "]")
                .add("event='" + event + "'")
                .add("field='" + field + "'")
                .add("keys=" + keys)
                .toString();
    }

    public String getEvent() {
        return event;
    }

    public String getField() {
        return field;
    }

    public List<String> getKeys() {
        return keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventFieldKey that = (EventFieldKey) o;
        return Objects.equals(event, that.event) &&
                Objects.equals(field, that.field) &&
                Objects.equals(keys, that.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, field, keys);
    }
}
