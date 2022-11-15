package io.ncmt.common.bit;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ByteBitVector implements BitVector {

    public static final long UNIT_BIT_LENGTH = 8;
    private byte[] data;
    long fixedBitSize = -1;

    public ByteBitVector(int initialSize) {
        data = new byte[initialSize];
    }

    public ByteBitVector(byte[] data) {
        this.data = data;
    }

    public static int increment(int a) {
        return a + 1;
    }

    public static long mapByteToLong(byte b, int position) {
        if (position > 8) return 0;
        return (b & 0xFFL) << 56 - (position * 8);
    }

    public static long mapByteArrayToLong(byte[] bytes) {
        return IntStream.iterate(0, i -> i + 1)
                .limit(bytes.length > 8 ? Long.BYTES : bytes.length)
                .mapToLong(i -> mapByteToLong(bytes[i], i))
                .reduce(0, (l, r) -> l | r);
    }

    private static Byte[] mapLongToByteArray(long item) {
        return IntStream.iterate(0, i -> i + 1)
                .limit(Long.BYTES)
                .mapToObj(i -> (byte) (Long.reverse(item) >>> (i * UNIT_BIT_LENGTH) & 0xff))
                .toArray(Byte[]::new);
    }

    public ByteBitVector(long[] data) {
        this.data = unboxByteArray(
                IntStream.iterate(data.length - 1, i -> i - 1)
                        .limit(data.length)
                        .mapToObj(i -> mapLongToByteArray(data[i]))
                        .flatMap(Arrays::stream)
                        .toArray(Byte[]::new)
        );
    }

    private static byte[] unboxByteArray(Byte[] boxed) {
        byte[] bytes = new byte[boxed.length];
        for (var i = 0; i < bytes.length; i++) bytes[i] = boxed[i];
        return bytes;
    }


    public ByteBitVector() {
        data = new byte[1];
    }

    @Override
    public void set(long index, boolean value) {
        if (index >= size() && fixedBitSize != -1) throw new IllegalArgumentException();
        if (index >= size()) data = Arrays.copyOf(data, (int) (index / UNIT_BIT_LENGTH) + 1);
        data[(int) (index / UNIT_BIT_LENGTH)] |= ((value ? 1 : 0) << (index % UNIT_BIT_LENGTH));
    }

    @Override
    public int getAsNumber(long index) {
        if (index >= size()) throw new IndexOutOfBoundsException();
        return (data[(int) (index / UNIT_BIT_LENGTH)] >>> (index % UNIT_BIT_LENGTH)) & 0x1;
    }

    @Override
    public void or(BitVector anotherVector) {
            var l = Math.min(anotherVector.size(), size());
            IntStream.iterate(0, ByteBitVector::increment)
                    .limit(l)
                    .parallel()
                    .forEach(i -> orSet(i, anotherVector.getAsBoolean(i)));
    }

    public void orSet(long index, boolean value) {
        if(value) set(index, true);
    }

    @Override
    public boolean getAsBoolean(long index) {
        return getAsNumber(index) == 1;
    }

    @Override
    public long size() {
        return data.length * UNIT_BIT_LENGTH;
    }

    @Override
    public long[] toLongArray() {
        return IntStream.iterate(0, ByteBitVector::increment)
                .limit(data.length % Long.BYTES != 0 ? (data.length / Long.BYTES) + 1 : data.length / Long.BYTES)
                .mapToLong(i -> toLongArrayStep(this.data, i))
                .toArray();
    }

    private static long toLongArrayStep(byte[] data, int i) {
        var remaining = data.length - i * Long.BYTES;
        var step = remaining >= 8 ? i * Long.BYTES + 8 : i * Long.BYTES + remaining;
        return mapByteArrayToLong(Arrays.copyOfRange(data, i * Long.BYTES, step));
    }

    @Override
    public <T extends BitVector> T convertVector(Function<long[], T> identity) {
        return identity.apply(toLongArray());
    }

    public static ByteBitVector ofFixedSize(long bitSize) {
        var res = new ByteBitVector((int) ((bitSize / UNIT_BIT_LENGTH) + 1));
        res.fixedBitSize = bitSize;
        return res;
    }

    @Override
    public String toString() {
        return IntStream.iterate(0, ByteBitVector::increment)
                .limit(size())
                .mapToObj(index -> ((data[(int) (index / UNIT_BIT_LENGTH)] >>> index % UNIT_BIT_LENGTH) & 1) == 1 ? "1" : "0")
                .collect(Collectors.joining());
    }

    @Override
    public ByteBitVector copy() {
        ByteBitVector clone = new ByteBitVector();
        clone.data = new byte[data.length];
        clone.fixedBitSize = fixedBitSize;
        System.arraycopy(data, 0, clone.data, 0, data.length);
        return clone;
    }


}
