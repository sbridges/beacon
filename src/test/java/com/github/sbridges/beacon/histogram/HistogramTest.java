package com.github.sbridges.beacon.histogram;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.github.sbridges.beacon.internal.test.event.TestEventFactory;
import com.github.sbridges.beacon.test.util.StubClock;
import jdk.jfr.consumer.RecordedEvent;

public class HistogramTest {

    StubClock clock = new StubClock();
    
    List<RecordedEvent> events = TestEventFactory.createRecordedEvents(
            t -> t.aLong = 1,
            t -> t.aLong = 2,
            t -> t.aLong = 3
            
            );
    
    Histogram fixture = new Histogram(new HistogramConfig("aLong"), clock);
    
    @Test
    public void test() {
      fixture.flush();
      fixture.hear(events.get(0));
      fixture.hear(events.get(1));
      fixture.flush();
      
      assertEquals(fixture.getAllTimeMean(), 1.5, 0.001);
      assertEquals(fixture.getAllTimeMax(), 2.0, 0.001);
      assertEquals(fixture.getAllTimeP50(), 1.0, 0.001);
      assertEquals(fixture.getAllTimeEvents(), 2);
      
      assertEquals(fixture.getLastMinuteMax(), 0, 0.00);
      assertEquals(fixture.getLastMinuteEvents(), 0);
      clock.advanceSeconds(60);
      fixture.flush();
      
      assertEquals(fixture.getLastMinuteMean(), 1.5, 0.001);
      assertEquals(fixture.getLastMinuteMax(), 2.0, 0.001);
      assertEquals(fixture.getLastMinuteP50(), 1.0, 0.001);
      assertEquals(fixture.getLastMinuteEvents(), 2);
      
      
      fixture.hear(events.get(2));
      clock.advanceSeconds(60);
      fixture.flush();
      
      assertEquals(fixture.getLastMinuteMean(), 3, 0.001);
      assertEquals(fixture.getLastMinuteMax(), 3, 0.001);
      assertEquals(fixture.getLastMinuteP50(), 3, 0.001);
      assertEquals(fixture.getLastMinuteEvents(), 1);

  }

}
