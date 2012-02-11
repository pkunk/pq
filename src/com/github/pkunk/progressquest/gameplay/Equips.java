package com.github.pkunk.progressquest.gameplay;

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

    public static final String STARTING_WEAPON = "Sharp Rock";

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

        equips.set(WEAPON, STARTING_WEAPON);

        return equips;
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
