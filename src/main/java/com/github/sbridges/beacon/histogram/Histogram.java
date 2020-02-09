package com.github.sbridges.beacon.histogram;

import java.time.Clock;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.HdrHistogram.DoubleHistogram;

import com.github.sbridges.beacon.RecordedEventListener;
import com.github.sbridges.beacon.internal.FirstException;

import jdk.jfr.consumer.RecordedEvent;

/**
 * Implementation of {@link HistogramMXBean} 
 */
public final class Histogram implements HistogramMXBean, RecordedEventListener {

    private static class Stats {
        private long events;
        private double p50;
        private double p90;
        private double p95;
        private double p99;
        private double p999;
        private double mean;
        private double max;
    }
    
    private static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
    
    private final Clock clock;
    private final DoubleHistogram allTimeHistogram;
    private final DoubleHistogram lastMinuteHistogram;
    private long lastFlushTime;
    private final String eventField;   
    private final FirstException firstException = new FirstException();
    
    volatile Stats allTimeStats = new Stats();
    volatile Stats lastMinuteStats = new Stats();
    
    
    public Histogram(HistogramConfig config, Clock clock) {
        this.eventField = config.getEventField();
        this.clock = clock;
        this.allTimeHistogram = new DoubleHistogram(3);
        this.lastMinuteHistogram = new DoubleHistogram(3);
    }
    
    @Override
    public void hear(RecordedEvent e) {
        try {
            double value = e.getDouble(eventField);
            allTimeHistogram.recordValue(value);
            lastMinuteHistogram.recordValue(value);
        } catch(RuntimeException re) {
            firstException.hear(re);
        }
    }

    @Override
    public void flush() {
        
        long now = clock.millis();
        if(now >= lastFlushTime + ONE_MINUTE) {
            Stats lastMinuteStats = this.lastMinuteStats;
            lastMinuteStats.p50 = lastMinuteHistogram.getValueAtPercentile(50);
            lastMinuteStats.p90 = lastMinuteHistogram.getValueAtPercentile(90);
            lastMinuteStats.p95 = lastMinuteHistogram.getValueAtPercentile(95);
            lastMinuteStats.p99 = lastMinuteHistogram.getValueAtPercentile(99);
            lastMinuteStats.p999 = lastMinuteHistogram.getValueAtPercentile(99.9);
            lastMinuteStats.max = lastMinuteHistogram.getMaxValue();
            lastMinuteStats.mean = lastMinuteHistogram.getMean();
            lastMinuteStats.events = lastMinuteHistogram.getTotalCount();
            this.lastMinuteStats = lastMinuteStats;
            
            lastMinuteHistogram.reset();
            //write to establish a happens before for readers of the stats
            lastFlushTime = now;
        }
        
        Stats allTimStats = allTimeStats;
        allTimeStats.p50 = allTimeHistogram.getValueAtPercentile(50);
        allTimeStats.p90 = allTimeHistogram.getValueAtPercentile(90);
        allTimeStats.p95 = allTimeHistogram.getValueAtPercentile(95);
        allTimeStats.p99 = allTimeHistogram.getValueAtPercentile(99);
        allTimeStats.p999 = allTimeHistogram.getValueAtPercentile(99.9);
        allTimeStats.max = allTimeHistogram.getMaxValue();
        allTimeStats.mean = allTimeHistogram.getMean();
        allTimeStats.events = allTimeHistogram.getTotalCount();
        //write to establish a happens before for readers of the stats
        this.allTimeStats = allTimStats;
    }
    
    

    @Override
    public String[] getFirstException() {
        return firstException.get();
    }
    
    @Override
    public double getAllTimeP50() {
        return allTimeStats.p50;
    }

    @Override
    public double getAllTimeP90() {
        return allTimeStats.p90;
    }

    @Override
    public double getAllTimeP95() {
        return allTimeStats.p95;
    }

    @Override
    public double getAllTimeP99() {
        return allTimeStats.p99;
    }

    @Override
    public double getAllTimeP999() {
        return allTimeStats.p999;
    }

    @Override
    public double getAllTimeMax() {
        return allTimeStats.max;
    }

    @Override
    public double getAllTimeMean() {
        return allTimeStats.mean;
    }

    @Override
    public long getAllTimeEvents() {
        return allTimeStats.events;
    }
    
    @Override
    public double getLastMinuteP50() {
        return lastMinuteStats.p50;
    }

    @Override
    public double getLastMinuteP90() {
        return lastMinuteStats.p90;
    }

    @Override
    public double getLastMinuteP95() {
        return lastMinuteStats.p95;
    }

    @Override
    public double getLastMinuteP99() {
        return lastMinuteStats.p99;
    }

    @Override
    public double getLastMinuteP999() {
        return lastMinuteStats.p999;
    }


    @Override
    public double getLastMinuteMax() {
        return lastMinuteStats.max;
    }

    @Override
    public double getLastMinuteMean() {
        return lastMinuteStats.mean;
    }
    
    @Override
    public long getLastMinuteEvents() {
        return lastMinuteStats.events;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventField);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Histogram other = (Histogram) obj;
        return Objects.equals(eventField, other.eventField);
    }

    @Override
    public String toString() {
        return "Histogram [eventField=" + eventField + "]";
    }

    public String getEventField() {
        return eventField;
    }
    
    
}
