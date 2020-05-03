Beacon
======

Beacon exposes [JFR](https://openjdk.java.net/jeps/349) events and stats over [JMX](https://openjdk.java.net/groups/jmx/).

Using Beacon you can use jconsole to connect to a running jvm and answer questions like :
 
 - What files are my jvm writing to
 - What remote hosts are my jvm talking to
 - What classes are taking up most of my heap

Requirements
============

Beacon relies on [JFR Event Streaming](https://openjdk.java.net/jeps/349) which is only available in Java 14 and later.

Installing
==========

Add a dependency using maven


    <dependency>
      <groupId>com.github.sbridges</groupId>
      <artifactId>beacon</artifactId>
      <version>${version}</version>
    </dependency>

Running
=======

Beacon can be run in two modes.  You can call Beacon programmatically using code like,

      var beacon = new Beacon();
      beacon.start();

Or by using Beacon as a java agent by adding the following argument to your java command,

     -javaagent:<path-to-beacon-jar-file>


MXBeans exposed by beacon
=========================

Inspector
---------

Captures the latest Event as a String.  This is useful for debgging and exploring events.

Gauge
-----

Captures the last value.

For example, you use this to expose the jdk.CPULoad#machineTotal field.

Rate
----

Captures the rate of change of a value.  

For example, you can capture the exceptions per second being thrown in jvm using jdk.ExceptionStatistics#throwables.  

You can also sum values from multiple events.  For example getting the rate of all network writes by summing the jdk.SocketRead#bytesRead from all events.

Top
---

Captures the name/value pairs sorted by value over the previous sampling interval, similar to the unix top command. 

For example, you can use jdk.ObjectCount#objectClass and jdk.ObjectCount#count to
show how many instances of each class you have (limited to those which take up some percentage of the heap)

TopSum
------

Similar to top but this sums the values of all events with the same nane, rather than using the value of the last event.  

For example, you can use jdk.FileRead#path and jdk.FileRead#bytesRead to see which files are being read by the jvm.

Histogram
---------

Exposes a Histogram of a value, both over the last
minute and over all time.


Configuration with yaml
===========

The mapping between JFR events and JMX beans can be configured using yaml.

By default, Beacon will load [beacon-default.yaml](https://github.com/sbridges/beacon/blob/master/src/main/resources/com/github/sbridges/beacon/beacon-default.yaml) which is included in the beacon jar file.

To change the default configuration, start your jvm either using,

    -Dcom.github.sbridges.beacon.conf.path=<full path to a yaml configuration file>

Or

    -Dcom.github.sbridges.beacon.conf.resouce=<resource on the classpath>

`<resource on the classpath>` will be loaded using `Beacon.class.getResourceAsStream()`

Configuration programmatically
===============

If you are using beacon as a library, and not as a java agent, you can provide Beacon a configuration String through the `Beacon(String)` constructor,
or you can programmatically create an instance using the `Beacon(List<Bean> beans, List<EventConfig> events)` constructor.


Configuration yaml file format
-------------------------------

Beacon can be configured using a yaml file.  

See [beacon-default.yaml](https://github.com/sbridges/beacon/blob/master/src/main/resources/com/github/sbridges/beacon/beacon-default.yaml) for an example.

The top-level of the yaml is a map with two keys, events and objects.

      events : [ <list of Events> ]
      objects : [ <list of Objects> ]

Events
------

events is a list of Maps.  Where each map describes a JFR event to monitor.

As an example,

      events :
         - eventName : jdk.ExceptionStatistics
           eventPeriod : {seconds : 5}

The allowed keys for each event are :

| Name | Description | Required |
| -----|-------------|---------|
|eventName | The name of the JFR Event| Yes |
|eventPeriod |If present how often the jvm should emit this event. Format is {\<timeUnit> : \<value>}, for example {seconds : 5}. | No, defaults to no period |
|  eventThreshold  |  If present, the jvm should only  emit events with a duration greater than threshold. Format is {\<timeUnit> : \<value>}, for example {seconds : 5}.  | No, defaults to no threshold |
| stackTrace | If present, should these events capture their stack traces. | No, defaults to false |

Objects 
------

Each Object must have two key which describe the object type, and may have other keys which configures the bean.  The required keys are :

| Name | Description | Required |
| -----|-------------|---------|
| objectName | The JMX [ObjectName](https://docs.oracle.com/en/java/javase/13/docs/api/java.management/javax/management/ObjectName.html) that this bean is bound to. | Yes |
| objectType | The type of the object, one of gauge, inspector, rate, top, topSum or histogram. | Yes |

Gauge
-----

A gauge is configured with a values key which lists the JFR events to listen for.  For example :

       - objectName : com.github.sbridges.beacon:event=jdk.CPULoad,field=jvmUser
         objectType : gauge
         values :
           - event : jdk.CPULoad
             field : jvmUser


Inspector
---------

An inspector is configured with an events key which gives the event to inspect, and optionally an InspectorConfig which specifies the number of objects to keep.

For example :

    - objectName : com.github.sbridges.beacon:event=Test
      objectType : inspector
      event : jdk.ExceptionStatistics 
      inspectorConfig :
         size : 10

Rate Config fields are :

| Name | Description | Required |
| -----|-------------|---------|
| size | The number of events to keep, as new events arrive older ones are discarded. | No, defaults to 1 |

Rate
----

if the objectType is rate, the Object may have a rateConfig key and must have a values key listing the events and fields to watch.  For example :

    - objectName : com.github.sbridges.beacon:event=AllocationRate
      objectType : rate
      rateConfig :
        sum : true
        period : {seconds : 10}
      values :
          - event : jdk.ObjectAllocationOutsideTLAB
            field : allocationSize
          - event : jdk.ObjectAllocationInNewTLAB
            field : tlabSize

Rate Config fields are :

| Name | Description | Required |
| -----|-------------|---------|
| sum | If set to true, then this rate will sum the value seen by all events, rather than take the value of the last event. Defaults to false.  See rate documentation above for examples. | No, defaults to false |
| period | Rate is calculated at most once every period. Format is {\<timeUnit> : \<value>}, for example {seconds : 2}. | No, defaults to 5 seconds |

Top/TopSum
-----------

Top/Top sum require a keyValues key which declares which events to listen to, and optionall a topConfig key which configures the top/topSum

An example of a TopSum declaration is

       - objectName : com.github.sbridges.beacon:event=jdk.SocketRead,by=byHostPort
         objectType : topSum
         topConfig :
           period : {seconds : 5}
         keyValues :
           - event : jdk.SocketRead
             keyFields : [host, port]
             valueField : bytesRead
             
An example of a Top declaration is,

       - objectName : com.github.sbridges.beacon:event=jdk.ObjectCount,field=totalSize
         objectType : top
         topConfig :
           period : {seconds : 60}
         keyValues :
           - event : jdk.ObjectCount
             keyFields : [objectClass]
             valueField : totalSize

topConfig allows the following keys :

| Name | Description | Required |
| -----|-------------|---------|
| period | The top list is recalculated at most once every period.  | No, defaults to 5 seconds |

Histogram
---------

If the objectType is histogram, it must have a values key which describes the events of the histogram

As an example :

        - objectName : com.github.sbridges.beacon:event=jdk.SocketRead,type=histo
            objectType : histogram
            values : 
               - event : jdk.SocketRead
                 field : bytesRead



Getting information about JFR events for your jvm
=================================================

Start your jvm and get a recording file using the command,

    jcmd <PID> JFR.dump filename=/tmp/recording.tmp.jfr

After getting a recording, you can use the jfr command to list the types of JFR events registered in the jvm that created the file with the command,

     jfr metadata /tmp/recording.tmp.jfr

This includes data for all events that this JVM can produce.
