package com.github.pkunk.pq.gameplay;

import java.util.ArrayList;
import java.util.List;

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

    private Stats(int capacity) {
        super(capacity);
    }

    public static Stats newStats(int[] statsArray) {
        if (STATS_NUM != statsArray.length) {
            throw new IllegalArgumentException("Number of stats is wrong");
        }

        Stats stats = new Stats(STATS_NUM);
        for (int i=0; i< STATS_NUM; i++) {
            stats.add(statsArray[i]);
        }
        return stats;
    }

    public void inc(int key, int value) {
        set(key, get(key) + value);
    }

    public void dec(int key, int value) {
        set(key, get(key) - value);
    }

    public static Stats newBonuses(int... bonuses) {

        int[] stats = new int[STATS_NUM];

        for (int i : bonuses) {
            stats[i] = 1;
        }

        return newStats(stats);
    }

    public int getTotal() {
        int total = 0;
        for (int i=0; i<BASE_STATS_NUM; i++) {
            total += get(i);
        }
        return total;
    }

    public List<String> saveStats() {
        List<String> result = new ArrayList<String>(STATS_NUM);
        for (Integer stat : this) {
            result.add(String.valueOf(stat));
        }
        return result;
    }

    public static Stats loadStats(List<String> strings) {
        int[] array = new int[strings.size()];
        for (int i=0; i<array.length; i++) {
            array[i] = Integer.valueOf(strings.get(i));
        }
        return newStats(array);
    }
}
