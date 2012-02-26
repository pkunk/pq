package com.github.pkunk.pq.gameplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * User: pkunk
 * Date: 2012-02-01
 */
public class QuestList extends LinkedList<String> {

    private QuestList() {
        super();
    }

    private QuestList(Collection<? extends String> collection) {
        super(collection);
    }

    public static QuestList newQuestList() {
        return new QuestList();
    }

    public List<String> saveQuestList() {
        return new ArrayList<String>(this);
    }

    public static QuestList loadQuestList(List<String> strings) {
        QuestList loaded = new QuestList(strings);
        loaded.remove("");
        return loaded;
    }

}
