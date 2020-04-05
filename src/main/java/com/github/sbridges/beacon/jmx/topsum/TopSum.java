package com.github.sbridges.beacon.jmx.topsum;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.sbridges.beacon.RecordedEventListener;
import com.github.sbridges.beacon.internal.FirstException;
import com.github.sbridges.beacon.internal.RecordedEventsUtil;
import com.github.sbridges.beacon.internal.TopIntermediateResults;
import com.github.sbridges.beacon.jmx.top.TopConfig;
import jdk.jfr.consumer.RecordedEvent;

/**
 * implementation of {@link TopSumMXBean}
 */
public final class TopSum implements RecordedEventListener, TopSumMXBean{

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
    public void hear(RecordedEvent e) {
        try {
            if(keyExtractor == null) {
                keyExtractor = RecordedEventsUtil.makeKeyExtractor(e, conf.getKeyFields());
            }
            String key = keyExtractor.apply(e);
            double value = e.getDouble(conf.getValueField());
            
            
            TopIntermediateResults intermediate = collecting.computeIfAbsent(
                    key, 
                    __ -> new TopIntermediateResults());
       
            long duration = e.getDuration().toMillis();
            intermediate.add(value, duration); 
            
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

        String key = conf.getKeyFields().stream().collect(Collectors.joining(" "));
        results.add(
                String.format("%-" + length + "s %-20s %-20s %-20s", key, conf.getValueField(), "invocations", "duration (ms)"));

        results.add("-".repeat(length) + " " + ("-------------------- ".repeat(3)));
        for(TopSumStatMBean bean : reportOn) {
            results.add(
                   String.format("%-" + length + "s %20.0f %20d %20d", 
                           bean.getKey(), 
                           bean.getValue(), 
                           bean.events(), 
                           bean.getTotalDuration())
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

}
