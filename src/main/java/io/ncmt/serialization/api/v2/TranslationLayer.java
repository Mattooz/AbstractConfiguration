package io.ncmt.serialization.api.v2;

import java.util.function.Supplier;

public interface TranslationLayer {

    <SN extends SerializationNode> SN read(byte[] input, Supplier<SN> objectProvider);
    byte[] write(SerializationNode object);
    byte[] writeDefault();

}
