package com.github.sbridges.beacon.internal.test.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jdk.jfr.FlightRecorder;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;

public class TestEventFactory {

    private static final Object lock = new Object();
    
    private static LinkedBlockingQueue<RecordedEvent> queue
        = new LinkedBlockingQueue<>();
    private static boolean setup = false;
    
    //set up a recorder to listen to our events
    private static void setup() {
        synchronized(lock) {
        
            if(setup) {
                return;
            }
            
            FlightRecorder.register(TestEvent.class);
            
            Thread t = new Thread(() -> {
                    try (var rs = new RecordingStream()) {
                        rs.enable(TestEvent.TEST_EVENT)
                        .withoutThreshold();
                        rs.onEvent(TestEvent.TEST_EVENT, e -> {
                            queue.add(e);
                        });
                        
                        rs.setOrdered(true);
                        rs.setMaxSize(1_000_000);
                        rs.start();
                    }
            });
            
            t.setName(TestEventFactory.class.getName() + ".recorder");
            t.setDaemon(true);
            t.start();

            long start = System.currentTimeMillis();
            try {
                while(!new TestEvent().isEnabled()) {
                    Thread.sleep(10);
                    if(System.currentTimeMillis() - start > 20_000) {
                        throw new IllegalStateException("not registered?");
                    }
                }
            } catch (InterruptedException e1) {
                throw new IllegalStateException(e1);
            }
            setup = true;
        }
    }
   
    /**
     * Create RecordedEvents from the given events
     */
    public static RecordedEvent createRecordedEvent(Consumer<TestEvent> eventConfigurator) {
        return createRecordedEvents(eventConfigurator).get(0);
    }


    /**
     * Create RecordedEvents from the given events
     */
    @SafeVarargs
    public static List<RecordedEvent> createRecordedEvents(Consumer<TestEvent>... eventConfigurator) {
        
        TestEvent[] events = Stream.of(eventConfigurator)
        .map(t -> {
            TestEvent te = new TestEvent();
            t.accept(te);
            return te;
        })
        .toArray(TestEvent[]::new);
        
        
        List<RecordedEvent> answer = new ArrayList<>();
        synchronized(lock) {
            setup();
            
            for(TestEvent e : events) {
                if(!e.isEnabled()) {
                    throw new IllegalStateException();
                }
                e.commit();
            }
            
            
            for(int i =0; i < events.length; i++) {
                RecordedEvent polled;
                try {
                    polled = queue.poll(30, TimeUnit.SECONDS);
                } catch (InterruptedException e1) {
                    throw new IllegalStateException(e1);
                }
                if(polled == null) {
                    throw new IllegalStateException("not found!");
                  
                }
                answer.add(polled);
            }
        }
        
        //make sure the order is the same as passed in, it should be
        for(int i = 0; i <  events.length; i++) {
            if(!events[i].id.equals(answer.get(i).getString("Id"))) {
                throw new IllegalStateException();
            }
        }
        
        return answer;
    }
}
