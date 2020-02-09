package com.github.sbridges.beacon;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.sbridges.beacon.histogram.Histogram;
import com.github.sbridges.beacon.jmx.gauge.Gauge;
import com.github.sbridges.beacon.jmx.inspector.Inspector;
import com.github.sbridges.beacon.jmx.rate.Rate;
import com.github.sbridges.beacon.jmx.top.Top;
import com.github.sbridges.beacon.jmx.top.TopConfig;
import com.github.sbridges.beacon.jmx.topsum.TopSum;

public class ConfigParserTest {

    @Test
    public void testParseEmpty() {
        List<Bean> beans = new ConfigParser().parse("events : []");
        assertTrue(beans.isEmpty());
    }
    
    @Test
    public void testParseEmptyObjects() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" + 
                "   - eventName : jdk.CPULoad\n" + 
                "     eventPeriod : {seconds : 5}\n" + 
                "     objects : []" 
                );
        assertTrue(beans.isEmpty());
    }

    @Test
    public void testParseBean() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" + 
                "   - eventName : jdk.CPULoad\n" + 
                "     eventPeriod : {seconds : 20}\n" +
                "     eventThreshold : {minutes : 20}\n" +
                "     stackTrace: true \n" +
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.CpuLoad,field=machineTotal\n" + 
                "         objectType : gauge\n" + 
                "         gaugeConfig : \n" + 
                "           eventField : machineTotal\n" 
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        assertEquals(true, b1.getStackTrace());
        assertEquals(Duration.of(20, ChronoUnit.SECONDS), b1.getEventPeriod().get());
        assertEquals(Duration.of(20, ChronoUnit.MINUTES), b1.getEventThreshold().get());
    }
    
    @Test
    public void testParseBeanDefaults() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" + 
                "   - eventName : jdk.CPULoad\n" + 
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.CpuLoad,field=machineTotal\n" + 
                "         objectType : gauge\n" + 
                "         gaugeConfig : \n" + 
                "           eventField : machineTotal\n" 
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        assertFalse(b1.getStackTrace());
        assertFalse(b1.getEventPeriod().isPresent());
        assertFalse(b1.getEventThreshold().isPresent());
        
    }
    
    @Test
    public void testParseHistogram() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" + 
                "   - eventName : jdk.CPULoad\n" + 
                "     eventPeriod : {seconds : 5}\n" +
                "     stackTrace: true \n" +
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.CpuLoad,field=machineTotal\n" + 
                "         objectType : histogram\n" + 
                "         histogramConfig : \n" + 
                "           eventField : machineTotal\n" 
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        Histogram histogram =  (Histogram) b1.getListener();
        assertEquals("machineTotal", histogram.getEventField());
        
    }
    
    @Test
    public void testParseGauge() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" + 
                "   - eventName : jdk.CPULoad\n" + 
                "     eventPeriod : {seconds : 5}\n" +
                "     stackTrace: true \n" +
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.CpuLoad,field=machineTotal\n" + 
                "         objectType : gauge\n" + 
                "         gaugeConfig : \n" + 
                "           eventField : machineTotal\n" 
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        Gauge gauge =  (Gauge) b1.getListener();
        assertEquals("machineTotal", gauge.getEventField());
        
    }
    
    @Test
    public void testParseGaugeUnknownField() throws Exception {
        try {
            new ConfigParser().parse(
                    "events :\n" + 
                    "   - eventName : jdk.CPULoad\n" + 
                    "     eventPeriod : {seconds : 5}\n" + 
                    "     objects :\n" + 
                    "       - objectName : com.github.sbridges.beacon:event=jdk.CpuLoad,field=machineTotal\n" + 
                    "         objectType : gauge\n" + 
                    "         gaugeConfig : \n" + 
                    "           eventField1 : machineTotal\n" 
                    );
            fail();
        } catch(IllegalStateException e) {
            assertTrue(e.getMessage().contains("unrecognized keys:[eventField1]"));
        }
    }
    
    @Test
    public void testParseInspectorDefault() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" +
                "   - eventName : jdk.ExceptionStatistics\n" +  
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.ExceptionStatistics,field=throwables\n" + 
                "         objectType : inspector\n" +
                "         inspectorConfig : {}\n"  
                 
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        Inspector rate =  (Inspector) b1.getListener();
        assertEquals(1, rate.getSize());
    }
    
    @Test
    public void testParseInspector() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" +
                "   - eventName : jdk.ExceptionStatistics\n" +  
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.ExceptionStatistics,field=throwables\n" + 
                "         objectType : inspector\n" +
                "         inspectorConfig : \n" + 
                "            size : 5\n" 
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        Inspector rate =  (Inspector) b1.getListener();
        assertEquals(5, rate.getSize());
    }
    
    
    @Test
    public void testParseRate() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" +
                "   - eventName : jdk.ExceptionStatistics\n" + 
                "     eventPeriod : {seconds : 5}\n" + 
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.ExceptionStatistics,field=throwables\n" + 
                "         objectType : rate\n" + 
                "         rateConfig : \n" + 
                "            valueField: throwables\n" +
                "            period : {hours : 5}\n" + 
                "            sum : true\n"                
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        Rate rate =  (Rate) b1.getListener();
        assertEquals("throwables", rate.getRateConfig().getValueVield());
        assertEquals(Duration.of(5, ChronoUnit.HOURS), rate.getRateConfig().getPeriod());
        assertEquals(true, rate.getRateConfig().isSum());
        
    }
    
    
    @Test
    public void testParseRateDefaults() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" +
                "   - eventName : jdk.ExceptionStatistics\n" + 
                "     eventPeriod : {seconds : 5}\n" + 
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.ExceptionStatistics,field=throwables\n" + 
                "         objectType : rate\n" + 
                "         rateConfig : \n" + 
                "            valueField: throwables\n" 
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        Rate rate =  (Rate) b1.getListener();
        assertEquals("throwables", rate.getRateConfig().getValueVield());
        assertEquals(Duration.of(5, ChronoUnit.SECONDS), rate.getRateConfig().getPeriod());
        assertEquals(false, rate.getRateConfig().isSum());
        
    }
    
    @Test
    public void testParseTop() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" +
                "   - eventName : jdk.ExceptionStatistics\n" + 
                "     eventPeriod : {seconds : 5}\n" + 
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.ExceptionStatistics,field=throwables\n" + 
                "         objectType : top\n" + 
                "         topConfig : \n" + 
                "            keyFields: [a, b]\n" +
                "            valueField: c\n"                
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        Top top =  (Top) b1.getListener();
        TopConfig conf = top.getConfig();
        assertEquals(Arrays.asList("a", "b"), conf.getKeyFields());
        assertEquals(Duration.of(5, ChronoUnit.SECONDS), conf.getPeriod());
        assertEquals("c", conf.getValueField());
        
    }
    
    @Test
    public void testParseTopSum() throws Exception {
        List<Bean> beans = new ConfigParser().parse(
                "events :\n" +
                "   - eventName : jdk.ExceptionStatistics\n" + 
                "     eventPeriod : {seconds : 5}\n" + 
                "     objects :\n" + 
                "       - objectName : com.github.sbridges.beacon:event=jdk.ExceptionStatistics,field=throwables\n" + 
                "         objectType : topSum\n" + 
                "         topConfig : \n" + 
                "            keyFields: [a, b]\n" +
                "            period : {hours : 5}\n" +                 
                "            valueField: c\n"                
                );
        assertEquals(1, beans.size());
        Bean b1 = beans.get(0);
        TopSum top =  (TopSum) b1.getListener();
        TopConfig conf = top.getConfig();
        assertEquals(Arrays.asList("a", "b"), conf.getKeyFields());
        assertEquals(Duration.of(5, ChronoUnit.HOURS), conf.getPeriod());
        assertEquals("c", conf.getValueField());
        
    }
}
