package com.github.sbridges.beacon.internal.topsum;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
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

    StubClock clock = new StubClock();
    long now = clock.millis();
    
    class Data  {

        final long val;
        final String key;

        public Data(long val, String key) {
            this.val = val;
            this.key = key;
        }
    }

    List<Data> events = Arrays.asList(
            new Data(1, "a"),
            new Data(1, "a"),
            new Data(2, "b"),
            new Data(2, "b"),
            new Data(3, "c"),
            new Data(3, "c")

    );
    
    @Test
    public void testEmpty() throws Exception {
        TopSum top = new TopSum(
                new TopConfig(Duration.of(1, ChronoUnit.SECONDS)),
                clock);
        
        assertEquals(0, top.getStats().length);
        assertNull(top.getStatsReport());
        
    }
    
    @Test
    public void testTop() throws Exception {
        TopSum top = new TopSum(
                new TopConfig(Duration.of(1, ChronoUnit.SECONDS)),
                clock);
        
        top.flush();
        for(Data e : events) {
            top.hear(e.key, e.val, Duration.of(0, ChronoUnit.HOURS));
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
                "key                  value                invocations          duration (ms)       \n" +
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
    
    
    @Test
    public void testTopLongKey() throws Exception {
        
        TopSum top = new TopSum(
                new TopConfig(Duration.of(1, ChronoUnit.SECONDS)),
                clock);
        
        top.flush();

        top.hear("averyveryverywellnotsoverylongstring", 1, Duration.of(0, ChronoUnit.HOURS));

        
        clock.advanceMillis(1000);
        top.flush();
        
        assertEquals(
                
      "key                                  value                invocations          duration (ms)       \n" +
              "------------------------------------ -------------------- -------------------- -------------------- \n" +
              "averyveryverywellnotsoverylongstring                    1                    1                    0",
                Stream.of(top.getStatsReport()).collect(Collectors.joining("\n")));
        
    }
}
