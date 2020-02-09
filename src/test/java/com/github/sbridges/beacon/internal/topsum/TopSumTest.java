package com.github.sbridges.beacon.internal.topsum;

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
import com.github.sbridges.beacon.jmx.top.TopConfig;
import com.github.sbridges.beacon.jmx.topsum.TopSum;
import com.github.sbridges.beacon.test.util.StubClock;

import jdk.jfr.consumer.RecordedEvent;

public class TopSumTest {

    static List<RecordedEvent> events; 
    
    StubClock clock = new StubClock(); 
    long now = clock.millis();
    
    @BeforeClass
    public static void createEvents() {
        events = TestEventFactory.createRecordedEvents(
                t -> {t.aLong = 1; t.aString = "a";},
                t -> {t.aLong = 1; t.aString = "a";},
                t -> {t.aLong = 2; t.aString = "b";},
                t -> {t.aLong = 2; t.aString = "b";},
                t -> {t.aLong = 3; t.aString = "c";},
                t -> {t.aLong = 3; t.aString = "c";}
                
                );
    }
    
    @Test
    public void testEmpty() throws Exception {
        TopSum top = new TopSum(
                new TopConfig(Arrays.asList("aString"), "aLong", Duration.of(1, ChronoUnit.SECONDS)),
                clock);
        
        assertEquals(0, top.getStats().length);
        assertNull(top.getStatsReport());
        
    }
    
    @Test
    public void testTop() throws Exception {
        TopSum top = new TopSum(
                new TopConfig(Arrays.asList("aString"), "aLong", Duration.of(1, ChronoUnit.SECONDS)),
                clock);
        
        top.flush();
        for(RecordedEvent e : events) {
            top.hear(e);
        }
                
        
        clock.advanceMillis(1000);
        top.flush();
        
        assertEquals("c", top.getStats()[0].getKey());
        assertEquals(6.0, top.getStats()[0].getValue(), 0);
        assertEquals(2, top.getStats()[0].events());
                
        assertEquals("b", top.getStats()[1].getKey());
        assertEquals(4.0, top.getStats()[1].getValue(), 0);
        assertEquals(2, top.getStats()[1].events());
        
        assertEquals("a", top.getStats()[2].getKey());
        assertEquals(2.0, top.getStats()[2].getValue(), 0);
        assertEquals(2, top.getStats()[2].events());
        
        assertEquals(
                "aString              aLong                invocations          duration (ms)       \n" + 
                "-------------------- -------------------- -------------------- -------------------- \n" + 
                "c                                       6                    2                    0\n" + 
                "b                                       4                    2                    0\n" + 
                "a                                       2                    2                    0", 
                Stream.of(top.getStatsReport()).collect(Collectors.joining("\n")));
        
        //no more events
        clock.advanceMillis(1000);
        top.flush();
        
        assertEquals(0, top.getStats().length);
    }
}
