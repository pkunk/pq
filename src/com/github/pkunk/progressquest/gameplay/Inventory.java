package com.github.pkunk.progressquest.gameplay;

import java.util.LinkedHashMap;

/**
 * User: pkunk
 * Date: 2012-01-06
 */
public class Inventory extends LinkedHashMap<String, Integer> {

    public static final String GOLD = "Gold";

    public Inventory() {
        put(GOLD, 0);
    }

    public void add(String item, int quantity) {
        Integer old = get(item);
        if (old == null) {
            old = 0;
        }
        put(item, old + quantity);
    }

    public String getLastItem() {
        String result = null;
        for (Entry<String,Integer> entry : this.entrySet()) {
            result = entry.getKey();
        }
        return result;
    }
}
