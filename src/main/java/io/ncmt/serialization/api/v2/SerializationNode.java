package io.ncmt.serialization.api.v2;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public interface SerializationNode extends Iterable<Object> {

    Collection<Class<?>> ATOMIC_TYPES = Arrays.asList(new Class<?>[] {
            byte.class, short.class, int.class, long.class,
            float.class, double.class,
            String.class, char.class,
            boolean.class
    });

    Collection<Class<?>> ARRAY_ATOMIC_TYPES = Arrays.asList(new Class<?>[] {
            byte[].class, short[].class, int[].class, long[].class,
            float[].class, double[].class,
            String[].class, char[].class,
            boolean[].class
    });

    <E> E get(Class<E> eClass);
    <E> E get(int index, Class<E> eClass) throws SerializationException;
    <E> E get(String key, Class<E> eClass) throws SerializationException;

    <E> Optional<E> opt(Class<E> eClass);
    <E> Optional<E> opt(int index, Class<E> eClass);
    <E> Optional<E> opt(String key, Class<E> eClass);

    void put(String key, Object val);
    void add(Object val);
    void set(Object atomic);
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
