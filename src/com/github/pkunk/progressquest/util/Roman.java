package com.github.pkunk.progressquest.util;

/**
 * User: pkunk
 * Date: 2012-01-02
 */
public final class Roman {
    private final int integer;
    private final String roman;

    public Roman(int value) {
        this.roman = toRoman(value);
        this.integer = value;
    }

    public int getInt() {
        return integer;
    }

    public String getRoman() {
        return roman;
    }

    @Override
    public int hashCode() {
        return integer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Roman) {
            return integer == ((Roman)obj).integer;
        }
        return false;
    }

    @Override
    public String toString() {
        return roman;
    }

    private static final char[] R = {'M', 'D', 'C', 'L', 'X', 'V', 'I'};

    private static final int MAX = 1000; // value of R[0], must be a power of 10

    private static final int[][] DIGITS = {
            {},{0},{0,0},{0,0,0},{0,1},{1},
            {1,0},{1,0,0},{1,0,0,0},{0,2}};

    public static String toRoman(int value) {
        if (value < 0) {
            throw new IllegalArgumentException(value + "is wrong - Romans are always positive");
        }
        if (value == 0) {
            return "N";
        }
        
        StringBuilder sb = new StringBuilder();
        
        while (value > MAX) {
            value -= MAX;
            sb.append(R[0]);
        }
        
        for (int i = 0, m = MAX; m > 0; m /= 10, i += 2) {
            int[] d = DIGITS[(value/m)%10];
            for (int n: d) sb.append(R[i-n]);
        }
        return sb.toString();
    }
}
