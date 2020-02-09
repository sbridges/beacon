package com.github.sbridges.beacon;

import static org.junit.Assert.*;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;

public class BeaconTest {

    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    
    @Test
    public void testBeaconStartStop() throws Exception {
        
        Beacon fixture = new Beacon();
        fixture.start();
        assertNotNull(mbs.getMBeanInfo(new ObjectName(Beacon.OBJECT_NAME)));
        fixture.stop();
        try {
            mbs.getMBeanInfo(new ObjectName(Beacon.OBJECT_NAME));
            fail();
        } catch(InstanceNotFoundException e) {
            //ok, no such bean
        }
        
        fixture.start();
        assertNotNull(mbs.getMBeanInfo(new ObjectName(Beacon.OBJECT_NAME)));
        fixture.stop();
        
    }
    
}
