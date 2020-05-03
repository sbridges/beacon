package com.github.sbridges.beacon.jmx.inspector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.github.sbridges.beacon.internal.FirstException;
import com.github.sbridges.beacon.listeners.RecordedEventListener;

import jdk.jfr.consumer.RecordedEvent;

/**
 * Implementation of {@link InspectorMXBean}
 */
public final class Inspector implements RecordedEventListener, InspectorMXBean {
    
    private final Object lock = new Object();
    private final ArrayDeque<String> elements;
    private final int size;
    private final FirstException firstException = new FirstException();

    public Inspector(InspectorConfig config) {
        this.size = config.getSize();
        this.elements = new ArrayDeque<>(size);
    }

    @Override
    public void hear(RecordedEvent e) {
        String asString = e.toString();
       synchronized(lock) {
           if(elements.size() == size) {
               elements.removeFirst();
           }
           elements.addLast(asString);
       }
    }
    
    @Override
    public String[] getLastEvent() {
        String event;
        synchronized(lock) {
            if(elements.isEmpty()) {
                return null;
            }
            event = elements.getLast();
        }
        return event.split("\n");
    }
    
    @Override
    public String[] getAllEvents() {
        List<String> copy;
        synchronized(lock) {
            copy = new ArrayList<>(elements);
        }
        if(copy.isEmpty()) {
            return null;
        }
        
        return copy.stream()
                .flatMap(t -> Arrays.stream(t.split("\n")))
                .toArray(String[]::new);
    }

    @Override
    public String[] getAllEventsNotSplit() {
        synchronized(lock) {
            if(elements.isEmpty()) {
                return null;
            }
            return elements.toArray(String[]::new);
        }
    }
    
    @Override
    public void flush() {}

    @Override
    public String toString() {
        return "Inspector [size=" + size + "]";
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
        Inspector other = (Inspector) obj;
        return size == other.size;
    }

    public int getSize() {
        return size;
    }


    @Override
    public void hearException(Exception e) {
        firstException.hear(e);
    }

    @Override
    public String[] getFirstException() {
        return firstException.get();
    }
    
    
}
