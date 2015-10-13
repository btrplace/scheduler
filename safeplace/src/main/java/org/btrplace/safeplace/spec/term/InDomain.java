package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.verification.spec.Context;

import java.util.Set;

/**
 * Created by fhermeni on 14/09/2015.
 */
public class InDomain<T> extends Primitive<T> {

    public InDomain(String lbl, Type t) {
        super(lbl, t);
    }

    @Override
    public Set<T> eval(Context mo, Object... args) {
        Set s = mo.domain(label());
        if (s == null) {
            throw new UnsupportedOperationException("No domain for variable '" + label() + "'");
        }
        return s;
    }

    @Override
    public String toString() {
        return label();
    }
}
