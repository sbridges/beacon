package com.github.sbridges.beacon.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to catch and track exceptions
 */
public final class FirstException {

    private static Logger logger = Logger.getLogger(FirstException.class.getName());

    private volatile String firstException;

    public void hear(Throwable t) {
        if (firstException == null) {
            logger.log(Level.WARNING, t.getMessage(), t);
            StringWriter sink = new StringWriter();
            PrintWriter pw = new PrintWriter(sink);
            t.printStackTrace(pw);
            firstException = "Failed for event:\n" + t + "\nException:\n"
                    + sink.toString();
        }
    }

    public String[] get() {
        if (firstException == null) {
            return null;
        }
        return firstException.split("\n");
    }

}
