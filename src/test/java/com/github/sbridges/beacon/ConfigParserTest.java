package com.github.sbridges.beacon;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.sbridges.beacon.jmx.histogram.Histogram;
import com.github.sbridges.beacon.jmx.gauge.Gauge;
import com.github.sbridges.beacon.jmx.inspector.Inspector;
import com.github.sbridges.beacon.jmx.rate.Rate;
import com.github.sbridges.beacon.jmx.top.Top;
import com.github.sbridges.beacon.jmx.top.TopConfig;
import com.github.sbridges.beacon.jmx.topsum.TopSum;

public class ConfigParserTest {

    @Test
    public void testParseEmpty() {
        Config conf = new ConfigParser().parse("events : []\n" +
                "objects : []\n");
        assertTrue(conf.getEvents().isEmpty());
        assertTrue(conf.getBeans().isEmpty());
    }

    @Test
    public void testParseEventDefaults() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects : []\n"
                );

        assertEquals(1, conf.getEvents().size());
        EventConfig eventConf = conf.getEvents().get(0);
        assertEquals(eventConf.getEventName(), "jdk.ExceptionStatistics");
        assertEquals(eventConf.getEventPeriod(), Optional.empty());
        assertEquals(eventConf.getEventThreshold(), Optional.empty());
        assertEquals(eventConf.getStackTrace(), false);

        assertTrue(conf.getBeans().isEmpty());

    }

    @Test
    public void testParseEventD() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "       eventPeriod : {seconds : 10}\n" +
                        "       eventThreshold : {seconds : 12}\n" +
                        "       stackTrace : true\n" +
                        "objects : []\n"

        );

        assertEquals(1, conf.getEvents().size());
        EventConfig eventConf = conf.getEvents().get(0);
        assertEquals(eventConf.getEventName(), "jdk.ExceptionStatistics");
        assertEquals(eventConf.getEventPeriod(), Optional.of(Duration.of(10, ChronoUnit.SECONDS)));
        assertEquals(eventConf.getEventThreshold(), Optional.of(Duration.of(12, ChronoUnit.SECONDS)));
        assertEquals(eventConf.getStackTrace(), true);

        assertTrue(conf.getBeans().isEmpty());

    }



    @Test
    public void testParseHistogram() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects : \n" +
                        "    - objectName : com.github.sbridges.beacon:event=Test\n" +
                        "      objectType : histogram\n" +
                        "      values : \n" +
                        "        - event : jdk.ExceptionStatistics\n" +
                        "          field : foo\n"
                );

        assertEquals(1, conf.getBeans().size());
        Bean b1 = conf.getBeans().get(0);
        Histogram bean =  (Histogram) b1.getMxBean();
        assertEquals("jdk.ExceptionStatistics", b1.getListeners().keySet().iterator().next());

    }

    @Test
    public void testParseGauge() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects : \n" +
                        "    - objectName : com.github.sbridges.beacon:event=Test\n" +
                        "      objectType : gauge\n" +
                        "      values : \n" +
                        "        - event : jdk.ExceptionStatistics\n" +
                        "          field : foo\n"
                );
        assertEquals(1, conf.getBeans().size());
        Bean b1 = conf.getBeans().get(0);
        Gauge bean =  (Gauge) b1.getMxBean();
        assertEquals("jdk.ExceptionStatistics", b1.getListeners().keySet().iterator().next());

    }


    @Test
    public void testParseInspectorDefault() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects : \n" +
                        "    - objectName : com.github.sbridges.beacon:event=Test\n" +
                        "      objectType : inspector\n" +
                        "      event : jdk.ExceptionStatistics \n"
                );
        assertEquals(1, conf.getBeans().size());
        Bean b1 = conf.getBeans().get(0);
        Inspector bean =  (Inspector) b1.getMxBean();
        assertEquals(1, bean.getSize());
        assertEquals("jdk.ExceptionStatistics", b1.getListeners().keySet().iterator().next());

    }

    @Test
    public void testParseInspectorSize() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects : \n" +
                        "    - objectName : com.github.sbridges.beacon:event=Test\n" +
                        "      objectType : inspector\n" +
                        "      event : jdk.ExceptionStatistics \n" +
                        "      inspectorConfig : \n" +
                        "         size : 10"
        );
        assertEquals(1, conf.getBeans().size());
        Bean b1 = conf.getBeans().get(0);
        Inspector bean =  (Inspector) b1.getMxBean();
        assertEquals(10, bean.getSize());
        assertEquals("jdk.ExceptionStatistics", b1.getListeners().keySet().iterator().next());

    }

    @Test
    public void testParseRateDefaults() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects : \n" +
                        "    - objectName : com.github.sbridges.beacon:event=Test\n" +
                        "      objectType : rate\n" +
                        "      values : \n" +
                        "        - event : jdk.ExceptionStatistics\n" +
                        "          field : foo\n"
        );

        assertEquals(1, conf.getBeans().size());
        Bean b1 = conf.getBeans().get(0);
        Rate bean =  (Rate) b1.getMxBean();
        assertFalse(bean.getRateConfig().isSum());
        assertEquals("jdk.ExceptionStatistics", b1.getListeners().keySet().iterator().next());

    }

    @Test
    public void testParseRateWithSum() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects : \n" +
                        "    - objectName : com.github.sbridges.beacon:event=Test\n" +
                        "      objectType : rate\n" +
                        "      values : \n" +
                        "        - event : jdk.ExceptionStatistics\n" +
                        "          field : foo\n" +
                        "      rateConfig : \n" +
                        "           sum : true"
        );

        assertEquals(1, conf.getBeans().size());
        Bean b1 = conf.getBeans().get(0);
        Rate bean =  (Rate) b1.getMxBean();
        assertTrue(bean.getRateConfig().isSum());
        assertEquals("jdk.ExceptionStatistics", b1.getListeners().keySet().iterator().next());

    }

    @Test
    public void testParseTop() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects :\n" +
                        "   - objectName : com.github.sbridges.beacon:event=jdk.ObjectCount,field=count\n" +
                        "     objectType : top\n" +
                        "     keyValues :\n" +
                        "       - event : jdk.ExceptionStatistics\n" +
                        "         keyFields : [objectClass]\n" +
                        "         valueField : count\n"
        );

        assertEquals(1, conf.getBeans().size());
        Bean b1 = conf.getBeans().get(0);
        Top bean =  (Top) b1.getMxBean();
        assertEquals(bean.getConfig().getPeriod(), Duration.of(5, ChronoUnit.SECONDS));
        assertEquals("jdk.ExceptionStatistics", b1.getListeners().keySet().iterator().next());

    }

    @Test
    public void testParseTopNonDefault() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects :\n" +
                        "   - objectName : com.github.sbridges.beacon:event=jdk.ObjectCount,field=count\n" +
                        "     objectType : top\n" +
                        "     topConfig :\n" +
                        "       period : {seconds : 60}\n" +
                        "     keyValues :\n" +
                        "       - event : jdk.ExceptionStatistics\n" +
                        "         keyFields : [objectClass]\n" +
                        "         valueField : count\n"

        );

        assertEquals(1, conf.getBeans().size());
        Bean b1 = conf.getBeans().get(0);
        Top bean =  (Top) b1.getMxBean();
        assertEquals(bean.getConfig().getPeriod(), Duration.of(60, ChronoUnit.SECONDS));
        assertEquals("jdk.ExceptionStatistics", b1.getListeners().keySet().iterator().next());

    }

    @Test
    public void testParseTopSumNonDefault() throws Exception {
        Config conf = new ConfigParser().parse(
                "events :\n" +
                        "     - eventName : jdk.ExceptionStatistics\n" +
                        "objects :\n" +
                        "   - objectName : com.github.sbridges.beacon:event=jdk.ObjectCount,field=count\n" +
                        "     objectType : topSum\n" +
                        "     topConfig :\n" +
                        "       period : {seconds : 60}\n" +
                        "     keyValues :\n" +
                        "       - event : jdk.ExceptionStatistics\n" +
                        "         keyFields : [objectClass]\n" +
                        "         valueField : count\n"

        );

        assertEquals(1, conf.getBeans().size());
        Bean b1 = conf.getBeans().get(0);
        TopSum bean =  (TopSum) b1.getMxBean();
        assertEquals(bean.getConfig().getPeriod(), Duration.of(60, ChronoUnit.SECONDS));
        assertEquals("jdk.ExceptionStatistics", b1.getListeners().keySet().iterator().next());

    }
}
