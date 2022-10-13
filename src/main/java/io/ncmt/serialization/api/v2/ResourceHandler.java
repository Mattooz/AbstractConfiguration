package io.ncmt.serialization.api.v2;

import java.io.IOException;

public interface ResourceHandler {

    byte[] readFromSource() throws IOException;
    void writeToSource(byte[] byteArray) throws IOException;

}
