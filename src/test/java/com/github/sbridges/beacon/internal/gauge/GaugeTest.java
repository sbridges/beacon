package com.github.sbridges.beacon.internal.gauge;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import com.github.sbridges.beacon.Bean;
import com.github.sbridges.beacon.EventField;
import com.github.sbridges.beacon.internal.test.event.TestEvent;
import org.junit.Test;

import com.github.sbridges.beacon.internal.test.event.TestEventFactory;
import com.github.sbridges.beacon.jmx.gauge.Gauge;

import jdk.jfr.consumer.RecordedEvent;


public class GaugeTest {

    @Test
    public void testGetSet() {

        Gauge gauge = new Gauge();

       gauge.hear(1);
       gauge.flush();
       assertEquals(1.0, gauge.getValue(), 0);
       
       gauge.hear(2);
       gauge.flush();
       assertEquals(2.0, gauge.getValue(), 0);
    }

}
