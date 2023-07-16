package com.dynamicelement.sdk.android.misc;

import static com.dynamicelement.sdk.android.misc.PingStates.PING_AVERAGE;
import static com.dynamicelement.sdk.android.misc.PingStates.PING_BAD;
import static com.dynamicelement.sdk.android.misc.PingStates.PING_GOOD;
import static com.dynamicelement.sdk.android.misc.PingStates.PING_VERY_GOOD;
import static com.dynamicelement.sdk.android.misc.PingStates.calculatePing;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PingStatesUnitTest {
    /**
     * Check different ping states by checking with different values
     */
    @Test
    public void PingStates_calculatePingTest() {
        assertEquals(PING_VERY_GOOD, calculatePing("0"));
        assertEquals(PING_VERY_GOOD, calculatePing("1000"));
        assertEquals(PING_VERY_GOOD, calculatePing("3500"));
        assertEquals(PING_GOOD, calculatePing("3501"));
        assertEquals(PING_GOOD, calculatePing("4000"));
        assertEquals(PING_GOOD, calculatePing("4500"));
        assertEquals(PING_AVERAGE, calculatePing("4501"));
        assertEquals(PING_AVERAGE, calculatePing("4800"));
        assertEquals(PING_AVERAGE, calculatePing("5500"));
        assertEquals(PING_BAD, calculatePing("5501"));
        assertEquals(PING_BAD, calculatePing("6000"));
        assertEquals(PING_BAD, calculatePing("10000"));
        assertEquals(PING_BAD, calculatePing("2147483647"));
        assertEquals(PING_VERY_GOOD, calculatePing("2147483648"));
    }
}