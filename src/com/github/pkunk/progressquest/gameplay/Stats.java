package com.github.pkunk.progressquest.gameplay;

import java.util.ArrayList;

/**
 * User: pkunk
 * Date: 2012-01-01
 */
public class Stats extends ArrayList<Integer> {
    public static final int STR  = 0;
    public static final int CON  = 1;
    public static final int DEX  = 2;
    public static final int INT  = 3;
    public static final int WIS  = 4;
    public static final int CHA  = 5;

    public static final int BASE_STATS_NUM = CHA + 1;

    public static final int HP_MAX = 6;
    public static final int MP_MAX = 7;

    public static final String[] label = new String[] {
            "STR",
            "CON",
            "DEX",
            "INT",
            "WIS",
            "CHA",
            "HP max",
            "SP max"
    };

    public static final int STATS_NUM = label.length;

    public Stats(int[] stats) {
        super(STATS_NUM);
        if (STATS_NUM != stats.length) {
            throw new IllegalArgumentException("Number of stats is wrong");
        }
        for (int i=0; i< STATS_NUM; i++) {
            add(stats[i]);
        }
    }

    public void inc(int key, int value) {
        set(key, get(key) + value);
    }

    public void dec(int key, int value) {
        set(key, get(key) - value);
    }
}
