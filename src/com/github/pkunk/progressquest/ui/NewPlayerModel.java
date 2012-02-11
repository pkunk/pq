package com.github.pkunk.progressquest.ui;

import com.github.pkunk.progressquest.gameplay.Player;
import com.github.pkunk.progressquest.gameplay.RaceClass;
import com.github.pkunk.progressquest.gameplay.Stats;
import com.github.pkunk.progressquest.gameplay.Traits;
import com.github.pkunk.progressquest.init.Res;
import com.github.pkunk.progressquest.util.PqUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * User: pkunk
 * Date: 2012-02-07
 */
public class NewPlayerModel {

    private static final int STATS_NUM = Stats.STATS_NUM;
    private static final int BASE_STATS_NUM = Stats.BASE_STATS_NUM;

    public static final int COLOR_MIN = 0;
    public static final int COLOR_LOW = 1;
    public static final int COLOR_NORMAL = 2;
    public static final int COLOR_HIGH = 3;
    public static final int COLOR_MAX = 4;

    private String selectedName = null;
    private RaceClass selectedRace = null;
    private RaceClass selectedRole = null;
    private Stats selectedStats = null;

    private Map<String,RaceClass> raceMap = new HashMap<String, RaceClass>(Res.RACES.size());
    private Map<String,RaceClass> roleMap = new HashMap<String, RaceClass>(Res.KLASSES.size());

    private LinkedList<Stats> rollStack = new LinkedList<Stats>();

    public void addRace(RaceClass race) {
        raceMap.put(race.getName(), race);
    }

    public void addRole(RaceClass role) {
        roleMap.put(role.getName(), role);
    }

    public void setName(String name) {
        selectedName = name;
    }

    public void setRace(String race) {
        selectedRace = raceMap.get(race);
    }

    public void setRole(String role) {
        selectedRole = roleMap.get(role);
    }

    public void rollStats() {
        if (selectedStats != null) {
            rollStack.push(selectedStats);
        }

        int[] stats = new int[STATS_NUM];
        for (int i=0; i<BASE_STATS_NUM; i++) {
            stats[i] = 3 + PqUtils.random(6) + PqUtils.random(6) + PqUtils.random(6);
        }
//        stats[Stats.HP_MAX] = PqUtils.random(8) + stats[Stats.CON] / 6;
//        stats[Stats.MP_MAX] = PqUtils.random(8) + stats[Stats.INT] / 6;
        selectedStats = Stats.newStats(stats);
    }

    public void unrollStats() {
        selectedStats = rollStack.pop();
    }

    public boolean hasOldRolls() {
        return rollStack.size() > 0;
    }

    public String[] getStats() {
        String[] result = new String[STATS_NUM];
        for (int i=0; i<BASE_STATS_NUM; i++) {
            result[i] = String.valueOf(selectedStats.get(i));
        }
        return result;
    }

    public String totalStats() {
        int total = selectedStats.getTotal();
        return String.valueOf(total);
    }

    public int totalColor() {
        int total = selectedStats.getTotal();

        if (total >= (63+18)) {
            return COLOR_MAX;
        }
        if (total > (4 * 18)) {
            return COLOR_HIGH;
        }
        if (total <= (63-18)) {
            return COLOR_MIN;
        }
        if (total < (3 * 18)) {
            return COLOR_LOW;
        }
        return COLOR_NORMAL;
    }

    public Player generatePlayer() {
        Traits traits = Traits.newTraits(selectedName, selectedRace.getName(), selectedRole.getName());
        Stats stats = applyBonuses(selectedStats, selectedRace.getStats(), selectedRole.getStats());
        return Player.newPlayer(traits, stats);
    }
    
    private Stats applyBonuses(Stats stats, Stats raceBonuses, Stats roleBonuses) {
        int[] statsArray = new int[STATS_NUM];
        for (int i=0; i<BASE_STATS_NUM; i++) {
            statsArray[i] = stats.get(i) + raceBonuses.get(i) + roleBonuses.get(i);
        }

        statsArray[Stats.HP_MAX] = PqUtils.random(8) + statsArray[Stats.CON] / 6
            + raceBonuses.get(Stats.HP_MAX) + roleBonuses.get(Stats.HP_MAX);

        statsArray[Stats.MP_MAX] = PqUtils.random(8) + statsArray[Stats.INT] / 6
            + raceBonuses.get(Stats.MP_MAX) + roleBonuses.get(Stats.MP_MAX);

        return Stats.newStats(statsArray);
    }
}
