/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec;

import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.spec.term.UserVar;
import org.btrplace.safeplace.spec.term.func.Function;
import org.btrplace.safeplace.spec.type.BoolType;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Fabien Hermenier
 */
public class Constraint implements Function<Boolean> {

  private final String name;

  private final Proposition prop;

    private List<UserVar<?>> args;

    private Class<? extends SatConstraint> impl;

    public Constraint(String name, Proposition prop) {
        this.name = name;
        this.prop = prop;
        args = new ArrayList<>();
    }

    @Override
    public BoolType type() {
        return BoolType.getInstance();
    }

    @Override
    public String id() {
        return name;
    }

    public String pretty() {
        return args.stream().map(UserVar::pretty)
                .collect(Collectors.joining(", ", id() + '(', ") ::= " + proposition()));
    }

    @Override
    public String toString() {
        return id();
    }

    public String toString(List<Constant> values) {
        return values.stream().map(Constant::toString).collect(Collectors.joining(", ", id() + "(", ")"));
    }

    public String signatureToString() {
        return Stream.of(signature()).map(Type::toString).collect(Collectors.joining(", ", id() + "(", ")"));
    }

    @Override
    public Boolean eval(Context mo, Object... values) {
        for (int i = 0; i < this.args.size(); i++) {
            UserVar v = this.args.get(i);
            mo.setValue(v.label(), values[i]);
        }
        return proposition().eval(mo);
    }

    public Proposition proposition() {
        return prop;
    }

    public List<UserVar<?>> args() {
        return args;
    }

    public Constraint args(List<UserVar<?>> args) {
        this.args = args;
        return this;
    }

    @Override
    public Type[] signature() {
        Type[] types = new Type[args.size()];
        for (int i = 0; i < args.size(); i++) {
            types[i] = args.get(i).type();
        }
        return types;

    }

    public Constraint impl(Class<? extends SatConstraint> impl) {
        this.impl = impl;
        return this;
    }

    public Class<? extends SatConstraint> impl() {
        return impl;
    }

    public SatConstraint instantiate(List<Object> args) {
        Exception ex = null;
        if (impl == null) {
            return null;
        }
        for (Constructor<?> c : impl.getConstructors()) {
            try {
                if (c.getParameterTypes().length == args.size()) {
                    return (SatConstraint) c.newInstance(args.toArray());
                }
            } catch (Exception e) {
                ex = e;
            }
        }
        String s = "No constructors compatible with values '" + args + "'";
        if (ex != null) {
            s += ": " + ex.getMessage();
        }
        throw new IllegalArgumentException(s);
    }

    public boolean isSatConstraint() {
        if (impl == null) {
            return false;
        }
        Class<?> c = impl;
        do {
            for (Class<?> i : c.getInterfaces()) {
                if (i.equals(SatConstraint.class)) {
                    return true;
                }
            }
            c = c.getSuperclass();
        } while (c != null);
        return false;
    }
}
