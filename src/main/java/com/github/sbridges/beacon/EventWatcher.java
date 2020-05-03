package com.github.sbridges.beacon;

import com.github.sbridges.beacon.internal.Util;
import jdk.jfr.EventSettings;
import jdk.jfr.FlightRecorder;
import jdk.jfr.consumer.RecordingStream;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Records jfr events and exposes them over jmx. 
 */
public final class EventWatcher {

    private static final Logger log = Logger.getLogger(EventWatcher.class.getName());

    private final List<Bean> coolBeans;
    private final List<EventConfig> events;

    private volatile boolean running = true;
    private final CountDownLatch started = new CountDownLatch(1);
    private long recordedEvents;
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
    
    public EventWatcher(List<Bean> coolBeans, List<EventConfig> events) {
        this.events = Util.immutableCopyOf(events);
        this.coolBeans = Util.immutableCopyOf(coolBeans);
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

            for(EventConfig conf : events) {
                EventSettings settings = rs.enable(conf.getEventName());
                conf.getEventPeriod().ifPresent(t -> settings.withPeriod(t));
                conf.getEventThreshold().ifPresent(t -> settings.withThreshold(t));
                if(conf.getStackTrace()) {
                    settings.withStackTrace();
                } else {
                    settings.withoutStackTrace();
                }
            }

            for(Bean b : coolBeans) {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                try {
                    mbs.registerMBean(
                            b.getMxBean(),
                            b.getObjectName());
                } catch(Exception e) {
                    throw new IllegalStateException(e);
                }

                b.getListeners().forEach((name, consumer) -> {
                    rs.onEvent(name, t -> {
                                try {
                                    consumer.accept(t);
                                } catch (Exception e) {
                                    log.log(Level.WARNING, e.getMessage(), e);
                                }
                            }
                    );
                });
            }
            
            rs.onEvent(__ -> {
                this.recordedEvents++;
            });

            rs.onError(t -> {
                if(running) {
                    log.log(Level.WARNING, t.getMessage(), t);
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
                b.getMxBean().flush();
            }
        } catch(Exception e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }
        if(!running) {
            rs.close();
        }
    }
    
    public long getRecordedEvents() {
        return recordedEvents;
    }

    public long getFlushes() {
        return flushes;
    }


    
}
