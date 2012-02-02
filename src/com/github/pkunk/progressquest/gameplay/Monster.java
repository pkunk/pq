package com.github.pkunk.progressquest.gameplay;

import com.github.pkunk.progressquest.util.Vfs;

/**
 * User: pkunk
 * Date: 2012-01-02
 */
public final class Monster {
    private final String name;
    private final int level;
    private final String loot;


    public Monster(String name, int level, String loot) {
        if (name == null) {
            throw new IllegalArgumentException("Monster's name cannot be null");
        }
        if (loot == null) {
            throw new IllegalArgumentException("Monster's loot cannot be null");
        }

        this.name = name;
        this.level = level;
        this.loot = loot;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public String getLoot() {
        return loot;
    }

    public String saveMonster() {
        String result = name + Vfs.SEPARATOR
                      + level + Vfs.SEPARATOR
                      + loot;
        return result;
    }

    public static Monster loadMonster(String string) {
        String[] strings = string.split(Vfs.SEPARATOR);
        String name = strings[0];
        int level = Integer.valueOf(strings[1]);
        String loot = strings[2];
        return new Monster(name, level, loot);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + name.hashCode();
        result = 31 * result + level;
        result = 31 * result + loot.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Monster) {
            Monster monster = (Monster) obj;
            return name.equals(monster.name)
                    && level == monster.level
                    && loot.equals(monster.loot);
        }
        return false;
    }
}
