package com.github.sbridges.beacon;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.management.MBeanServer;

import jdk.jfr.EventSettings;
import jdk.jfr.consumer.RecordingStream;

/**
 * Records jfr events and exposes them over jmx. 
 */
public final class EventWatcher {

    private final List<Bean> coolBeans;
    private volatile boolean running = true;
    private final CountDownLatch started = new CountDownLatch(1);
    
    
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
            rs.setMaxSize(1024 * 1024);;
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
                rs.onEvent(b.getEventName(), e -> b.getListener().hear(e));
            }
            
            rs.onError(t -> {
                if(running) {
                    System.err.println(t);
                }
            });
            
            rs.onFlush(() -> {
                for(Bean b : coolBeans) {
                    b.getListener().flush();
                }
                if(!running) {
                    rs.close();
                }
            });
            
            //still somewhat racy, but as close
            //as we can get without counting down in the event thread
            started.countDown();
            rs.start();
        }
        
    }
    
}
