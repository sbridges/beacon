package com.github.sbridges.beacon.internal;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.github.sbridges.beacon.internal.test.event.TestEventFactory;

import jdk.jfr.consumer.RecordedEvent;

public class RecordedEventsTest {

    @Test
    public void test() {
        RecordedEvent event = TestEventFactory.createRecordedEvent(e -> {
            e.aByte = 1;
            e.aDouble = 5.0;
            e.aClass = RecordedEventsUtil.class;
            e.aThread = Thread.currentThread();
        });
        
        
        var keyExtractor = RecordedEventsUtil.makeKeyExtractor(event, 
                Arrays.asList("aByte", "aDouble", "aClass", "aThread"));
        
        String key = keyExtractor.apply(event);
        assertEquals("1 5.0 com.github.sbridges.beacon.internal.RecordedEventsUtil " + Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")",
                key);
    }

}
