package com.github.sbridges.beacon;

import java.io.Reader;
import java.io.StringReader;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.management.ObjectName;

import org.yaml.snakeyaml.Yaml;

import com.github.sbridges.beacon.histogram.Histogram;
import com.github.sbridges.beacon.histogram.HistogramConfig;
import com.github.sbridges.beacon.jmx.gauge.Gauge;
import com.github.sbridges.beacon.jmx.gauge.GaugeConfig;
import com.github.sbridges.beacon.jmx.inspector.Inspector;
import com.github.sbridges.beacon.jmx.inspector.InspectorConfig;
import com.github.sbridges.beacon.jmx.rate.Rate;
import com.github.sbridges.beacon.jmx.rate.RateConfig;
import com.github.sbridges.beacon.jmx.top.Top;
import com.github.sbridges.beacon.jmx.top.TopConfig;
import com.github.sbridges.beacon.jmx.topsum.TopSum;

/**
 * Parses a yaml config file to generate a list of beans 
 */
final class ConfigParser {

    List<Bean> parse(String conf) {
        Reader reader = new StringReader(conf);
        return parseInternal(new Yaml().load(reader));
    }

    private List<Bean> parseInternal(Object conf) {
        if(!(conf instanceof Map)) {
            throw new IllegalArgumentException(
                    "conf should be a map not:" + conf);
        }
        Map confMap = (Map) conf;
        assertKeys(confMap, confMap, "events");
        List events = getFromMap(confMap, List.class, "events", conf);
        
        List<Bean> coolBeans = new ArrayList<>();
        for(Object e : events) {
            if(!(e instanceof Map)) {
                throw new IllegalArgumentException(
                        "not a map:" + e + " in:" + conf);
            }
            Map event = (Map) e;
            assertKeys(event, conf, "eventName", "eventPeriod", "eventThreshold", "objects", "stackTrace");
            String eventName = getFromMap(event, String.class, "eventName", conf);

            Optional<Duration> eventPeriod;
            if(event.containsKey("eventPeriod")) {
                eventPeriod = Optional.of(getDurationFromMap(event, "eventPeriod", conf));
            } else {
                eventPeriod = Optional.empty();
            }
            
            Optional<Duration> eventThreshold;
            if(event.containsKey("eventThreshold")) {
                eventThreshold = Optional.of(getDurationFromMap(event, "eventThreshold", conf));
            } else {
                eventThreshold = Optional.empty();
            }
            
            boolean stackTrace = false;
            if(event.containsKey("stackTrace")) {
                stackTrace = getFromMap(event, Boolean.class, "stackTrace", conf);
            }
            
            List objects = getFromMap(event, List.class, "objects", conf);
            for(Object o : objects) {
                if(!(o instanceof Map)) {
                    throw new IllegalStateException("expecting a map, not:" + o + " in conf:" + conf);
                }
                Map object = (Map) o;
                String objectName = getFromMap(object, String.class, "objectName", conf);
                String objectType = getFromMap(object, String.class, "objectType", conf);
                
                
                switch(objectType) {
                    case "gauge" :
                        assertKeys(object, conf, "objectName", "objectType", "gaugeConfig");
                        Map gaugeConfigMap = getFromMap(object, Map.class, "gaugeConfig", conf);
                        GaugeConfig gaugeConfig = parseGaugeConfig(gaugeConfigMap, conf);
                        coolBeans.add(new Bean(
                                objectName, 
                                objectType, 
                                eventName, 
                                eventPeriod,
                                eventThreshold,
                                stackTrace,
                                new Gauge(gaugeConfig)));
                        break;
                    case "inspector" :
                        Map inspectorConfigMap = getFromMap(object, Map.class, "inspectorConfig", conf);
                        InspectorConfig inspectorConfig = parseInspectorConfig(inspectorConfigMap, conf);
                        assertKeys(object, conf, "objectName", "objectType", "inspectorConfig");
                        coolBeans.add(new Bean(
                                objectName, 
                                objectType, 
                                eventName, 
                                eventPeriod,
                                eventThreshold,
                                stackTrace,
                                new Inspector(inspectorConfig)));
                        break;
                    case "rate" :
                        assertKeys(object, conf, "objectName", "objectType", "rateConfig");
                        Map rateConfigMap = getFromMap(object, Map.class, "rateConfig", conf);
                        RateConfig rateConfig = parseRateConfig(rateConfigMap, conf);
                        coolBeans.add(new Bean(
                                objectName, 
                                objectType, 
                                eventName, 
                                eventPeriod,
                                eventThreshold,
                                stackTrace,
                                new Rate(rateConfig, Clock.systemDefaultZone())));
                        break;
                    case "histogram" :
                        assertKeys(object, conf, "objectName", "objectType", "histogramConfig");
                        Map histogramConfigMap = getFromMap(object, Map.class, "histogramConfig", conf);
                        HistogramConfig histogramConfig = parseHistogramConfig(histogramConfigMap, conf);
                   
                        coolBeans.add(new Bean(
                                objectName, 
                                objectType, 
                                eventName, 
                                eventPeriod,
                                eventThreshold,
                                stackTrace,
                                new Histogram(histogramConfig, Clock.systemDefaultZone())));
                    
                        break;
                    case "top" :
                    case "topSum" :
                        assertKeys(object, conf, "objectName", "objectType", "topConfig");
                        Map topConfigMap = getFromMap(object, Map.class, "topConfig", conf);
                        TopConfig topConfig = parseTopConfig(topConfigMap, conf);
                        if(objectType.equals("topSum")) {
                            coolBeans.add(new Bean(
                                    objectName, 
                                    objectType, 
                                    eventName, 
                                    eventPeriod,
                                    eventThreshold,
                                    stackTrace,
                                    new TopSum(topConfig, Clock.systemDefaultZone())));
                        } else {
                            coolBeans.add(new Bean(
                                    objectName, 
                                    objectType, 
                                    eventName, 
                                    eventPeriod,
                                    eventThreshold,
                                    stackTrace,
                                    new Top(topConfig, Clock.systemDefaultZone())));
                        }
                        break;
                    default : 
                        throw new IllegalStateException("unrecognized objectType:" + objectType + " in conf:" + conf);
                }
           
            }
            
            Set<ObjectName> objectNames = new HashSet<>();
            for(Bean b : coolBeans) {
                if(!objectNames.add(b.getObjectName())) {
                    throw new IllegalStateException("duplicate object name:" + b.getObjectName());
                }
            }
        }
        return coolBeans;
    }


