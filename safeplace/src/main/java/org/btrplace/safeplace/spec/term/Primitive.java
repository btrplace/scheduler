/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.fuzzer.domain.Domain;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Primitive<T> implements Var<Set<T>> {

  private final Type type;

  private final String lbl;

    public Primitive(String name, Type enclosingType) {
        lbl = name;
        type = new SetType(enclosingType);
    }

    @Override
    public Set<T> eval(Context mo, Object... args) {
        Domain<T> dom = mo.domain(label());
        return new HashSet<>(dom.values());
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String label() {
        return lbl;
    }

    @Override
    public String pretty() {
        return label() + ":" + type();
    }

    @Override
    public String toString() {
        return label();
    }
}
