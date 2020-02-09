package com.github.sbridges.beacon.internal.inspector;

import static org.junit.Assert.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.github.sbridges.beacon.internal.test.event.TestEventFactory;
import com.github.sbridges.beacon.jmx.inspector.Inspector;
import com.github.sbridges.beacon.jmx.inspector.InspectorConfig;

import jdk.jfr.consumer.RecordedEvent;

public class InspectorTest {

    @Test
    public void test() {
        List<RecordedEvent> events = TestEventFactory.createRecordedEvents(
                t -> t.aString = "hey",
                t -> t.aString = "now",
                t -> t.aString = "hank"
                
                );
        
        Inspector inspector = new Inspector(new InspectorConfig(2));
        
        assertNull(inspector.getAllEvents());
        assertNull(inspector.getLastEvent());
        assertNull( inspector.getAllEventsNotSplit());
        
        inspector.hear(events.get(0));
        inspector.flush();
        
        assertEquals(
                events.get(0).toString().trim(), 
                Stream.of(inspector.getLastEvent()).collect(Collectors.joining("\n")));
        assertEquals(
                events.get(0).toString().trim(), 
                Stream.of(inspector.getAllEvents()).collect(Collectors.joining("\n")));
        assertEquals(
                events.get(0).toString(), 
                inspector.getAllEventsNotSplit()[0]);
        
        
        inspector.hear(events.get(1));
        inspector.hear(events.get(2));
        
        
        
        assertEquals(
                events.get(2).toString().trim(), 
                Stream.of(inspector.getLastEvent()).collect(Collectors.joining("\n")));
        
        
        assertEquals(
                events.get(1).toString(), 
                inspector.getAllEventsNotSplit()[0]);
        assertEquals(
                events.get(2).toString(), 
                inspector.getAllEventsNotSplit()[1]);
        
    }

}
