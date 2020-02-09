package com.github.sbridges.beacon;

/**
 * Java app for testing beacon.
 *  
 */
final class BeaconMain {
   
    /**
     * Starts beacon and never returns. 
     */
    public static void main(String[] args) {
        Beacon.premain(null);
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
