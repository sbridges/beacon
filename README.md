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

TODO

Running
=======

Beacon can be run in two modes.  You can call Beacon programaticaly using code like,

      var beacon = new Beacon();
      beacon.start();

Or by using Beacon as a java agent by adding the following argument to your java command,

     -javaagent:<path-to-beacon-jar-file>


MXBeans exposed by beacon
=========================

Inspector
---------

Captures the latest Event as a String

Gauge
-----

Captures the last value of a specific event field, and exposes it as a double.  

For example, you use this to expose the jdk.CPULoad#machineTotal feild.

Rate
----

Captures the change of a value.  

For example you can capture the exceptions per second being thrown in jvm using jdk.ExceptionStatistics#throwables.  

You can also sum values from multiple events.  For example getting the rate of all network writes by summing the jdk.SocketRead#bytesRead from all events


Top
---

Captures the name/value pairs sorted by value over the previous sampling interval, simular to the unix top command. 

For example, you can use jdk.ObjectCount#objectClass and jdk.ObjectCount#count to
show how many instances of each class you have (limited to those which take up some percentage of the heap)

TopSum
------

Similar to top but this sums the values of all events with the same name, rather than using the value of the last event.  

For example, you can use jdk.FileRead#path and jdk.FileRead#bytesRead to see which files are being read by the jvm.

Histogram
---------

Exposes a Histogram of a value, both over the last
minute and over all time.



Configuration
===========

The mapping between JFR events and JMX beans is configured using yaml.

By default Beacon will load [beacon-default.yaml](https://github.com/sbridges/beacon/blob/master/src/main/resources/com/github/sbridges/beacon/beacon-default.yaml) which is included in the beacon jar file.

To change the default configuration, start your jvm either using,

    -Dcom.github.sbridges.beacon.conf.path=<full path to a yaml configuration file>

Or

    -Dcom.github.sbridges.beacon.conf.resouce=<resource on the classpath>

\<resource on the classpath\> will be loaded using Beacon.class.getResourceAsStream()

If you are using beacon as a library, and not as a java agent, you can provide Beacon a configuration String through the Beacon(String) constructor.


Configuration yaml file format
-------------------------------

Beacon is configured using a yaml file.  

See [beacon-default.yaml](https://github.com/sbridges/beacon/blob/master/src/main/resources/com/github/sbridges/beacon/beacon-default.yaml) for an example.

The top level of the yaml is a map with a single key called events.  The value of this key is a list.

      events : [ <list of Events> ]

Each Event is a map, with keys :

| Name | Description | Required |
| -----|-------------|---------|
|eventName | The name of the JFR Event| Yes |
|eventPeriod |If present how often the jvm should emit this event. Format is {\<timeUnit> : \<value>}, for example {seconds : 5}. | No |
|  eventThreshold  |  If present, the jvm should only  emit events with a duration greater than threshold. Format is {\<timeUnit> : \<value>}, for example {seconds : 5}.  | No |
| stackTrace | If present, should these events capture their stack traces. | No, defaults to false |
| objects |  A list of Objects, where each Object represents a JMX bean. | Yes |




Each Object must have two key which describe the object type, and may have another key which configures the bean.  The required keys are :

| Name | Description | Required |
| -----|-------------|---------|
| objectName | The JMX [ObjectName](https://docs.oracle.com/en/java/javase/13/docs/api/java.management/javax/management/ObjectName.html) that this bean is bound to. | Yes |
|  objectType | The type of the object, one of gauge, inspector, rate, top, topSum or histogram. | Yes |

Gauge
-----

If the objectType is gauge, the Object must have a gaugeConfig key, whose value is a map with keys :

| Name | Description | Required |
| -----|-------------|---------|
| eventField | The field of the JFR event which is exposed by this gauge.  This event field must be a numeric type, and will be exposes as a double. | Yes |

Inspector
---------

If the objectTye is inspector the Object must have a inspectorConfig key, whose value is a map with keys :

| Name | Description | Required |
| -----|-------------|---------|
| size | The number of events. | No, defaults to 1 |

Rate
----

if the objectType is rate, the Object must have a rateConfig key, whose value is a map with keys 

| Name | Description | Required |
| -----|-------------|---------|
| sum | If true to true, then this rate will sum the value seen by all events, rather than take the value of the last event. Defaults to false.  See rate documentation above for examples. | No, defaults to false |
| period | Tate is calculated at most once every period. Format is {\<timeUnit> : \<value>}, for example {seconds : 2}. | No, defaults to 5 seconds |
| valueField | The field of the JFR event to use for calculating the rate.  This must be a numeric field. | Yes |

Top/TopSum
-----------

If the objectType is top or topSum the Object field must have a topConfig key whose value is a map with the following keys :

| Name | Description | Required |
| -----|-------------|---------|
| keyFields | A list of 1 or more event fields names which are used as the name of the entries in the top list. | Yes |
| valueField | The field of the JFR event to use for calculating the top names.  This must be a numeric field. | Yes |
| period | The top list is recalculated at most once every period.  | No, defaults to 5 seconds |

Histogram
---------

If the objectType is histogram, the Object must have a histogramConfig key, whose value is a map with keys :

| Name | Description | Required |
| -----|-------------|---------|
| eventField | The field of the JFR event which is exposed by this histogram.  This event field must be a numeric type, and will be exposes as a double. | Yes |


Getting information about JFR events for your jvm
=================================================

Start your jvm and get a recording file using the command,

    jcmd <PID> JFR.dump filename=/tmp/recording.tmp.jfr

After getting a recording, you can use the jfr command to list the types of JFR events registered in the jvm that created the file with the command,

     jfr metadata /tmp/recording.tmp.jfr

This includes data for all events that this JVM can produce.