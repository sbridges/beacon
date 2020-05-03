package com.github.sbridges.beacon;

import java.io.Reader;
import java.io.StringReader;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;

import javax.management.ObjectName;

import org.yaml.snakeyaml.Yaml;

import com.github.sbridges.beacon.jmx.histogram.Histogram;
import com.github.sbridges.beacon.jmx.gauge.Gauge;
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

    public static final Clock CLOCK = Clock.systemUTC();

    Config parse(String conf) {
        Reader reader = new StringReader(conf);
        return parseInternal(new Yaml().load(reader));
    }

    private Config parseInternal(Object conf) {
        if(!(conf instanceof Map)) {
            throw new IllegalArgumentException(
                    "conf should be a map not:" + conf);
        }
        Map confMap = (Map) conf;
        assertKeys(confMap, confMap, "events", "objects");

        List<EventConfig> events = parseEvents(conf);
        List<Bean> coolBeans = parseBeans(conf);

        Set<ObjectName> objectNames = new HashSet<>();
        for(Bean b : coolBeans) {
            if(!objectNames.add(b.getObjectName())) {
                throw new IllegalStateException("duplicate object name:" + b.getObjectName());
            }
        }

        return new Config(coolBeans, events);
    }

    private List<Bean> parseBeans(Object conf) {
        List<Bean> coolBeans = new ArrayList<>();
        List objects = getFromMap((Map) conf, List.class, "objects", conf);
        for(Object o : objects) {
            if(!(o instanceof Map)) {
                throw new IllegalStateException("expecting a map, not:" + o + " in conf:" + conf);
            }
            Map object = (Map) o;
            String objectName = getFromMap(object, String.class, "objectName", conf);
            String objectType = getFromMap(object, String.class, "objectType", conf);


            switch(objectType) {
                case "gauge" :
                    parseGauge(objectName, coolBeans, object, conf);
                    break;
                case "inspector" :
                    parseInspector(objectName, coolBeans, object, conf);
                    break;
                case "rate" :
                    parseRate(objectName, coolBeans, object, conf);
                    break;
                case "histogram" :
                    parseHistogram(objectName, coolBeans, object, conf);
                    break;
                case "top" :
                case "topSum" :
                    parseTopOrTopSum(coolBeans, object, objectName, objectType, conf);
                    break;
                default :
                    throw new IllegalStateException("unrecognized objectType:" + objectType + " in conf:" + conf);
            }

        }
        return coolBeans;
    }

    private void parseTopOrTopSum(List<Bean> coolBeans, Map object, String objectName, String objectType, Object conf) {
        assertKeys(object, conf, "objectName", "objectType", "topConfig", "keyValues");
        Map topConfigMap = new HashMap();
        if(object.get("topConfig") != null) {
            topConfigMap = getFromMap(object, Map.class, "topConfig", conf);
        }
        TopConfig topConfig = parseTopConfig(topConfigMap, conf);
        List<EventFieldKey> eventFieldKeys = parseKeyValues(object, conf);

        if(objectType.equals("topSum")) {
            coolBeans.add(
                    Bean.newKeyValueDurationBean(objectName,
                            new TopSum(topConfig, CLOCK),
                            eventFieldKeys));
        } else {
            coolBeans.add(
                    Bean.newKeyValueListenerBean(objectName,
                            new Top(topConfig, CLOCK),
                            eventFieldKeys));
        }
    }

    private void parseHistogram(String objectName, List<Bean> coolBeans, Map object, Object conf) {
        assertKeys(object, conf, "objectName", "objectType", "values");
        List<EventField> eventFields = parseValues(object, conf);
        coolBeans.add(
                Bean.newValueListenerBean(objectName,
                        new Histogram(CLOCK),
                        eventFields));
    }

    private void parseRate(String objectName, List<Bean> coolBeans, Map object, Object conf) {
        assertKeys(object, conf, "objectName", "objectType", "rateConfig", "values");
        Map rateConfigMap = new HashMap();
        if(object.get("rateConfig") != null) {
            rateConfigMap = getFromMap(object, Map.class, "rateConfig", conf);
        }

        RateConfig rateConfig = parseRateConfig(rateConfigMap, conf);
        List<EventField> eventFields = parseValues(object, conf);
        coolBeans.add(
                Bean.newValueListenerBean(objectName,
                        new Rate(rateConfig, CLOCK),
                        eventFields));
    }

    private void parseInspector(String objectName, List<Bean> coolBeans, Map object, Object conf) {
        Map inspectorConfigMap = new HashMap();
        if(object.get("inspectorConfig") != null) {
            inspectorConfigMap = getFromMap(object, Map.class, "inspectorConfig", conf);
        }
        InspectorConfig inspectorConfig = parseInspectorConfig(inspectorConfigMap, conf);
        assertKeys(object, conf, "objectName", "objectType", "inspectorConfig", "event");
        String eventName = getFromMap(object, String.class, "event", conf);

        coolBeans.add(
                Bean.newEventListenerBean(objectName,
                        new Inspector(inspectorConfig),
                        Arrays.asList(eventName))
        );
    }

    private void parseGauge(String objectName, List<Bean> coolBeans, Map object, Object conf) {
        assertKeys(object, conf, "objectName", "objectType", "values");
        List<EventField> eventFields = parseValues(object, conf);
        coolBeans.add(
                Bean.newValueListenerBean(objectName, new Gauge(), eventFields));
    }

    private List<EventConfig> parseEvents(Object conf) {
        List eventConf = getFromMap((Map) conf, List.class, "events", conf);
        List<EventConfig> answer = new ArrayList<>();
        for(Object e : eventConf) {
            if (!(e instanceof Map)) {
                throw new IllegalArgumentException(
                        "not a map:" + e + " in:" + conf);
            }
            Map event = (Map) e;
            assertKeys(event, conf, "eventName", "eventPeriod", "eventThreshold", "stackTrace");
            String eventName = getFromMap(event, String.class, "eventName", conf);

            Optional<Duration> eventPeriod;
            if (event.containsKey("eventPeriod")) {
                eventPeriod = Optional.of(getDurationFromMap(event, "eventPeriod", conf));
            } else {
                eventPeriod = Optional.empty();
            }

            Optional<Duration> eventThreshold;
            if (event.containsKey("eventThreshold")) {
                eventThreshold = Optional.of(getDurationFromMap(event, "eventThreshold", conf));
            } else {
                eventThreshold = Optional.empty();
            }

            boolean stackTrace = false;
            if (event.containsKey("stackTrace")) {
                stackTrace = getFromMap(event, Boolean.class, "stackTrace", conf);
            }

            answer.add(new EventConfig(
                    eventName,
                    eventThreshold,
                    eventPeriod,
                    stackTrace));
        }
        return answer;
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

    private List<EventFieldKey> parseKeyValues(Map parent, Object conf) {
        List keyValues = getFromMap(parent, List.class, "keyValues", conf);
        List<EventFieldKey> answer = new ArrayList<>();
        for(Object val : keyValues) {
            if (!(val instanceof Map)) {
                throw new IllegalArgumentException(
                        "not a map:" + val + " in:" + conf);
            }
            Map valMap = (Map) val;
            assertKeys(valMap, conf, "event", "keyFields", "valueField");
            String eventName = getFromMap(valMap, String.class, "event", conf);
            List<String> keys = getFromMap(valMap, List.class, "keyFields", conf);
            if(keys.isEmpty() || !keys.stream().allMatch(t -> t instanceof String)) {
                throw new IllegalArgumentException("expected a non empty map of strings, not:" + keys);
            }
            String field = getFromMap(valMap, String.class, "valueField", conf);
            answer.add(new EventFieldKey(eventName, field, keys));
        }
        return answer;
    }

    private TopConfig parseTopConfig(Map topConfig, Object conf) {
        assertKeys(topConfig, conf, "period");
        Duration period = Duration.of(5, ChronoUnit.SECONDS);
        if(topConfig.containsKey("period")) {
            period = getDurationFromMap(topConfig, "period", conf);
        }
        return new TopConfig(period);
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
        assertKeys(rateConfig, conf, "sum", "period");
        boolean sum = false;
        if(rateConfig.containsKey("sum")) {
            sum = getFromMap(rateConfig, Boolean.class, "sum", conf);
        }
        Duration eventPeriod = Duration.of(5, ChronoUnit.SECONDS);
        if(rateConfig.containsKey("period")) {
            eventPeriod = getDurationFromMap(rateConfig, "period", conf);
        }
        return new RateConfig(sum, eventPeriod);
    }

    private List<EventField> parseValues(Map parent, Object conf) {
        List vals = getFromMap(parent, List.class, "values", conf);
        List<EventField> answer = new ArrayList<>();
        for(Object val : vals) {
            if (!(val instanceof Map)) {
                throw new IllegalArgumentException(
                        "not a map:" + vals + " in:" + conf);
            }
            Map valMap = (Map) val;
            assertKeys(valMap, conf, "event", "field");
            String event =  getFromMap(valMap, String.class, "event", conf);
            String field =  getFromMap(valMap, String.class, "field", conf);
            answer.add(new EventField(event, field));
        }
        return answer;
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
        if(val == null) {
            return null;
        }
        if(!type.isAssignableFrom(val.getClass())) {
            throw new IllegalStateException(
                    "expecting type:" + type + " not:" + val.getClass() + " for val:" + val + " and for conf:" + conf);
        }
        return (T) val;
    }
    


}
