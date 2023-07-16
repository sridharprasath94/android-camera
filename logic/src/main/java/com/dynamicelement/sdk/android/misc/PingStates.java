package com.dynamicelement.sdk.android.misc;

/**
 * Ping states.
 */
public enum PingStates {
    /**
     * Returns this status if the MDDI service connection is very good.
     */
    PING_VERY_GOOD,
    /**
     * Returns this status if the MDDI service connection is good.
     */
    PING_GOOD,
    /**
     * Returns this status if the MDDI service connection is average.
     */
    PING_AVERAGE,
    /**
     * Returns this status if the MDDI service connection is bad.
     */
    PING_BAD;

    /**
     * Determine the Ping state from the single trip time.
     * Some times, if the timestamp sent along the request is greater than the server UNIX time,
     * it results in a negative value.
     * For such cases, the singleTripTime value is returned as the current server UNIX time
     * itself( For example: "1647509304995").
     * At the time, NumberFormatException occurs. But that does not mean that, the single trip
     * time is very huge.
     * So, We return PING_VERY_GOOD for such exception.
     *
     * @param singleTripTime is the singleTripTime received from the MDDI backend.
     * @return PingStates
     */
    public static PingStates calculatePing(String singleTripTime) {
        try {
            int singleTripTimeInt = Integer.parseInt(singleTripTime);
            if (singleTripTimeInt >= 0 && singleTripTimeInt <= 3500) {
                return PING_VERY_GOOD;
            }
            if (singleTripTimeInt > 3500 && singleTripTimeInt <= 4500) {
                return PING_GOOD;
            }
            if (singleTripTimeInt > 4500 && singleTripTimeInt <= 5500) {
                return PING_AVERAGE;
            }
            return PING_BAD;
        } catch (Exception e) {
            return PING_VERY_GOOD;
        }
    }
}
