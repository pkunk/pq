package com.github.pkunk.pq.gameplay;

import com.github.pkunk.pq.util.Roman;
import com.github.pkunk.pq.util.Vfs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * User: pkunk
 * Date: 2012-01-02
 */
public class SpellBook extends LinkedHashMap<String, Roman> {

    private SpellBook() {
    }

    public static SpellBook newSpellBook() {
        return new SpellBook();
    }

    void addR (String key, int value) {
        Roman oldValue = get(key);
        if (oldValue == null) {
            oldValue = new Roman(0);
        }

        Roman newValue = new Roman(oldValue.getInt() + value);

        put(key, newValue);
    }

    public List<String> saveSpellBook() {
        List<String> result = new ArrayList<String>(this.size());
        for (Entry<String, Roman> entry : this.entrySet()) {
            result.add(entry.getKey() + Vfs.SEPARATOR + entry.getValue().getInt());
        }
        return result;
    }

    public static SpellBook loadSpellBook(List<String> strings) {
        SpellBook spellBook = new SpellBook();
        for (String s : strings) {
            if (s.length() == 0) {
                continue;
            }
            String[] entry = s.split(Vfs.SEPARATOR);
            spellBook.put(entry[0], new Roman(Integer.valueOf(entry[1])));
        }
        return spellBook;
    }
}
