package com.github.pkunk.progressquest.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: pkunk
 * Date: 2012-01-02
 */
public class ResList<E> extends ArrayList<E> {
    public ResList(int capacity) {
        super(capacity);
    }

    public ResList() {
    }

    public ResList(Collection<? extends E> es) {
        super(es);
    }

    public E pick() {
        return get(PqUtils.random(size()));
    }

    public E pickLow() {
        return get(PqUtils.randomLow(size()));
    }
}
