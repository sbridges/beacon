package com.github.sbridges.beacon;

import com.github.sbridges.beacon.internal.Util;
import jdk.jfr.Event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * A utility to expose JFR events over JMX.<p>
 */
public final class Beacon implements BeaconMXBean {

    static final String OBJECT_NAME = "com.github.sbridges.beacon:name=Beacon";

    private final Object lock = new Object();
    private final List<Bean> beans;
    private final List<EventConfig> events;
    private EventWatcher eventWatcher;
    private String conf;
    
    /**
     * Starts beacon using the standard configuration lookup process.<P>
     * 
     * Meant to be used when Beacon is deployed as a java agent.
     */
    public static void premain(String agentArgs) {
        new Beacon().start();
    }

    /**
     * Loads the beacon configuration using the standard lookup process.<P>
     * 
     * If the system property com.github.sbridges.beacon.conf.path is set, tries to 
     * load a yaml file from that path.<P>
     * 
     * If the config wasn't loaded from a path, and there is a system property
     * com.github.sbridges.beacon.conf.resouce , will try to load the conf
     * from the classpath, using Beacon.class.getResourceAsStream <P>
     * 
     * If neither system property is set, will load a default yaml config. 
     */
    public static String loadDefaultConfig() {
        String conf;
        
        String configPath = System.getProperty("com.github.sbridges.beacon.conf.path");
        if(configPath != null) {
            Path path = Paths.get(configPath);
            if(!Files.exists(path)) {
                throw new IllegalStateException("conf path does not exitst:" + path);
            }
            try {
                conf = Files.readString(path);
            } catch(IOException e) {
                throw new IllegalStateException("could not read path:" + path, e);
            }
        } else {
            String resource = System.getProperty("com.github.sbridges.beacon.conf.resouce", "beacon-default.yaml");
            InputStream is = Beacon.class.getResourceAsStream(resource);
            if(is == null) {
                throw new IllegalStateException("could not find resouce:" + resource);
            }
            try(is) {
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                conf = new BufferedReader(reader).lines().collect(Collectors.joining("\n"));
            } catch(IOException e) {
                throw new IllegalStateException("could not read resource:" + resource, e);
            }
        }
        return conf;
    }
        
    /**
     * Create a Beacon instance using the standard conf lookup
     */
    public Beacon() {
        this(loadDefaultConfig());
    }
    
    /**
     * @param conf  a yaml file describing the jfr events to listen to, and how to expose them to jmx.
     */
    public Beacon(String conf) {
        this(conf, new ConfigParser().parse(conf));
    }
    
    public Beacon(List<Bean> beans, List<EventConfig> events) {
        this(null, new Config(beans, events));
    }

    private Beacon(String conf, Config config) {
        this.conf = conf;
        this.beans = Util.immutableCopyOf(config.getBeans());
        this.events = Util.immutableCopyOf(config.getEvents());
    }

    /**
     * Start exposing JFR events over jmx.  This will start a background daemon thread.<P>  
     * 
     * Call {@link #stop()} to stop exposing events, and unregister any jmx beans.<P>
     * 
     * Only one instance of Beacon can be started at any one time in a jvm.<P>
     * 
     * @throws IllegalStateException if this instance of Beacon, or another instance of Beacon instance is running
     */
    public void start() {
        synchronized(lock) {
            if(eventWatcher != null) {
                throw new IllegalStateException("already started");
            }
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                mbs.registerMBean(
                        this, 
                        new ObjectName(OBJECT_NAME));
            } catch(InstanceAlreadyExistsException e) {
                throw new IllegalStateException("another instance of Beacon is running", e);
            } 
            catch(Exception e) {
                throw new IllegalStateException(e);
            }
            this.eventWatcher = new EventWatcher(beans, events);
            this.eventWatcher.start();
        }
    }
    
    /**
     * Stop this instance of Beacon, or if this instance is not running, do nothing.
     */
    public void stop() {
        synchronized(lock) {
            if(this.eventWatcher == null) {
                return;
            }
            this.eventWatcher.stop();
            this.eventWatcher = null;
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            try {
                mbs.unregisterMBean(
                        new ObjectName(OBJECT_NAME));
            } catch(InstanceNotFoundException infe) {
                //ignore, we can't unregister the bean
                //as its not there
            }
            catch(Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getConf() {
        return conf.split("\n");
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void updateConf(String newConf) {
        
        //make sure the config is valid before we do anything
        new ConfigParser().parse(newConf);
        
        try {
            synchronized(lock) {
                boolean running = eventWatcher != null;
                if(running) {
                    stop();
                }
                
                this.conf = newConf;
                
                if(running) {
                    start();
                }
            }
        } catch(Exception e) {
            Writer sink = new StringWriter();
            PrintWriter pw = new PrintWriter(sink);
            e.printStackTrace(pw);
            
            //this method is called may be called jmx, so 
            //give an exception that can be serialized over
            //jconsole
            throw new IllegalStateException("cause:" + sink.toString());
        }
        
    }

    @Override
    public long getRecordedEvents() {
        return eventWatcher.getRecordedEvents();
    }

    @Override
    public long getFlushes() {
        return eventWatcher.getFlushes();
    }
}
