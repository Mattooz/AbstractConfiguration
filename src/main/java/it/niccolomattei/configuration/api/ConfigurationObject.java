package it.niccolomattei.configuration.api;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

public interface ConfigurationObject extends ConfigurationElement {

    /**
     * Retrieves a value from the ConfigurationObject and returns it as a desired type.
     * If no value is present at the given key or path or if it fails to cast to the
     * desired type it will throw a {@link ConfigurationException}.
     * <p>
     * Param key can be either a simple key: {@code "example_key"} or a path to a
     * nested value using a '.' as path separator: {@code "example.key"}
     *
     * @param key a key to a value, syntax explained above
     * @param <E> the type parameter of the desired value
     * @return the value if it is found and no exception are thrown
     * @throws ConfigurationException if it fails to cast or no value is present at a given key
     */
    <E> E obt(String key) throws ConfigurationException;

    /**
     * Retrieves a value from the ConfigurationObject and returns it as a desired type.
     * If no value is present at the give it will return the given default value.
     * Note that this method won't throw an exception if it fails to cast the value to
     * the desired return type.
     * <p>
     * Param key can be either a simple key: {@code "example_key"} or a path to a
     * nested value using a '.' as path separator: {@code "example.key"}
     *
     * @param key          a key to a value, syntax explained above.
     * @param defaultValue a default value that is returned if no value is found at the given key
     * @param <E>          the type parameter of the desired value
     * @return the value if it is found otherwise the give default value
     */
    <E> E opt(String key, E defaultValue);

    /**
     * Puts a key-value pair inside of this object.
     * As {@link #obt(String)} and {@link #opt(String, Object)} the key can be either a path to a key
     * or a simple key.
     * <p>
     * This method will also try to serialize some objects, such as lists. Support for other classes
     * will be added in future classes.
     *
     * @param key   a key to a value
     * @param value a value
     */
    void put(String key, Object value);

    Optional<String> optString(String key);

    String getString(String key) throws ConfigurationException;

    Optional<Integer> optInt(String key);

    int getInt(String key) throws ConfigurationException;

    Optional<Long> optLong(String key);

    long getLong(String key) throws ConfigurationException;

    Optional<Short> optShort(String key);

    short getShort(String key) throws ConfigurationException;

    Optional<ConfigurationObject> optSection(String key);

    ConfigurationObject getSection(String key) throws ConfigurationException;

    Set<String> keySet();

    Object originalFormat();

    @Override
    default String string(StringBuilder builder) {
        for (String key : keySet()) {
            Object o = obt(key);

            if (o instanceof ConfigurationElement)
                ((ConfigurationElement) o).string(builder);
            else
                builder.append(getFullPath(key)).append(": ").append(o).append("\n");
        }

        return builder.toString();
    }

}
