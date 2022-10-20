package io.ncmt.serialization.api.v2;

import java.util.Optional;

public interface SerializationNode extends Iterable<Object> {

    Object get();
    <E> E get(int index) throws SerializationException;
    <E> E get(String key) throws SerializationException;

    <E> Optional<E> opt();
    <E> Optional<E> opt(int index);
    <E> Optional<E> opt(String key);

    void put(String key, Object val);
    void add(Object val);
    void remove(Object val);
    void remove(int index);

    boolean checkKey(String key);
    boolean isRecord();
    boolean isArray();
    boolean isAtomic();
    boolean isNull();

    SerializationNode parent();

    String key();
    String path();

}
