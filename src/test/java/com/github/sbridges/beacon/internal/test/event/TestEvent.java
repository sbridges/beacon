package com.github.sbridges.beacon.internal.test.event;

import java.util.UUID;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;




@Name(TestEvent.TEST_EVENT)
@Label("Test")
public class TestEvent extends Event {
    
    static final String TEST_EVENT = "com.github.sbridges.beacon.internal.test.TestEvent";

    @Name("Id")
    public final String id = UUID.randomUUID().toString();

    @Name("aString")
    public String aString;

    
    @Name("aLong")
    public long aLong;

    @Name("aByte")
    public byte aByte;
    
    
    @Name("aClass")
    public Class aClass;
    
    @Name("aThread")
    public Thread aThread;
    
    @Name("aDouble")
    public double aDouble;

}
