package com.github.sbridges.beacon.jmx.topsum;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.sbridges.beacon.internal.FirstException;
import com.github.sbridges.beacon.internal.TopIntermediateResults;
import com.github.sbridges.beacon.jmx.top.TopConfig;
import com.github.sbridges.beacon.listeners.KeyValueDurationListener;
import jdk.jfr.consumer.RecordedEvent;

/**
 * implementation of {@link TopSumMXBean}
 */
public final class TopSum implements KeyValueDurationListener, TopSumMXBean{

    private final Clock clock;
    private final FirstException firstException = new FirstException();
    private final TopConfig conf;
    private volatile TopSumStatMBean[] lastResults = new TopSumStatMBean[0];
    private final Map<String, TopIntermediateResults> collecting = new HashMap<>();
    public long lastCompletedTime;
    private Function<RecordedEvent, String> keyExtractor;
    
    public TopSum(TopConfig conf, Clock clock) {
        this.clock = clock;
        this.conf = conf;
    }
    
    @Override
    public TopSumStatMBean[] getStats() {
        return lastResults;
    }
    
    @Override
    public void hear(String key, double value, Duration duration) {
        try {
            TopIntermediateResults intermediate = collecting.computeIfAbsent(
                    key, 
                    __ -> new TopIntermediateResults());
            intermediate.add(value, duration.toNanos());
        } catch(Exception ex) {
            firstException.hear(ex);
        }
    }



    @Override
    public void flush() {
        try {
            long now = clock.millis();
            if(lastCompletedTime + conf.getPeriod().toMillis() <= now) {
                TopSumStatMBean[] results = 
                        collecting.entrySet().stream()
                        .map(t -> t.getValue().complete(t.getKey()))
                        .toArray(TopSumStatMBean[]::new);
                collecting.clear();
                Arrays.sort(results);
                lastResults = results;
                lastCompletedTime = now;
            }
        } catch(Exception e) {
            firstException.hear(e);
        }
    }


    @Override
    public String[] getFirstException() {
        return firstException.get();
    }

    @Override
    public String[] getStatsReport() {

        TopSumStatMBean[] reportOn = lastResults;
        int length = Arrays.stream(reportOn).mapToInt(t -> t.getKey().length()).max().orElse(20);
        length = Math.max(length, 20);
        length = Math.min(length, 512);
        
        List<String> results = new ArrayList<>();

        results.add(
                String.format("%-" + length + "s %-20s %-20s %-20s", "key", "value", "invocations", "duration (ms)"));

        results.add("-".repeat(length) + " " + ("-------------------- ".repeat(3)));
        for(TopSumStatMBean bean : reportOn) {
            results.add(
                   String.format("%-" + length + "s %20.0f %20d %20d", 
                           bean.getKey(), 
                           bean.getValue(), 
                           bean.events(),
                           TimeUnit.NANOSECONDS.toMillis(bean.getTotalDurationNanos()))
                    );
        }
        if(results.size() == 2) {
            return null;
        }
        return results.toArray(new String[0]);
        
    }

    //VisibleForTesting
    public TopConfig getConfig() {
        return conf;
    }

    @Override
    public void hearException(Exception e) {
        firstException.hear(e);
    }

}
