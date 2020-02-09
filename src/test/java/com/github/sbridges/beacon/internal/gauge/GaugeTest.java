package com.github.sbridges.beacon.internal.gauge;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.github.sbridges.beacon.internal.test.event.TestEventFactory;
import com.github.sbridges.beacon.jmx.gauge.Gauge;
import com.github.sbridges.beacon.jmx.gauge.GaugeConfig;

import jdk.jfr.consumer.RecordedEvent;


public class GaugeTest {


    @Test
    public void testGetSet() {

        List<RecordedEvent> events = TestEventFactory.createRecordedEvents(
               t -> t.aLong = 1,
               t -> t.aLong = 2
               );
       RecordedEvent re1 = events.get(0);
       RecordedEvent re2 = events.get(1);
       
       Gauge gauge = new Gauge(new GaugeConfig("aLong"));
       gauge.hear(re1);
       gauge.flush();
       assertEquals(1.0, gauge.getValue(), 0);
       
       gauge.hear(re2);
       gauge.flush();
       assertEquals(2.0, gauge.getValue(), 0);
       
       
       
    }

    
    @Test
    public void testGetSetByte() {

        List<RecordedEvent> events = TestEventFactory.createRecordedEvents(
               t -> t.aByte = 1,
               t -> t.aByte = 2
               );
       RecordedEvent re1 = events.get(0);
       RecordedEvent re2 = events.get(1);
       
       Gauge gauge = new Gauge(new GaugeConfig("aByte"));
       gauge.hear(re1);
       gauge.flush();
       assertEquals(1.0, gauge.getValue(), 0);
       
       gauge.hear(re2);
       gauge.flush();
       assertEquals(2.0, gauge.getValue(), 0);
       
       
       
    }

}
