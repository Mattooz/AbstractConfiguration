package it.niccolomattei.configuration.impl.basic;

import it.niccolomattei.configuration.api.*;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.FileHandler;

public class BasicConfigurationFactory implements ConfigurationFactory<BasicConfigurationObject> {

    private final ConfigurationReader<?, ?> reader;

    public BasicConfigurationFactory(ConfigurationReader<?, ?> reader) {
        this.reader = reader;
    }

    @Override
    public Optional<BasicConfigurationObject> optConfiguration(ResourceHandler handler) {
        try {
            return Optional.of(getConfiguration(handler));
        } catch (ConfigurationException ex) {
            //do nothing
            return Optional.empty();
        }
    }

    @Override
    public BasicConfigurationObject getConfiguration(ResourceHandler handler) throws ConfigurationException {
        byte[] data;

        try {
            data = handler.toByteArray();
        } catch (IOException ex) {
            throw ConfigurationException.throwConfigException("Couldn't read source! Source doesn't exist!");
        }

        BasicConfigurationObject object = reader.read(data, BasicConfigurationObject::empty, BasicConfigurationList::empty);
        object.setSource(handler);
        return object;
    }

    @Override
    public void writeDefault(ResourceHandler handler) {
        try {
            handler.toOriginalFormat(this.reader.defaultObject());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void write(ConfigurationObject configurationObject) {
        if(configurationObject.getSource() == null) throw ConfigurationException.throwConfigException("Cannot write config as no source was defined!");

        try {
            configurationObject.getSource().toOriginalFormat(reader.write(configurationObject));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
