/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.term.UserVar;
import org.btrplace.safeplace.spec.term.Var;
import org.btrplace.safeplace.testing.verification.spec.Context;
import org.btrplace.safeplace.util.AllTuplesGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class ForAll implements Proposition {

  private final List<UserVar<?>> vars;

  private final Term<Set> from;

  private final Proposition prop;

    public ForAll(List<UserVar<?>> vars, Proposition p) {
        this.vars = vars;
        this.from = vars.get(0).getBackend();
        prop = p;
    }

    @Override
    public Proposition not() {
        return new Exists(vars, prop.not());
    }

    @Override
    public Boolean eval(Context m) {
        List<List<Object>> values = new ArrayList<>(vars.size());
        for (int i = 0; i < vars.size(); i++) {
            Collection<Object> o = from.eval(m);
            if (o == null) {
                return null;
            }
            values.add(new ArrayList<>(o));
        }
        AllTuplesGenerator<Object> tg = new AllTuplesGenerator<>(Object.class, values);
        while (tg.hasNext()) {
            Object[] tuple = tg.next();
            for (int i = 0; i < tuple.length; i++) {
                m.setValue(vars.get(i).label(), tuple[i]);
            }
            Boolean r = prop.eval(m);
            if (r == null) {
                return null;
            }
            if (!r) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("!(");
        Iterator<UserVar<?>> ite = vars.iterator();
        while (ite.hasNext()) {
            Var v = ite.next();
            if (ite.hasNext()) {
                b.append(v.label());
                b.append(',');
            } else {
                b.append(v.pretty());
            }
        }
        return b.append(") ").append(prop).toString();
    }

}
