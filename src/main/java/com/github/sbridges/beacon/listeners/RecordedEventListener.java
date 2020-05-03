package com.github.sbridges.beacon.listeners;

import jdk.jfr.consumer.RecordedEvent;

public interface RecordedEventListener extends Flushable {

    void hear(RecordedEvent event);

}