    private static Duration getDurationFromMap(Map bean, String key, Object conf) {
        Map val = getFromMap(bean, Map.class, key, conf);
        if(val.size() != 1) {
            throw new IllegalArgumentException("expecting one key in:" + val + " for conf:" + conf);
        }
        String unit = val.keySet().iterator().next().toString();
        long value = getFromMap(val, Number.class, unit, conf).longValue();
        
        TemporalUnit tempUnit = null;
        for(ChronoUnit t : ChronoUnit.values()) {
            if(t.name().equalsIgnoreCase(unit)) {
                tempUnit = t;
            }
        }
        if(tempUnit == null) {
            throw new IllegalArgumentException("invalid temporal unit:" + unit + " in conf:" + conf);
        }
        return Duration.of(value, tempUnit);
    }

    
    private void assertKeys(Map map, Object conf, String...allowedKeys) {
        Set<String> allowedKeySet = Set.of(allowedKeys);
        Set actualKeys = new HashSet(map.keySet());
        actualKeys.removeAll(allowedKeySet);
        if(actualKeys.isEmpty()) {
            return;
        }
        throw new IllegalStateException(
                "unrecognized keys:" + actualKeys + " in:" + map + 
                " for conf:" + conf
                );
    }
    
    private TopConfig parseTopConfig(Map topConfig, Object conf) {
        assertKeys(topConfig, conf, "keyFields", "valueField", "period");
        List keyFields = getFromMap(topConfig, List.class, "keyFields", conf);
        if(keyFields.isEmpty()) {
            throw new IllegalStateException("expecting a key for:" + topConfig  + " in:" + conf);
        }
        String valueFields = getFromMap(topConfig, String.class, "valueField", conf);
        keyFields.stream()
            .filter(t -> !(t instanceof String))
            .findFirst()
            .ifPresent(t -> {
                throw new IllegalArgumentException("expected a string, not:" + t + " in conf:" + conf);
            });
        Duration period = Duration.of(5, ChronoUnit.SECONDS);
        if(topConfig.containsKey("period")) {
            period = getDurationFromMap(topConfig, "period", conf);
        }
        
        return new TopConfig(keyFields, valueFields, period);
    } 
    
    private GaugeConfig parseGaugeConfig(Map gaugeConfig, Object conf) {
        assertKeys(gaugeConfig, conf, "eventField");
        String eventField = getFromMap(gaugeConfig, String.class, "eventField", conf);
        return new GaugeConfig(eventField);
    }
    
    
    private HistogramConfig parseHistogramConfig(Map histogramConfigMap,
            Object conf) {
        assertKeys(histogramConfigMap, conf, "eventField");
        String eventField = getFromMap(histogramConfigMap, String.class, "eventField", conf);
        return new HistogramConfig(eventField);
    }
    
    private InspectorConfig parseInspectorConfig(Map inspectorConfig, Object conf) {
        assertKeys(inspectorConfig, conf, "size");
        int size = 1;
        if(inspectorConfig.containsKey("size")) {
            size = getFromMap(inspectorConfig, Number.class, "size", conf).intValue();
        }
        return new InspectorConfig(size);
    }

    private RateConfig parseRateConfig(Map rateConfig, Object conf) {
        assertKeys(rateConfig, conf, "sum", "period", "valueField");
        String valueField = getFromMap(rateConfig, String.class, "valueField", conf);
        boolean sum = false;
        if(rateConfig.containsKey("sum")) {
            sum = getFromMap(rateConfig, Boolean.class, "sum", conf);
        }
        Duration eventPeriod = Duration.of(5, ChronoUnit.SECONDS);
        if(rateConfig.containsKey("period")) {
            eventPeriod = getDurationFromMap(rateConfig, "period", conf);
        }
        return new RateConfig(valueField, sum, eventPeriod);
    }
    
    private static <T> T getFromMap(
            Map<?, ?> map, 
            Class<T> type, 
            String key, 
            Object conf) {
        if(!map.containsKey(key)) {
            throw new IllegalStateException(
                    "expecting key:" + key + " in:" + map + " for conf:" + conf);
        }
        Object val = map.get(key);
        if(!type.isAssignableFrom(val.getClass())) {
            throw new IllegalStateException(
                    "expecting type:" + type + " not:" + val.getClass() + " for val:" + val + " and for conf:" + conf);
        }
        return (T) val;
    }
    


}
