package com.github.sbridges.beacon.jmx.top;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.sbridges.beacon.listeners.KeyValueListener;
import com.github.sbridges.beacon.internal.FirstException;

import jdk.jfr.consumer.RecordedEvent;

class MutableDouble {
    public double value;
}

/**
 * Implementation of {@link TopMXBean}
 */
public final class Top implements TopMXBean, KeyValueListener {
    private final Clock clock;
    private final FirstException firstException = new FirstException();
    private final TopConfig conf;
    private volatile TopStatMBean[] lastResults = new TopStatMBean[0];
    private final Map<String, MutableDouble> collecting = new HashMap<>();
    public long lastCompletedTime;
    private Function<RecordedEvent, String> keyExtractor;
    
    public Top(TopConfig conf, Clock clock) {
        this.clock = clock;
        this.conf = conf;
    }
    
    @Override
    public TopStatMBean[] getStats() {
        return lastResults;
    }
    
    @Override
    public void hear(String key, double value) {
        try {
            MutableDouble intermediate = collecting.computeIfAbsent(
                    key, 
                    __ -> new MutableDouble());
            intermediate.value = value;
            
        } catch(Exception ex) {
            firstException.hear(ex);
        }
    }



    @Override
    public void flush() {
        try {
            long now = clock.millis();
            if(lastCompletedTime + conf.getPeriod().toMillis() <= now) {
                TopStatMBean[] results = 
                        collecting.entrySet().stream()
                        .map(t -> new TopStat(t.getKey(), t.getValue().value))
                        .toArray(TopStatMBean[]::new);
                collecting.clear();
                Arrays.sort(results);
                lastResults = results;
                lastCompletedTime = now;
            }
        } catch(Exception e) {
            firstException.hear(e);
        }
    }
    
    public TopConfig getConfig() {
        return conf;
    }


    @Override
    public String[] getFirstException() {
        return firstException.get();
    }

    @Override
    public String[] getStatsReport() {

        TopStatMBean[] reportOn = lastResults;
        int length = Arrays.stream(reportOn).mapToInt(t -> t.getKey().length()).max().orElse(20);
        length = Math.max(length, 20);
        length = Math.min(length, 512);
        
        List<String> results = new ArrayList<>();
        results.add(
                String.format("%-" + length + "s %-20s", "key", "value"));

        results.add("-".repeat(length) + " --------------------");
        for(TopStatMBean bean : reportOn) {
            results.add(
                   String.format("%-" + length + "s %20.0f", 
                           bean.getKey(), 
                           bean.getValue()
                           )
                    );
        }
        if(results.size() == 2) {
            return null;
        }
        return results.toArray(new String[0]);
        
    }

    @Override
    public int hashCode() {
        return Objects.hash(conf);
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
        Top other = (Top) obj;
        return Objects.equals(conf, other.conf);
    }

    @Override
    public String toString() {
        return "Top [conf=" + conf + "]";
    }

    @Override
    public void hearException(Exception e) {
        firstException.hear(e);
    }

}
