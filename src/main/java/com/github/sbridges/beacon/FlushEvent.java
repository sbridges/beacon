package com.github.sbridges.beacon;

import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.Period;

@Name(FlushEvent.NAME)
@Label("periodic event for flushing")
//default to once a chunk
//it doesn't look like this has an effect
@Period
class FlushEvent extends Event {
    static final String NAME = "com.github.sbridges.beacon.FlushEvent";
}
