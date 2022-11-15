package io.ncmt.common;

public class FowlerNollVoHash {

    private final static long FNV_PRIME = 0x00000100000001B3L;
    private final static long FNV_BASIS = 0xcbf29ce484222325L;
    
    public static long fnv1a64(byte[] input) {
        var hash = FNV_BASIS;

        for (byte b : input) {
            hash ^= b;
            hash *= FNV_PRIME;
        }

        return hash;
    }

}
