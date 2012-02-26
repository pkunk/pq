package com.github.pkunk.pq.gameplay;

import com.github.pkunk.pq.util.Vfs;

import java.util.ArrayList;
import java.util.List;

/**
 * User: pkunk
 * Date: 2012-01-01
 */
public class Traits {
    private String name;
    private String race;
    private String role;
    private int level;
    
    private Traits() {
    }

    public static Traits newTraits(String name, String race, String role) {
        Traits traits = new Traits();
        traits.name = name;
        traits.race = race;
        traits.role = role;
        traits.level = 1;
        return traits;
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

    public List<String> saveTraits() {
        List<String> result = new ArrayList<String>();

        result.add("name" + Vfs.EQ + name);
        result.add("race" + Vfs.EQ + race);
        result.add("role" + Vfs.EQ + role);
        result.add("level" + Vfs.EQ + level);

        return result;
    }

    public static Traits loadTraits(List<String> strings) {
        Traits traits = new Traits();
        for (String s : strings) {
            String entry[] = s.split(Vfs.EQ);
            if ("name".equals(entry[0])) {
                traits.name = entry[1];
            } else if ("race".equals(entry[0])) {
                traits.race = entry[1];
            } else if ("role".equals(entry[0])) {
                traits.role = entry[1];
            } else if ("level".equals(entry[0])) {
                traits.level = Integer.valueOf(entry[1]);
            }
        }
        return traits;
    }
}
