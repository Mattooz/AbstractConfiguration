package io.ncmt.serialization.api.v2.atomizer;

import io.ncmt.serialization.api.v2.SerializationNode;

public interface Constructor {

    <T> T construct(SerializationNode node, Class<T> clazz);

}
