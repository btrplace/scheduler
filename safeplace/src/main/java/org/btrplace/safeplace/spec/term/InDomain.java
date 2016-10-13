package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.Domain;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InDomain<T> extends Primitive<T> {

    public InDomain(String lbl, Type t) {
        super(lbl, t);
    }

    private Set cache = null;
    @Override
    public Set<T> eval(Context mo, Object... args) {
        Domain dom = mo.domain(label());
        if (dom.constant()) {

            if (cache == null) {
                cache = new HashSet(dom.values());
            }
            return cache;
        }
        List s = dom.values();
        if (s == null) {
            throw new UnsupportedOperationException("No domainValue for variable '" + label() + "'");
        }
        return new HashSet(s);
    }

    @Override
    public String toString() {
        return label();
    }
}
