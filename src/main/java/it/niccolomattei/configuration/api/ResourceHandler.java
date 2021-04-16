package it.niccolomattei.configuration.api;

import java.io.IOException;

public interface ResourceHandler {

    byte[] toByteArray() throws IOException;
    void toOriginalFormat(byte[] byteArray) throws IOException;

}
