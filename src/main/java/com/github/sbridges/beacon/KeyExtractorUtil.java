package com.github.sbridges.beacon;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jdk.jfr.ValueDescriptor;
import jdk.jfr.consumer.*;

final class KeyExtractorUtil {
    
    
    static Function<RecordedEvent, String> makeKeyExtractor(
            RecordedEvent e,
            List<String> keyFields) {
     
        if(keyFields.size() == 1) {
            return makeKeyExtractor(e, keyFields.get(0));
        }
        
        List<Function<RecordedEvent, String>> extractors = 
                keyFields.stream().map(t -> makeKeyExtractor(e, t))
                .collect(Collectors.toList());
        return event -> {
            StringBuilder sb = new StringBuilder(64);
            for(Function<RecordedEvent, String> extractor : extractors) {
                if(sb.length() != 0) {
                    sb.append(" ");
                }
                sb.append(extractor.apply(event));
            }
            return sb.toString();
        };
    }

    private static String format(RecordedStackTrace s) {
        if(s == null || s.getFrames() == null || s.getFrames().isEmpty()) {
            return "null";
        }
        RecordedFrame rf = s.getFrames().get(0);
        if(!rf.isJavaFrame()) {
            return rf.getType();
        }
        if(rf.getMethod() == null) {
            return "??";
        }
        return formatMethod(rf.getMethod());
    }

    private static String formatMethod(RecordedMethod m) {
        return m.getType().getName() + "." + m.getName() + " : " + m.getDescriptor();
    }

    private static Function<RecordedEvent, String> makeKeyExtractor(RecordedEvent e, String field) {
        if(field.equalsIgnoreCase("stack$top")) {
            return event -> format(event.getStackTrace());
        }

        for(ValueDescriptor vd : e.getFields()) {
            if(vd.getName().equals(field)) {
                switch(vd.getTypeName()) {
                case "java.lang.String" :
                    return event -> event.getString(field);
                case "boolean" :
                    return event -> Boolean.toString(event.getBoolean(field));
                case "byte" :
                case "short" :
                case "int" :
                case "long" :
                    return event -> Long.toString(event.getLong(field));
                case "double" :
                case "float" :
                    return event -> Double.toString(event.getDouble(field));
                case "java.lang.Class" :
                    return event -> event.getClass(field).getName();
                case "java.lang.Thread" :
                    return event -> {
                        Object v = event.getValue(field);
                        if(v == null) {
                            return "null";
                        }
                        if(v instanceof RecordedThread) {
                            RecordedThread thread = (RecordedThread) v;
                            return thread.getJavaName() + "(" + thread.getJavaThreadId() + ")";
                        }
                        //sometimes this returns a recorded object which is not a
                        //thread, and trying to call toString() on that fails with
                        //a class cast exception
                        return "??" + v.getClass().getSimpleName();
                    };
                default : throw new IllegalStateException("unrecognized:" + vd.getTypeName());
                }
            }
        }
        throw new IllegalStateException("no field:" + field + " in event:" + e);
    }

}
