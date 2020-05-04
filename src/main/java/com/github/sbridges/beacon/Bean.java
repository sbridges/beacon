package com.github.sbridges.beacon;


import com.github.sbridges.beacon.listeners.*;
import jdk.jfr.consumer.RecordedEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * A Bean that exposes some JFR events over JMX. 
 */
public final class Bean {
    
    private final ObjectName objectName;
    private final Flushable mxBean;
    private final Map<String, Consumer<RecordedEvent>> listeners;

    public static Bean newValueListenerBean(String objectName, DoubleValueListener bean, List<EventField> eventFields) {
        Map<String, Consumer<RecordedEvent>> listeners = eventFields.stream().collect(
                Collectors.toMap(
                        t -> t.getEvent(),
                        t -> {
                            return event -> {
                                try {
                                    bean.hear(getValue(event, t.getField()));
                                } catch (RuntimeException re) {
                                    bean.hearException(re);
                                }
                            };
                        }
                        )
                );
        return new Bean(objectName, bean, listeners);
    }

    public static Bean newEventListenerBean(String objectName, RecordedEventListener bean, List<String> events) {
        Map<String, Consumer<RecordedEvent>> listeners = events.stream().collect(
                Collectors.toMap(
                        t -> t,
                        t -> {
                            return event -> {
                                try {
                                    bean.hear(event);
                                } catch (RuntimeException re) {
                                    bean.hearException(re);
                                }
                            };
                        }
                )
        );
        return new Bean(objectName, bean, listeners);
    }

    public static Bean newKeyValueListenerBean(String objectName, KeyValueListener bean, List<EventFieldKey> events) {

        Map<String, Consumer<RecordedEvent>> listeners = events.stream().collect(
                Collectors.toMap(
                        t -> t.getEvent(),
                        t -> {
                            return getKeyValueConsumer(bean, t);
                        }
                )
        );
        return new Bean(objectName, bean, listeners);
    }

    public static Bean newKeyValueDurationBean(String objectName, KeyValueDurationListener bean, List<EventFieldKey> events) {

        Map<String, Consumer<RecordedEvent>> listeners = events.stream().collect(
                Collectors.toMap(
                        t -> t.getEvent(),
                        t -> {
                            return getKeyValueDurationConsumer(bean, t);
                        }
                )
        );
        return new Bean(objectName, bean, listeners);
    }

    private static Consumer<RecordedEvent> getKeyValueConsumer(KeyValueListener bean, EventFieldKey t) {
        return new Consumer<RecordedEvent>() {
            //cache the key extractor
            private Function<RecordedEvent, String> keyExtractor;

            @Override
            public void accept(RecordedEvent event) {
                if(keyExtractor == null) {
                    keyExtractor = KeyExtractorUtil.makeKeyExtractor(event, t.getKeys());
                }
                bean.hear(
                        keyExtractor.apply(event),
                        getValue(event, t.getField())
                );
            }
        };
    }

    private static Consumer<RecordedEvent> getKeyValueDurationConsumer(KeyValueDurationListener bean, EventFieldKey t) {
        return new Consumer<RecordedEvent>() {
            //cache the key extractor
            private Function<RecordedEvent, String> keyExtractor;

            @Override
            public void accept(RecordedEvent event) {
                if(keyExtractor == null) {
                    keyExtractor = KeyExtractorUtil.makeKeyExtractor(event, t.getKeys());
                }
                bean.hear(
                        keyExtractor.apply(event),
                        getValue(event, t.getField()),
                        event.getDuration()
                );
            }
        };
    }

    private static double getValue(RecordedEvent event, String field) {
        if(field.equals("$0")) {
            return 0;
        }
        return event.getDouble(field);
    }

    public Bean(String objectName, Flushable mxBean, Map<String, Consumer<RecordedEvent>> listeners) {
        try {
            this.objectName = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("invalid objectName:" + objectName, e);
        }
        this.mxBean = mxBean;
        this.listeners = Collections.unmodifiableMap(new HashMap<>(listeners));
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public Map<String, Consumer<RecordedEvent>> getListeners() {
        return listeners;
    }

    public Flushable getMxBean() {
        return mxBean;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bean bean = (Bean) o;
        return Objects.equals(objectName, bean.objectName) &&
                Objects.equals(mxBean, bean.mxBean) &&
                Objects.equals(listeners, bean.listeners);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectName, mxBean, listeners);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Bean.class.getSimpleName() + "[", "]")
                .add("objectName=" + objectName)
                .add("mxBean=" + mxBean)
                .add("listeners=" + listeners)
                .toString();
    }


}


