package com.github.pkunk.pq.util;

/**
 * User: pkunk
 * Date: 2012-01-19
 */
public class ProgressCounter {
    private int current = 0;
    private int max = 0;

    public ProgressCounter(int max) {
        this.current = 0;
        this.max = max;
    }

    public void reset(int max, int current) {
        this.current = current;
        this.max = max;
    }

    public void reset(int max) {
        reset(max, 0);
    }

    public void increment(int value) {
        current += value;
    }

    public int getCurrent() {
        return Math.min(current, max);
    }

    public int getMax() {
        return max;
    }

    public boolean done() {
        return current >= max;
    }
}
