package it.niccolomattei.configuration.api;

public interface ConfigurationList extends ConfigurationElement, Iterable<Object> {

    void put(Object o);

    void putAll(Object[] o);

    void putAll(Iterable<?> o);

    <E> E get(int index) throws ConfigurationException;

    <E> E opt(int index, E defaultValue);

    int size();

    Object originalFormat();

    @Override
    default String string(StringBuilder builder) {
        for(int i = 0; i < size(); i++) {
            Object o = get(i);
            if(o instanceof ConfigurationElement)
                ((ConfigurationElement) o).string(builder);
            else
                builder.append(getFullPath()).append("[").append(i).append("]").append(": ").append(o).append("\n");
        }

        return builder.toString();
    }

}
