package com.github.sbridges.beacon.internal.top;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.sbridges.beacon.internal.test.event.TestEventFactory;
import com.github.sbridges.beacon.jmx.top.Top;
import com.github.sbridges.beacon.jmx.top.TopConfig;
import com.github.sbridges.beacon.test.util.StubClock;

import jdk.jfr.consumer.RecordedEvent;

public class TopTest {
    static List<RecordedEvent> events; 
    
    StubClock clock = new StubClock(); 
    long now = clock.millis();
    
    @BeforeClass
    public static void createEvents() {
        events = TestEventFactory.createRecordedEvents(
                t -> {t.aLong = 1; t.aString = "a";},
                t -> {t.aLong = 2; t.aString = "b";},
                t -> {t.aLong = 3; t.aString = "c";}
                
                );
    }
    
    @Test
    public void testEmpty() throws Exception {
        Top top = new Top(
                new TopConfig(Arrays.asList("aString"), "aLong", Duration.of(1, ChronoUnit.SECONDS)),
                clock);
        
        assertEquals(0, top.getStats().length);
        assertNull(top.getStatsReport());
        
    }
    
    @Test
    public void testTop() throws Exception {
        Top top = new Top(
                new TopConfig(Arrays.asList("aString"), "aLong", Duration.of(1, ChronoUnit.SECONDS)),
                clock);
        
        top.flush();
        top.hear(events.get(1));
        top.hear(events.get(0));
        top.hear(events.get(2));
        
        clock.advanceMillis(1000);
        top.flush();
        
        assertEquals("c", top.getStats()[0].getKey());
        assertEquals(3.0, top.getStats()[0].getValue(), 0);
                
        assertEquals("b", top.getStats()[1].getKey());
        assertEquals(2.0, top.getStats()[1].getValue(), 0);
        
        assertEquals("a", top.getStats()[2].getKey());
        assertEquals(1.0, top.getStats()[2].getValue(), 0);
        
        assertEquals(
                "aString              aLong               \n" + 
                "-------------------- --------------------\n" + 
                "c                                       3\n" + 
                "b                                       2\n" + 
                "a                                       1", 
                Stream.of(top.getStatsReport()).collect(Collectors.joining("\n")));
        
        //no more events
        clock.advanceMillis(1000);
        top.flush();
        
        assertEquals(0, top.getStats().length);
    }
    
    @Test
    public void testTopLongKey() throws Exception {
        
        List<RecordedEvent> eventsLocal = TestEventFactory.createRecordedEvents(
                t -> {t.aLong = 1; t.aString = "averyveryverywellnotsoverylongstring";});
        
        
        Top top = new Top(
                new TopConfig(Arrays.asList("aString"), "aLong", Duration.of(1, ChronoUnit.SECONDS)),
                clock);
        
        top.flush();
        top.hear(eventsLocal.get(0));
        
        clock.advanceMillis(1000);
        top.flush();
        
        assertEquals(
                "aString                              aLong               \n" + 
                "------------------------------------ --------------------\n" + 
                "averyveryverywellnotsoverylongstring                    1", 
                Stream.of(top.getStatsReport()).collect(Collectors.joining("\n")));
        
    }
    
    
}
