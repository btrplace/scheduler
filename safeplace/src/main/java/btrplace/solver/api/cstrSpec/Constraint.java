/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
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

package btrplace.solver.api.cstrSpec;

import btrplace.model.constraint.SatConstraint;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.term.Var;
import btrplace.solver.api.cstrSpec.spec.term.func.Function;
import btrplace.solver.api.cstrSpec.spec.type.BoolType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;
import edu.emory.mathcs.backport.java.util.Collections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Constraint extends Function<Boolean> {

    private Proposition p;

    private List<UserVar> params;

    private String cstrName;

    private boolean discreteOnly, core;

    public static Constraint newCoreConstraint(String n, Proposition p, boolean discrete) {
        return new Constraint(n, p, Collections.<UserVar>emptyList(), discrete, true);
    }

    public static Constraint newPluggableConstraint(String n, Proposition p, List<UserVar> params, boolean discrete) {
        return new Constraint(n, p, params, discrete, false);
    }

    private Constraint(String n, Proposition p, List<UserVar> params, boolean discrete, boolean core) {
        this.p = p;
        this.cstrName = n;
        this.params = params;
        this.discreteOnly = discrete;
        this.core = core;

    }

    @Override
    public BoolType type() {
        return BoolType.getInstance();
    }

    public boolean isDiscrete() {
        return discreteOnly;
    }

    public boolean isCore() {
        return core;
    }

    @Override
    public Type[] signature() {
        Type[] types = new Type[params.size()];
        for (int i = 0; i < params.size(); i++) {
            types[i] = params.get(i).type();
        }
        return types;
    }

    public Proposition getProposition() {
        return p;
    }

    public List<UserVar> getParameters() {
        return params;
    }

    public Boolean eval(SpecModel mo, List<Object> values) {
        for (int i = 0; i < params.size(); i++) {
            UserVar v = params.get(i);
            mo.setValue(v.label(), values.get(i));
        }
        Proposition good = p;
        //Proposition noGood = good.not();
        Boolean bOk = good.eval(mo);
        /*Boolean bKo = noGood.eval(mo);
        if (bOk == null || bKo == null) {
            throw new RuntimeException(good.eval(mo) + "\n" + noGood.eval(mo));
        }
        if (bOk.equals(bKo)) {
            throw new RuntimeException("Both have the same result: " + bOk + " " + bKo);
        } */
        return bOk;
    }

    public SatConstraint instantiate(List values) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return instantiate("btrplace.model.constraint", values);
    }

    public SatConstraint instantiate(String pkg, List values) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (isCore()) {
            return null;
        }
        String clName = id().substring(0, 1).toUpperCase() + id().substring(1);
        Class<SatConstraint> cl = (Class<SatConstraint>) Class.forName(pkg + "." + clName);
        for (Constructor c : cl.getConstructors()) {
            if (c.getParameterTypes().length == values.size()) {
                //System.out.println(Arrays.toString(c.getParameterTypes()));
                //System.out.println(values);
                return (SatConstraint) c.newInstance(values.toArray());
            }
        }
        throw new IllegalArgumentException("No constructors compatible with values '" + values + "'");
    }

    public String id() {
        return cstrName;
    }

    public String pretty() {
        StringBuilder b = new StringBuilder();
        b.append(cstrName);
        if (!params.isEmpty()) {
            b.append('(');
            Iterator<UserVar> ite = params.iterator();
            Var v = ite.next();
            b.append(v.pretty());
            while (ite.hasNext()) {
                v = ite.next();
                b.append(", ").append(v.pretty());
            }
            b.append(')');
        }
        b.append(" ::= ").append(p);
        return b.toString();
    }

    public static String toString(Constraint c, List<Constant> values) {
        return c.toString(values);
    }

    public String toString(List<Constant> values) {
        StringBuilder b = new StringBuilder();
        b.append(id());
        if (!core) {
            b.append('(');
            Iterator<Constant> ite = values.iterator();
            while (ite.hasNext()) {
                b.append(ite.next().toString());
                if (ite.hasNext()) {
                    b.append(", ");
                }
            }
            b.append(')');
        }
        return b.toString();

    }
}
