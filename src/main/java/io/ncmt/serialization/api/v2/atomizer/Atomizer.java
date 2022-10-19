package io.ncmt.serialization.api.v2.atomizer;

import io.ncmt.serialization.api.v2.SerializationNode;

import java.util.Arrays;
import java.util.Collection;

public interface Atomizer<SN extends SerializationNode> {

    Collection<Class<?>> ATOMIC_TYPES = Arrays.asList(new Class<?>[] {
            byte.class, short.class, int.class, long.class,
            float.class, double.class,
            String.class, char.class,
            boolean.class,

            byte[].class, short[].class, int[].class, long[].class,
            float[].class, double[].class,
            String[].class, char[].class,
            boolean[].class,

    });


    SN atomize(Object object);

}
