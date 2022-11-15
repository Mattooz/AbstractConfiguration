package io.ncmt.common;

import io.ncmt.common.bit.BitVector;
import io.ncmt.common.bit.ByteBitVector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.ncmt.common.FowlerNollVoHash.fnv1a64;

/* Stylized as Rime* */
public class RimestarPHF<T> {


    private final Function<T, byte[]> mapper;
    private PhiL[] perfectHash;
    private final float R_FACTOR = .4f;
    private final float M_FACTOR = 1f;
    private int gRange;

    public RimestarPHF(Function<T, byte[]> mapper) {
        this.mapper = mapper;
    }

    @SafeVarargs
    public final List<List<T>> genPhfExp(T... keys) {
        var r = (int) Math.ceil(keys.length * R_FACTOR);
        var m = (int) Math.ceil(keys.length * M_FACTOR);

        List<List<T>> buckets = Collections
                .synchronizedList(
                        Stream.generate(() -> Collections.synchronizedList(new ArrayList<T>()))
                                .limit(r)
                                .toList());

        Arrays.stream(keys).parallel().forEach(key -> {
            var gHash = (int) Long.remainderUnsigned(fnv1a64(mapper.apply(key)), r);
            buckets.get(gHash).add(key);
        });


        Vector<PhiL> perfectHash = new Vector<>();

        var T = BitVector.synchronizedBitVector(ByteBitVector.ofFixedSize(m));
        var usedL = BitVector.synchronizedBitVector(new ByteBitVector());

        IntStream.iterate(0, ByteBitVector::increment)
                .parallel().mapToObj(bucket -> {
            var rndHash = new AtomicInteger(0);
            var tTemp = T.copy();


        });


        return buckets;
    }

    public static void main(String[] args) {

        String[] testArr = new String[]
                {
                        "a", "me", "mi", "piace", "la", "nutella", "porcodio", "gelato", "ca", "panna", "e", "merendine",
                        "in", "guandità"
                };

        //RimestarPHF<String> stringRimestarPHF = new RimestarPHF<>(String::getBytes);

        //System.out.println(stringRimestarPHF.genPhfExp(testArr));

        long[] d = new long[]{0xf2389353da238da5L};
        System.out.println(Long.toBinaryString(d[0]));
        ByteBitVector byteBitVector = new ByteBitVector(d);
        System.out.println(byteBitVector);

        byte[] b = new byte[] {(byte) 0xff, (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a, (byte) 0xbc, (byte) 0xde, (byte) 0xff};
        var by = new ByteBitVector(b);
        System.out.println(Long.toHexString(by.toLongArray()[0]) + ", " + Long.toHexString(by.toLongArray()[1]));

        long[] a = {0b00110011};
        long[] c = {0b11001100};
        var b1 = new ByteBitVector(a);
        var b2 = new ByteBitVector(c);
        b1.or(b2);
        System.out.println(b1);
    }

    @SuppressWarnings("unchecked")
    public void generatePhf(T... keys) {
        var r = (int) Math.ceil(keys.length * R_FACTOR);
        var m = (int) Math.ceil(keys.length * M_FACTOR);

        gRange = r;

        perfectHash = new PhiL[r];
        Object[][] buckets = new Object[r][];
        for (var i = 0; i < r; i++) buckets[i] = new Object[0];

        for (var key : keys) {
            var gHash = Integer.remainderUnsigned((int) fnv1a64(mapper.apply(key)), r);

            if (buckets[gHash].length == 0) buckets[gHash] = new Object[]{key};
            else {
                buckets[gHash] = Arrays.copyOf(buckets[gHash], buckets[gHash].length + 1);
                buckets[gHash][buckets[gHash].length - 1] = key;
            }
        }

        var T = ByteBitVector.ofFixedSize(m);
        var usedL = new ByteBitVector();

        for (var i = 0; i < r; i++) {
            var bucket = buckets[i];

            var rndHash = 0;
            var tTemp = T.copy();
            for (var l = 0; l < bucket.length; l++) {
                if (usedL.getAsBoolean(l)) {
                    l = -1;
                    rndHash++;
                    tTemp = T.copy();
                    continue;
                }

                var phiL = new PhiL(rndHash, m);

                T key = (T) bucket[l];

                if (tTemp.getAsBoolean(phiL.applyAsInt(mapper.apply(key)))) {
                    l = -1;
                    rndHash++;
                    tTemp = T.copy();
                    continue;
                }

                tTemp.set(phiL.applyAsInt(mapper.apply(key)), true);
            }
            T.or(tTemp);
            perfectHash[i] = new PhiL(rndHash, m);
        }
    }

    public int hash(T key) {
        return perfectHash[(int) Long.remainderUnsigned(fnv1a64(mapper.apply(key)), gRange)].applyAsInt(mapper.apply(key));
    }

    public record PhiL(int offset, int m) implements ToIntFunction<byte[]> {

        @Override
        public int applyAsInt(byte[] bytes) {
            return (int) Long.remainderUnsigned(new Xoshiro256StarStar(fnv1a64(bytes) + offset).next(), m);
        }

    }

}
