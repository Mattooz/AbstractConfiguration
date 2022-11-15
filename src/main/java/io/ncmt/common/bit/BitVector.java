package io.ncmt.common.bit;

import java.util.function.Function;
import java.util.function.Supplier;

public interface BitVector {

    void set(long index, boolean value);
    int getAsNumber(long index);
    boolean getAsBoolean(long index);
    void or(BitVector anotherVector);
    BitVector copy();

    long size();

    long[] toLongArray();

    <T extends BitVector> T convertVector(Function<long[], T> identity);

    static SynchronizedBitVector synchronizedBitVector(BitVector anotherVector) {
        return new SynchronizedBitVector(anotherVector.copy());
    }

    class SynchronizedBitVector implements BitVector {

        protected SynchronizedBitVector(BitVector encapsulated) {
            this.contained = encapsulated;
        }

        public BitVector contained;

        @Override
        public synchronized void set(long index, boolean value) {
            contained.set(index, value);
        }

        @Override
        public synchronized int getAsNumber(long index) {
            return contained.getAsNumber(index);
        }

        @Override
        public synchronized boolean getAsBoolean(long index) {
            return contained.getAsBoolean(index);
        }

        @Override
        public synchronized void or(BitVector anotherVector) {
            contained.or(anotherVector);
        }

        @Override
        public synchronized SynchronizedBitVector copy() {
            return BitVector.synchronizedBitVector(contained);
        }

        @Override
        public synchronized long size() {
            return contained.size();
        }

        @Override
        public synchronized long[] toLongArray() {
            return contained.toLongArray();
        }

        @Override
        public synchronized <T extends BitVector> T convertVector(Function<long[], T> identity) {
            return contained.convertVector(identity);
        }
    }

}
