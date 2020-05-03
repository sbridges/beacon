package com.github.sbridges.beacon;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Configuration for a Beacon instance
 */
class Config {

    private final List<Bean> beans;
    private final List<EventConfig> events;

    public Config(List<Bean> beans, List<EventConfig> events) {
        this.beans = Collections.unmodifiableList(new ArrayList<>(beans));
        this.events = Collections.unmodifiableList(new ArrayList<>(events));

        Set<String> eventNames = events.stream()
                .map(t -> t.getEventName())
                .collect(Collectors.toSet());

        Set<String> missing = beans.stream()
                .flatMap(t -> t.getListeners().keySet().stream())
                .filter(t -> !eventNames.contains(t))
                .collect(Collectors.toSet());

        if(!missing.isEmpty()) {
            throw new IllegalStateException("not all events required by beans in event list, missing:" + missing);
        }
    }

    public List<Bean> getBeans() {
        return beans;
    }

    public List<EventConfig> getEvents() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return Objects.equals(beans, config.beans) &&
                Objects.equals(events, config.events);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beans, events);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Config.class.getSimpleName() + "[", "]")
                .add("beans=" + beans)
                .add("events=" + events)
                .toString();
    }
}
