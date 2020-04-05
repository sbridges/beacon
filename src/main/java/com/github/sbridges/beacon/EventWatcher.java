package com.github.sbridges.beacon;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.management.MBeanServer;

import jdk.jfr.EventSettings;
import jdk.jfr.FlightRecorder;
import jdk.jfr.consumer.RecordingStream;

/**
 * Records jfr events and exposes them over jmx. 
 */
public final class EventWatcher {

    private final List<Bean> coolBeans;
    private volatile boolean running = true;
    private final CountDownLatch started = new CountDownLatch(1);
    private long events;
    private long flushes;
    
    static {
        
        //see comments later
        //we are simulating a flush
        //in testing we see that the recording stream sometimes stops
        //giving us flush events, so simulate this with an event hopefully once a chunk
        FlightRecorder.register(FlushEvent.class);
        FlushEvent fe = new FlushEvent();
        new ScheduledThreadPoolExecutor(1, r -> {
            Thread answer = new Thread(r);
            answer.setName(EventWatcher.class.getName() + ".flush");
            answer.setDaemon(true);
            return answer;
        }).scheduleWithFixedDelay(() -> {
            fe.begin();
            fe.commit();
        }, 1, 1, TimeUnit.SECONDS);
        
        
    }
    
    public EventWatcher(List<Bean> coolBeans) {
        this.coolBeans = coolBeans; 
    }

    public void start() {
        Thread thread = new Thread(() -> run(), Beacon.class.getName() + ".eventWatcher");
        thread.setDaemon(true);
        thread.start();
        try {
            if(!started.await(1, TimeUnit.MINUTES)) {
                throw new IllegalStateException("not started!");
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public void stop() {
        synchronized(this) {
            if(!running) {
                return;
            }
            running = false;
        }
        for(Bean b : coolBeans) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                mbs.unregisterMBean(
                        b.getObjectName());
            } catch(Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private void run() {

        try(var rs = new RecordingStream()) {
            rs.setMaxAge(Duration.of(15, ChronoUnit.MINUTES));
            List<String> events = coolBeans.stream()
                    .map(t -> t.getEventName())
                    .distinct()
                    .collect(Collectors.toList());
            
            for(String event : events) {
                EventSettings settings = rs.enable(event);
                
                //use the lowest period
                coolBeans.stream()
                    .map(t -> t.getEventPeriod())
                    .flatMap(t -> t.stream())
                    .sorted()
                    .findFirst()
                    .ifPresent(t -> settings.withPeriod(t));
                    
                //use the lowest duration
                coolBeans.stream()
                    .map(t -> t.getEventThreshold())
                    .flatMap(t -> t.stream())
                    .sorted()
                    .findFirst()
                    .ifPresent(t -> settings.withThreshold(t));
                coolBeans.stream()
                    .map(t -> t. getStackTrace())
                    .filter(t -> t)
                    .map(__ -> settings.withStackTrace());
            }
            
            for(Bean b : coolBeans) {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                try {
                    mbs.registerMBean(
                            b.getListener(), 
                            b.getObjectName());
                } catch(Exception e) {
                    throw new IllegalStateException(e);
                }
                rs.onEvent(b.getEventName(), e -> {
                    try {
                        b.getListener().hear(e);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                    });
            }
            
            rs.onEvent(__ -> {
                this.events++;
            });

            rs.onError(t -> {
                if(running) {
                    System.err.println(t);
                }
            });
            
            //originally tried to use flush, but running on some larger
            //programs we would sometimes stop seeing flush events
            //TODO - enable this and remove our own flush event
            //rs.onFlush(() -> {
            //    flush(rs);
            //});
            
            
            rs.enable(FlushEvent.class);
            rs.onEvent(FlushEvent.NAME, __ -> {
                flush(rs);
            });
            
            //still somewhat racy, but as close
            //as we can get without counting down in the event thread
            started.countDown();
            rs.start();
        }
        
    }

    private void flush(RecordingStream rs) {
        this.flushes++;
        try {
            for(Bean b : coolBeans) {
                b.getListener().flush();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(!running) {
            rs.close();
        }
    }
    
    public long getEvents() {
        return events;
    }

    public long getFlushes() {
        return flushes;
    }


    
}
