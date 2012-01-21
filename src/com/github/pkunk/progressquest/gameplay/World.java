package com.github.pkunk.progressquest.gameplay;

import com.github.pkunk.progressquest.init.Res;
import com.github.pkunk.progressquest.util.PqUtils;

import java.util.Locale;

/**
 * User: pkunk
 * Date: 2012-01-08
 */
public class World {
    
    static class MonsterTask {
        MonsterTask(String description, int level) {
            this.description = description;
            this.level = level;
        }

        String description;
        int  level;
    } 

    public static MonsterTask monsterTask(Game game, int level) {
        boolean definite = false;
        for (int i = level; i >= 1; i--) {
            if (PqUtils.odds(2, 5))
                level += PqUtils.randSign();
        }
        if (level < 1) level = 1;
        // level = level of puissance of opponent(s) we"ll return

        Monster monster;
        String name;
        int lev;
        if (PqUtils.odds(1, 25)) {
            // Use an NPC every once in a while
            name = " " + Res.RACES.pick().getName();
            if (PqUtils.odds(1, 2)) {
                name = "passing" + name + " " + Res.KLASSES.pick().getName();
            } else {
                name = Res.TITLES.pickLow() + " " + generateName() + " the" + name;
                definite = true;
            }
            lev = level;
            monster = new Monster(name, level, "*");
        } else if (game.questmonster != null && PqUtils.odds(1, 4)) {
            // Use the quest monster
            monster = Res.MONSTERS.get(game.questmonsterindex);
            lev = monster.getLevel();
        } else {
            // Pick the monster out of so many random ones closest to the level we want
            monster = Res.MONSTERS.pick();
            lev = monster.getLevel();
            for (int i = 0; i < 5; i++) {
                Monster m1 = Res.MONSTERS.pick();
                if (Math.abs(level-m1.getLevel()) < Math.abs(level-lev)) {
                    monster = m1;
                    lev = m1.getLevel();
                }
            }
        }

        String result = monster.getName();
        game.task = Task.killTask(monster);

        int qty = 1;
        if (level-lev > 10) {
            // lev is too low. multiply...
            qty = (int)Math.floor((level + PqUtils.random(lev)) / Math.max(lev,1));
            if (qty < 1) qty = 1;
            level = (int)Math.floor(level / qty);
        }

        if ((level - lev) <= -10) {
            result = "imaginary " + result;
        } else if ((level-lev) < -5) {
            int i = 10+(level-lev);
            i = 5-PqUtils.random(i + 1);
            result = sick(i,young((lev-level)-i,result));
        } else if (((level-lev) < 0) && (PqUtils.random(2) == 1)) {
            result = sick(level - lev, result);
        } else if (((level-lev) < 0)) {
            result = young(level - lev, result);
        } else if ((level-lev) >= 10) {
            result = "messianic " + result;
        } else if ((level-lev) > 5) {
            int i = 10-(level-lev);
            i = 5-PqUtils.random(i + 1);
            result = big(i,special((level-lev)-i,result));
        } else if (((level-lev) > 0) && (PqUtils.random(2) == 1)) {
            result = big(level-lev,result);
        } else if (((level-lev) > 0)) {
            result = special(level-lev,result);
        }

        lev = level;
        level = lev * qty;

        if (!definite) result = PqUtils.indefinite(result, qty);
        return new MonsterTask(result, level);
    }

    private static String sick(int m, String s) {
        m = 6 - Math.abs(m);
        return prefix(new String[]{"dead","comatose","crippled","sick","undernourished"}, m, s, " ");
    }

    private static String young(int m, String s) {
        m = 6 - Math.abs(m);
        return prefix(new String[]{"foetal","baby","preadolescent","teenage","underage"}, m, s, " ");
    }


    private static String big(int m, String s) {
        return prefix(new String[]{"greater","massive","enormous","giant","titanic"}, m, s, " ");
    }


    private static String special(int m, String s) {
        if (s.contains(" "))
            return prefix(new String[]{"veteran","cursed","warrior","undead","demon"}, m, s, " ");
        else
            return prefix(new String[]{"Battle-","cursed ","Were-","undead ","demon "}, m, s, "");
    }

    private static String generateName() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i <= 5; i++) {
            result.append(Res.PARTS.get(i%3).pick());
        }
        String first =  result.substring(0,1).toUpperCase(Locale.US);
        result.replace(0,1, first);
        return result.toString();
    }

    public static String namedMonster(int level) {
        int lev = 0;
        String result = null;
        for (int i=0; i<5; i++) {
            Monster m = Res.MONSTERS.pick();
            if (result == null || (Math.abs(level-m.getLevel())) < Math.abs(level-lev)) {
                result = m.getName();
                lev = m.getLevel();
            }
        }
        return generateName() + " the " + result;
    }

    public static String impressiveGuy() {
        return Res.IMPRESSIVE_TITLES.pick() +
                (PqUtils.random(2) == 0 ? " of the " + Res.RACES.pick().getName() : " of " + generateName());
    }

    private static String prefix(String[] array, int m, String s, String sep) {
        if (sep == null) sep = " ";
        m = Math.abs(m);
        if (m < 1 || m > array.length) return s;  // In case of screwups
        return array[m-1] + sep + s;
    }

    public static String getSpecialItem() {
        return getInterestingItem() + " of " + Res.ITEM_OFS.pick();
    }

    public static String getInterestingItem() {
        return Res.ITEM_ATTRIB.pick() + " " + Res.SPECIAL_ITEMS.pick();
    }

    public static String getBoringItem() {
        return Res.BORING_ITEMS.pick();
    }
}
