package com.github.sbridges.beacon.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {

    /**
     * avoid a dependency on guava by implementing this ourselves, although the implementation is not as nice
     */
    public static <T> List<T> immutableCopyOf(List<T> list) {
        return Collections.unmodifiableList(new ArrayList<>(list));
    }
}
