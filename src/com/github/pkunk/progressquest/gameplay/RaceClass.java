package com.github.pkunk.progressquest.gameplay;

/**
 * User: pkunk
 * Date: 2011-12-24
 */
public class RaceClass {
    private String name;
    private Stats stats;

    public RaceClass(String name, Stats stats) {
        this.name = name;
        this.stats = stats;
    }

    public String getName() {
        return name;
    }

    public Stats getStats() {
        return stats;
    }
}
