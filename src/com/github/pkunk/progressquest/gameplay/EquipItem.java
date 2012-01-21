package com.github.pkunk.progressquest.gameplay;

/**
 * User: pkunk
 * Date: 2011-12-24
 */
public class EquipItem {
    private final int modifier;
    private final String name;

    public EquipItem(String name, int value) {
        if (name == null) {
            throw new IllegalArgumentException("EquipItem's name cannot be null");
        }

        this.name = name;
        this.modifier = value;
    }

    public String getName() {
        return name;
    }

    public int getMod() {
        return modifier;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + modifier;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EquipItem) {
            return name.equals(((EquipItem)obj).getName())
                   && modifier == ((EquipItem)obj).getMod();
        }
        return false;
    }
}
