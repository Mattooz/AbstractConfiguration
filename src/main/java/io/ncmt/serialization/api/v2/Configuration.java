package io.ncmt.serialization.api.v2;

public class Configuration {

    private static String PATH_DELIM = "\\.";

    public static String getPathDelim() {
        return PATH_DELIM;
    }

    public static void setPathDelim(String pathDelim) {
        PATH_DELIM = pathDelim;
    }
}
