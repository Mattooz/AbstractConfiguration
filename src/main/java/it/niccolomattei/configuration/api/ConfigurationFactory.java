package it.niccolomattei.configuration.api;

import java.util.Optional;

public interface ConfigurationFactory<T extends ConfigurationObject> {

    Optional<T> optConfiguration(ResourceHandler handler);
    T getConfiguration(ResourceHandler handler) throws ConfigurationException;

    void writeDefault(ResourceHandler handler);
    void write(ConfigurationObject configurationObject);

}
