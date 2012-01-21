package com.github.pkunk.progressquest.gameplay;

import com.github.pkunk.progressquest.util.Roman;

import java.util.LinkedHashMap;

/**
 * User: pkunk
 * Date: 2012-01-02
 */
public class SpellBook extends LinkedHashMap<String, Roman> {
    void addR (String key, int value) {
        Roman oldValue = get(key);
        if (oldValue == null) {
            oldValue = new Roman(0);
        }

        Roman newValue = new Roman(oldValue.getInt() + value);

        put(key, newValue);
    }
}
