package it.niccolomattei.configuration.api;

public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public static ConfigurationException throwConfigException(String message) {
        return new ConfigurationException(message);
    }
}
