package it.niccolomattei.configuration.impl.basic;

import it.niccolomattei.configuration.api.ConfigurationElement;
import it.niccolomattei.configuration.api.ConfigurationObject;
import it.niccolomattei.configuration.api.ConfigurationException;
import it.niccolomattei.configuration.api.ResourceHandler;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class BasicConfigurationObject implements ConfigurationObject {

    private final HashMap<String, Object> contents;
    private String sectionPath;
    private ConfigurationElement parent;
    private ResourceHandler source;

    public BasicConfigurationObject(HashMap<String, Object> contents, String sectionPath) {
        this.contents = contents;
        this.sectionPath = sectionPath;
        this.source = null;
    }

    public static BasicConfigurationObject empty() {
        return new BasicConfigurationObject(new HashMap<>(), null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E get(String key) throws ConfigurationException {
        if (key == null || key.isEmpty()) throw ConfigurationException.throwConfigException("Key is empty! At: " + getFullPath(key));

        String[] path = key.split("\\.");
        Object o = null;

        if (path.length == 1) {
            o = contents.get(path[0]);
        } else if (path.length > 1) {
            int depth = 0;
            ConfigurationObject nested = this;
            while (depth < path.length - 1) {
                try {
                    nested = nested.get(path[depth]);
                } catch (ClassCastException | NullPointerException ex) {
                    throw ConfigurationException.throwConfigException("Error while obtaining object: couldn't retrieve nested ConfigurationObject. At: " + getFullPath(key));
                }
                depth++;
            }

            o = nested.get(path[path.length - 1]);
        }

        E e;
        try {
            e = (E) o;
        } catch (ClassCastException ex) {
            throw ConfigurationException.throwConfigException("Error while obtaining object: couldn't cast to required class. At: " + getFullPath(key));
        }

        return e;
    }

    @Override
    public <E> E opt(String key, E defaultValue) {
        E e = null;
        try {
            e = get(key);
        } catch (ConfigurationException ex) {
            //do nothing
        }

        return e == null ? defaultValue : e;
    }

    @Override
    public void put(String key, Object value) {
        if (key == null || key.isEmpty()) throw ConfigurationException.throwConfigException("Key is empty! At: " + getFullPath());

        String[] path = key.split("\\.");

        if (path.length == 1) {
            if (value instanceof Number || value instanceof String)
                this.contents.put(key, value);
            else if (value instanceof ConfigurationElement) {
                ((ConfigurationElement) value).setParent(this, key);
                this.contents.put(key, value);
            } else if (value.getClass().isArray()) {
                BasicConfigurationList list = BasicConfigurationList.of((Object[]) value);
                list.setParent(this, key);
                this.contents.put(key, list);
            } else if(value instanceof Iterable) {
                BasicConfigurationList list = BasicConfigurationList.of((Iterable<?>) value);
                list.setParent(this, key);
                this.contents.put(key, list);
            } else
                this.contents.put(key, String.valueOf(value));
        } else if (path.length > 1) {
            int depth = 0;
            ConfigurationObject nested = this;

            while (depth < path.length - 1) {
                ConfigurationObject object = nested.opt(path[depth], BasicConfigurationObject.empty());
                nested.put(path[depth], object);
                nested = object;
                depth++;
            }

            nested.put(path[path.length - 1], value);
        }
    }

    @Override
    public Optional<String> optString(String key) {
        return Optional.ofNullable(get(key));
    }

    @Override
    public String getString(String key) throws ConfigurationException {
        String s = get(key);
        if (s == null)
            throw ConfigurationException.throwConfigException("Error while obtaining String: config doesn't contain key. At: " + getFullPath(key));
        return s;
    }

    @Override
    public Optional<Integer> optInt(String key) {
        return Optional.ofNullable(get(key));
    }

    @Override
    public int getInt(String key) throws ConfigurationException {
        Integer i = get(key);
        if (i == null)
            throw ConfigurationException.throwConfigException("Error while obtaining Integer: config doesn't contain key. At: " + getFullPath(key));
        return i;
    }

    @Override
    public Optional<Long> optLong(String key) {
        return Optional.ofNullable(get(key));
    }

    @Override
    public long getLong(String key) throws ConfigurationException {
        Long l = get(key);
        if (l == null)
            throw ConfigurationException.throwConfigException("Error while obtaining Long: config doesn't contain key. At: " + getFullPath(key));
        return l;
    }

    @Override
    public Optional<Short> optShort(String key) {
        return Optional.ofNullable(get(key));
    }

    @Override
    public short getShort(String key) throws ConfigurationException {
        Short s = get(key);
        if (s == null)
            throw ConfigurationException.throwConfigException("Error while obtaining Short: config doesn't contain key. At: " + getFullPath(key));
        return s;
    }

    @Override
    public Optional<ConfigurationObject> optSection(String key) {
        return Optional.ofNullable(get(key));
    }

    @Override
    public ConfigurationObject getSection(String key) throws ConfigurationException {
        ConfigurationObject obt = get(key);
        if (obt == null)
            throw ConfigurationException.throwConfigException("Error while obtaining section: config doesn't contain key. At: " + getFullPath(key));
        return obt;
    }

    @Override
    public Set<String> keySet() {
        return contents.keySet();
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
        return source;
    }

    @Override
    public void setSource(ResourceHandler handler) {
        this.source = handler;
    }
}
