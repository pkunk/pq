package com.github.pkunk.progressquest.gameplay;

/**
 * User: pkunk
 * Date: 2012-01-01
 */
public class Traits {
    private String name;
    private String race;
    private String role;
    private int level = 1;

    public Traits(String name, String race, String role) {
        this.name = name;
        this.race = race;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getRace() {
        return race;
    }

    public String getRole() {
        return role;
    }

    public int getLevel() {
        return level;
    }

    public void levelUp() {
        level += 1;
    }
}
