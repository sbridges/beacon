package com.github.sbridges.beacon;

import com.github.sbridges.beacon.internal.test.event.TestEvent;
import com.github.sbridges.beacon.internal.test.event.TestEventFactory;
import com.github.sbridges.beacon.listeners.DoubleValueListener;
import com.github.sbridges.beacon.listeners.KeyValueDurationListener;
import com.github.sbridges.beacon.listeners.KeyValueListener;
import com.github.sbridges.beacon.listeners.RecordedEventListener;
import jdk.jfr.consumer.RecordedEvent;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeanTest {
    static final String OBJECT_NAME = "com.github.sbridges.beacon:event=test,by=test";
    static final String EVENT_NAME = TestEvent.TEST_EVENT;
    static List<RecordedEvent> events;

    @BeforeClass
    public static void createEvents() {
        events = TestEventFactory.createRecordedEvents(
                t -> {t.aLong = 1; t.aString = "a";},
                t -> {t.aLong = 2; t.aString = "b";},
                t -> {t.aLong = 3; t.aString = "c";}
        );
    }

    @Test
    public void testValueListener() {
        DoubleValueListener listener = mock(DoubleValueListener.class);
        Bean b = Bean.newValueListenerBean(OBJECT_NAME, listener, Arrays.asList(
                new EventField(EVENT_NAME, "aLong")
        ));

        b.getListeners().get(EVENT_NAME).accept(events.get(0));
        verify(listener).hear(1);
        verifyNoMoreInteractions(listener);
    }


    @Test
    public void testEventListener() {
        RecordedEventListener listener = mock(RecordedEventListener.class);
        Bean b = Bean.newEventListenerBean(OBJECT_NAME, listener, Arrays.asList(EVENT_NAME));

        b.getListeners().get(EVENT_NAME).accept(events.get(0));
        verify(listener).hear(events.get(0));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testKVListener() {
        KeyValueListener listener = mock(KeyValueListener.class);
        Bean b = Bean.newKeyValueListenerBean(OBJECT_NAME, listener, Arrays.asList(
                new EventFieldKey(EVENT_NAME, "aLong", Arrays.asList("aLong", "aString"))
        ));


        b.getListeners().get(EVENT_NAME).accept(events.get(0));
        verify(listener).hear("1 a", 1);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testKVDurationListener() {
        KeyValueDurationListener listener = mock(KeyValueDurationListener.class);
        Bean b = Bean.newKeyValueDurationBean(OBJECT_NAME, listener, Arrays.asList(
                new EventFieldKey(EVENT_NAME, "aLong", Arrays.asList("aLong", "aString"))
        ));


        b.getListeners().get(EVENT_NAME).accept(events.get(0));
        verify(listener).hear("1 a", 1, Duration.of(0, ChronoUnit.NANOS));
        verifyNoMoreInteractions(listener);
    }
}