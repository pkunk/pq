package com.github.pkunk.progressquest.gameplay;

import com.github.pkunk.progressquest.util.Vfs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * User: pkunk
 * Date: 2012-01-06
 */
public class Inventory extends LinkedHashMap<String, Integer> {

    public static final String GOLD = "Gold";
    
    private Inventory() {
    }

    public static Inventory newInventory() {
        Inventory inventory = new Inventory();
        inventory.put(GOLD, 0);
        return inventory;
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

    public List<String> saveInventory() {
        List<String> result = new ArrayList<String>(this.size());
        for (Entry<String, Integer> entry : this.entrySet()) {
            result.add(entry.getKey() + Vfs.SEPARATOR + entry.getValue());
        }
        return result;
    }

    public static Inventory loadInventory(List<String> strings) {
        Inventory inventory = new Inventory();
        for (String s : strings) {
            String[] entry = s.split(Vfs.SEPARATOR);
            inventory.put(entry[0], Integer.decode(entry[1]));
        }
        return inventory;
    }
}
