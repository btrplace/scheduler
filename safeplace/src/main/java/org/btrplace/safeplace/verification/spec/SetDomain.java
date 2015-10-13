package org.btrplace.safeplace.verification.spec;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by fhermeni on 14/09/2015.
 */
public class SetDomain<T> implements Domain<T> {

    private Set<T> set;
    private String type;

    public SetDomain(String t, Set<T> s) {
        set = Collections.unmodifiableSet(s);
        type = t;
    }

    @Override
    public Set<T> values() {
        return set;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Domain clone() {
        return new SetDomain<>(type, new HashSet<>(set));
    }
}
