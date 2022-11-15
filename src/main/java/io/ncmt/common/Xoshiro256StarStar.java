package io.ncmt.common;

public class Xoshiro256StarStar {

    static class Splitmix64 {
        private long state;

        public Splitmix64(long seed) {
            this.state = seed;
        }

        public long next() {
            long z = (state += 0x9e3779b97f4a7c15L);
            z = (z ^ (z >> 30)) * 0xbf58476d1ce4e5b9L;
            z = (z ^ (z >> 27)) * 0x94d049bb133111ebL;
            return z ^ (z >> 31);
        }

    }

    private final long[] state;
    
    public Xoshiro256StarStar(long seed) {
        this.state = new long[4];

        var splitmix64 = new Splitmix64(seed);
        this.state[0] = splitmix64.next();
        this.state[1] = splitmix64.next();
        this.state[2] = splitmix64.next();
        this.state[3] = splitmix64.next();
    }

    private long rotl(long x, int k) {
        return (x << k) | (x >>> 64 - k);
    }

    public long next() {
        if(state == null) throw new RuntimeException("State has not been initialized");

        var result = rotl(state[1] * 5, 7) * 9;
        var tmp = state[1] << 17;

        state[2] ^= state[0];
        state[3] ^= state[1];
        state[1] ^= state[2];
        state[0] ^= state[3];

        state[2] ^= tmp;
        state[3] = rotl(state[3], 45);

        return result;
    }

}
