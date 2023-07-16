package com.dynamicelement.sdk.android.exceptions;

/**
 * Throws exception when there is any error in the configuration. For example, providing the
 * wrong format for IP, port, certificate etc.
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
