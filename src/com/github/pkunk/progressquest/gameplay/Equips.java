package com.github.pkunk.progressquest.gameplay;

import com.github.pkunk.progressquest.util.ResList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: pkunk
 * Date: 2012-01-06
 */
public class Equips extends ArrayList<String> {

    public static final int WEAPON  = 0;
    public static final int SHIELD  = 1;

    public static final String[] label = new String[] {
            "Weapon",
            "Shield",
            "Helm",
            "Hauberk",
            "Brassairts",
            "Vambraces",
            "Gauntlets",
            "Gambeson",
            "Cuisses",
            "Greaves",
            "Sollerets"
    };

    public static final int EQUIP_NUM = label.length;

    private Equips() {
        super(EQUIP_NUM);
    }

    private Equips(Collection<? extends String> collection) {
        super(collection);
    }

    public static Equips newEquips() {
        Equips equips = new Equips();
        for (int i=0; i< EQUIP_NUM; i++) {
            equips.add("");
        }
        return equips;
    }

    public static EquipItem lPick(ResList<EquipItem> list, int goal) {
        EquipItem result = list.pick();
        for (int i = 1; i <= 5; i++) {
            int best = result.getMod();
            EquipItem s = list.pick();
            int b1 = s.getMod();
            if (Math.abs(goal - best) > Math.abs(goal - b1))
                result = s;
        }
        return result;
    }

    public List<String> saveEquips() {
        return new ArrayList<String>(this);
    }

    public static Equips loadEquips(List<String> strings) {
        Equips loaded = new Equips(strings);
        while (loaded.size() < EQUIP_NUM) {
            loaded.add("");
        }
        return loaded;
    }
}
