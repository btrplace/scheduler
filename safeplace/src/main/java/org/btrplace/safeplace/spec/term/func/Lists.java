/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.ColType;
import org.btrplace.safeplace.spec.type.ListType;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Lists implements Function<List> {

    @Override
    public Type type() {
        return new SetType(new ListType(null));
    }


    @Override
    public List eval(Context mo, Object... args) {
        Collection<?> c = (Collection<?>) args[0];
        if (c == null) {
            return null;
        }
        List<ArrayList<?>> l = new ArrayList<>();
        l.add(new ArrayList<>(c));
        return l;
    }

    @Override
    public String id() {
        return "lists";
    }

    @Override
    public Type[] signature() {
        return new Type[]{new SetType(null)};
    }

    @Override
    public Type type(List<Term> args) {
        return new SetType(new ListType(((ColType) args.get(0).type()).inside()));
    }

}
