package it.niccolomattei.configuration.impl.basic;

import it.niccolomattei.configuration.api.ConfigurationElement;
import it.niccolomattei.configuration.api.ConfigurationException;
import it.niccolomattei.configuration.api.ConfigurationList;
import it.niccolomattei.configuration.api.ResourceHandler;

import java.util.Iterator;
import java.util.LinkedList;

public class BasicConfigurationList implements ConfigurationList {

    private LinkedList<Object> contents;
    private ConfigurationElement parent;
    private String sectionPath;

    public static BasicConfigurationList empty() {
        return new BasicConfigurationList(new LinkedList<>());
    }

    public static BasicConfigurationList of(Iterable<?> iterable) {
        BasicConfigurationList configurationObject = new BasicConfigurationList(new LinkedList<>());
        configurationObject.putAll(iterable);
        return configurationObject;
    }

    public static BasicConfigurationList of(Object[] array) {
        BasicConfigurationList configurationObject = new BasicConfigurationList(new LinkedList<>());
        configurationObject.putAll(array);
        return configurationObject;
    }

    private BasicConfigurationList(LinkedList<Object> contents) {
        this.contents = contents;
    }



    @Override
    public void put(Object value) {
        if (value instanceof Number || value instanceof String)
            this.contents.offer(value);
        else if (value instanceof ConfigurationElement) {
            ((ConfigurationElement) value).setParent(this, "[" + this.contents.size() + "]");
            this.contents.offer(value);
        } else if (value.getClass().isArray()) {
            this.put(BasicConfigurationList.of((Object[]) value));
        } else if(value instanceof Iterable) {
            this.put(BasicConfigurationList.of((Iterable<?>) value));
        } else
            this.contents.offer(String.valueOf(value));
    }

    @Override
    public void putAll(Object[] o) {
        for(Object val : o)
            this.put(val);
    }

    @Override
    public void putAll(Iterable<?> o) {
        o.forEach(this::put);
    }

    @Override
    public <E> E get(int index) throws ConfigurationException {
        E e = opt(index, null);

        if (e == null)
            throw ConfigurationException.throwConfigException("Out of bounds or couldn't cast object to desired class! At: ");

        return e;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E opt(int index, E defaultValue) {
        E e;
        try {
            e = (E) this.contents.get(index);
        } catch (IndexOutOfBoundsException ex) {
            //do nothing
            e = null;
        }

        return e == null ? defaultValue : e;
    }

    @Override
    public int size() {
        return contents.size();
    }

    @Override
    public Object originalFormat() {
        return null;
    }

    @Override
    public boolean isSection() {
        return sectionPath != null;
    }

    @Override
    public String sectionPath() {
        return sectionPath;
    }

    @Override
    public ConfigurationElement getParent() {
        return parent;
    }

    @Override
    public void setParent(ConfigurationElement object, String sectionPath) {
        this.parent = object;
        this.sectionPath = sectionPath;
    }

    @Override
    public ResourceHandler getSource() {
        throw new UnsupportedOperationException("For the time being this operation is not supported in lists");
    }

    @Override
    public void setSource(ResourceHandler handler) {
        throw new UnsupportedOperationException("For the time being this operation is not supported in lists");
    }

    @Override
    public Iterator<Object> iterator() {
        return this.contents.iterator();
    }
}
