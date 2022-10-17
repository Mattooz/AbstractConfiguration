package io.ncmt.serialization.api.v2.atomizer;

import io.ncmt.serialization.api.v2.SerializationNode;

public interface Atomizer<SN extends SerializationNode> {

    SN atomize(Object object);
    <T> T construct(SN node, Class<T> clazz);

}
