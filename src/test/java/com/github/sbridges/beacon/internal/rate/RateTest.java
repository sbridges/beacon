package com.github.sbridges.beacon.internal.rate;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.sbridges.beacon.internal.test.event.TestEventFactory;
import com.github.sbridges.beacon.jmx.rate.Rate;
import com.github.sbridges.beacon.jmx.rate.RateConfig;
import com.github.sbridges.beacon.test.util.StubClock;

import jdk.jfr.consumer.RecordedEvent;

public class RateTest {

    
    static List<RecordedEvent> events; 
    
    StubClock clock = new StubClock(); 
    long now = clock.millis();
    

    
    @Test
    public void testEmpty() {
        
        Rate rate = new Rate(new RateConfig(false, Duration.of(1, ChronoUnit.SECONDS)), clock);
        rate.flush();
        assertEquals(0, rate.getRatePerSecond(), 0);
        assertEquals(0, rate.getTotal(), 0);
    }
    
    @Test
    public void testNonSum() {
        
        Rate rate = new Rate(new RateConfig(false, Duration.of(1, ChronoUnit.SECONDS)), clock);
        
        //last value is 1
        //we don'thave enough info to calculate rate though on the 
        //first flush, so rate remains 0
        rate.hear(1);
        rate.flush();
        
        assertEquals(0, rate.getRatePerSecond(), 0);
        assertEquals(1, rate.getTotal(), 0);

        
        //last value is now 2, delta is 1
        rate.hear(2);
        clock.advanceMillis(1000);
        rate.flush();
        
        assertEquals(1, rate.getRatePerSecond(), 0);
        assertEquals(2, rate.getTotal(), 0);
        
        //wait a while, no more events
        clock.advanceMillis(1000);
        rate.flush();
        assertEquals(0, rate.getRatePerSecond(), 0);
        assertEquals(2, rate.getTotal(), 0);
    }
    
    
    @Test
    public void testFlushBeforeTime() {
        
        Rate rate = new Rate(new RateConfig(false, Duration.of(1, ChronoUnit.SECONDS)), clock);
        
        //last value is 1
        //we don'thave enough info to calculate rate though on the 
        //first flush, so rate remains 0
        rate.hear(1);
        rate.flush();
        
        assertEquals(0, rate.getRatePerSecond(), 0);
        assertEquals(1, rate.getTotal(), 0);

        
        rate.hear(2);
        clock.advanceMillis(500);
        rate.flush();

        //time hasn't advanced enough, use our initial value
        assertEquals(0, rate.getRatePerSecond(), 0);
        assertEquals(1, rate.getTotal(), 0);
        
        
        clock.advanceMillis(500);
        
        //now we have new values
        rate.flush();
        assertEquals(1, rate.getRatePerSecond(), 0);
        assertEquals(2, rate.getTotal(), 0);
    }
    
    
    @Test
    public void testSum() {
        
        Rate rate = new Rate(new RateConfig(true, Duration.of(1, ChronoUnit.SECONDS)), clock);

        //last value is 1
        //we don'thave enough info to calculate rate though on the 
        //first flush, so rate remains 0
        rate.hear(1);
        rate.flush();
        
        assertEquals(0, rate.getRatePerSecond(), 0);
        assertEquals(1, rate.getTotal(), 0);

        
        //last value is now 6, delta is 5
        rate.hear(2);
        rate.hear(3);
        clock.advanceMillis(1000);
        rate.flush();
        
        assertEquals(5, rate.getRatePerSecond(), 0);
        assertEquals(6, rate.getTotal(), 0);
        
        //wait a while, no more events
        clock.advanceMillis(1000);
        rate.flush();
        assertEquals(0, rate.getRatePerSecond(), 0);
        assertEquals(6, rate.getTotal(), 0);
    }
    
    
        
    

}
