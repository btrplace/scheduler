/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class ExplodedSet implements Term<Set> {

  private final List<Term> terms;

  private final Type t;

    public ExplodedSet(List<Term> ts, Type enclType) {
        this.terms = ts;
        t = new SetType(enclType);
    }

    @Override
    public Set eval(Context mo, Object... args) {
        return terms.stream().map(x -> x.eval(mo)).collect(Collectors.toSet());
    }

    @Override
    public Type type() {
        return t;
    }

    @Override
    public String toString() {
        return terms.stream()
                .map(Term::toString)
                .collect(Collectors.joining(", ", "{", "}"));
    }

}
