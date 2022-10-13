package io.ncmt.serialization.api.v2;

import java.util.Optional;

public interface SerializationNode extends Iterable<Object> {

    <E> E get();
    <E> E get(int index) throws SerializationException;
    <E> E get(String key) throws SerializationException;

    <E> Optional<E> opt();
    <E> Optional<E> opt(int index);
    <E> Optional<E> opt(String key);

    void put(String key, Object val);
    void add(Object val);

    boolean isRecord();
    boolean isArray();
    boolean isAtomic();
    boolean isNull();

    SerializationNode parent();

    void setPathDelim(String delim);
    String getPathDelim();

    String path();

}
