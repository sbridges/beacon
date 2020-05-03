package com.github.sbridges.beacon;

import java.util.Objects;
import java.util.StringJoiner;

public final class EventField {

    private final String event;
    private final String field;

    public EventField(String event, String field) {
        this.event = event;
        this.field = field;
    }

    public String getEvent() {
        return event;
    }

    public String getField() {
        return field;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EventField.class.getSimpleName() + "[", "]")
                .add("event='" + event + "'")
                .add("field='" + field + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventField that = (EventField) o;
        return Objects.equals(event, that.event) &&
                Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, field);
    }
}

