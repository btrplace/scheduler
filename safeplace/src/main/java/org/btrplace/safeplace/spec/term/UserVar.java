/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term;

import org.btrplace.safeplace.spec.type.ColType;
import org.btrplace.safeplace.spec.type.SetType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.fuzzer.domain.Domain;
import org.btrplace.safeplace.testing.verification.spec.Context;
import org.btrplace.safeplace.util.AllTuplesGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class UserVar<T> implements Var<T> {

  private final Term<?> backend;

  private final String lbl;

  private final String op;

    public UserVar(String lbl, String op, Term<?> backend) {
        this.backend = backend;
        this.lbl = lbl;
        this.op = op;
    }

    @Override
    public String label() {
        return lbl;
    }

    @Override
    public Type type() {
        switch (op) {
            case ":":
            case "/:":
                return ((ColType) backend.type()).inside();
            case "<:":
            case "/<:":
                return backend.type();
            case "<<:":
            case "/<<:":
                return new SetType(backend.type());
            default:
                throw new IllegalArgumentException(op);

        }
    }

    @Override
    public String pretty() {
        return label() + " " + op + " " + backend;
    }

    public Term getBackend() {
        return backend;
    }

    @Override
    public T eval(Context m, Object... args) {
        return (T) m.getValue(label());
    }

    @Override
    public String toString() {
        return label();
    }

    public List<Constant> domain(Context mo) {
        Collection<?> col = (Collection<?>) backend.eval(mo);
        if ("<:".equals(op) || "/<:".equals(op)) {
            List<Object> s = new ArrayList<>(col);
            List<List<Object>> tuples = s.stream().map(o -> s).collect(Collectors.toList());
            AllTuplesGenerator<Object> tg = new AllTuplesGenerator<>(Object.class, tuples);
            Set<Constant> res = new HashSet<>();
            while (tg.hasNext()) {
                Object[] tuple = tg.next();
                res.add(new Constant(new HashSet<>(Arrays.asList(tuple)), backend.type()));
            }
            return new ArrayList<>(res);
        }
        List<Constant> s = new ArrayList<>();
        for (Object o : col) {
            s.add(new Constant(o, type()));
        }
        return s;
    }

    public Object pick(Domain d) {
        switch (op) {
            case ":":
                return d.randomValue();
            case "<:":
                return new HashSet<>(d.randomSubset());
            case "<<:":
                return new HashSet<>(d.randomPacking());
            default:
                throw new IllegalArgumentException("Cannot pick a value inside " + type());
        }

    }

}
