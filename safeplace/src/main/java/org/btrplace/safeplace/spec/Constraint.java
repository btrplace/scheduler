/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.safeplace.spec;

import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.term.Constant;
import org.btrplace.safeplace.spec.term.UserVar;
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
public class Constraint {

    private String name;

    private Proposition prop;

    private List<UserVar> args;

    private Class<? extends SatConstraint> impl;

    public Constraint(String name, Proposition prop) {
        this.name = name;
        this.prop = prop;
        args = new ArrayList<>();
    }

    public BoolType type() {
        return BoolType.getInstance();
    }

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

    public List<UserVar> args() {
        return args;
    }

    public Constraint args(List<UserVar> args) {
        this.args = args;
        return this;
    }

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
        for (Constructor c : impl.getConstructors()) {
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
        Class c = impl;
        do {
            for (Class i : c.getInterfaces()) {
                if (i.equals(SatConstraint.class)) {
                    return true;
                }
            }
            c = c.getSuperclass();
        } while (c != null);
        return false;
    }
}
