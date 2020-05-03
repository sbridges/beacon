package com.github.sbridges.beacon;

import com.github.sbridges.beacon.internal.test.event.TestEvent;
import com.github.sbridges.beacon.internal.test.event.TestEventFactory;
import jdk.jfr.consumer.RecordedEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

public class KeyExtractorUtilTest {


    static RecordedEvent event;
    @BeforeClass
    public static void createEvents() {
        event = TestEventFactory.createRecordedEvents(
                t -> {
                    t.aLong = 1;
                    t.aString = "a";
                    t.aByte = 2;
                    t.aDouble = 3;
                    t.aThread = Thread.currentThread();
                    t.aClass = KeyExtractorUtil.class;
                }
        ).get(0);
    }


    @Test
    public void testGetKey() {
        Function<RecordedEvent, String> extractor = KeyExtractorUtil.makeKeyExtractor(
                event,
                Arrays.asList("aLong", "aString", "aByte", "aDouble", "aClass", "aThread")
        );
        String extracted = extractor.apply(event);

        assertEquals("1 a 2 3.0 com.github.sbridges.beacon.KeyExtractorUtil "
                + Thread.currentThread().getName() + "(" + Thread.currentThread().getId() + ")",
                extracted);
    }
}