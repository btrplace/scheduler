package btrplace.solver.api.cstrSpec;

import btrplace.model.Model;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.api.cstrSpec.spec.prop.Proposition;
import btrplace.solver.api.cstrSpec.spec.term.Primitive;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.term.Var;
import btrplace.solver.api.cstrSpec.spec.term.func.Function;
import btrplace.solver.api.cstrSpec.spec.type.BoolType;
import btrplace.solver.api.cstrSpec.spec.type.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Constraint extends Function<Boolean> {

    private Proposition p;

    private Proposition not;

    private List<UserVar> params;

    private List<Primitive> primitives;

    private String cstrName;

    public Constraint(String n, Proposition p, List<Primitive> primitives, List<UserVar> params) {
        this.p = p;
        this.not = p.not();
        this.cstrName = n;
        this.params = params;
        this.primitives = primitives;
    }

    @Override
    public BoolType type() {
        return BoolType.getInstance();
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

    public Boolean eval(Model res, List<Object> values) {

        for (int i = 0; i < values.size(); i++) {
            UserVar var = params.get(i);
            if (!var.set(res, values.get(i))) {
                throw new IllegalArgumentException("Unable to set '" + var.label() + "' (type '" + var.type() + "') to '" + values.get(i) + "'");
            }
        }
        if (res == null) {     //TODO: flaw ?
            return false;
        }
        Boolean bOk = this.p.eval(res);
        Boolean bKO = this.not.eval(res);

        if (bOk == null || bKO == null) {
            throw new RuntimeException("Both null !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        }
        if (bOk && bKO) {
            throw new RuntimeException(values + " good and bad !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        } else if (!(bOk || bKO)) {
            throw new RuntimeException("Nor good or bad !\ngood:" + this.p + "\nnotGood: " + not + "\n" + res.getMapping().toString());
        }
        this.reset();
        return bOk;
    }

    public SatConstraint instantiate(List values) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return instantiate("btrplace.model.constraint", values);
    }

    public SatConstraint instantiate(String pkg, List values) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String clName = id().substring(0, 1).toUpperCase() + id().substring(1);
        Class<SatConstraint> cl = (Class<SatConstraint>) Class.forName(pkg + "." + clName);
        for (Constructor c : cl.getConstructors()) {
            if (c.getParameterTypes().length == values.size()) {
                return (SatConstraint) c.newInstance(values.toArray());
            }
        }
        throw new IllegalArgumentException("No constructors compatible with values '" + values + "'");
    }

    public void reset() {
        for (UserVar var : params) {
            var.unset();
        }
    }

    public String id() {
        return cstrName;
    }

    public String pretty() {
        StringBuilder b = new StringBuilder(toString()).append(" ::=\n");
        b.append('\t').append(p);
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(cstrName).append('(');
        Iterator<UserVar> ite = params.iterator();
        if (ite.hasNext()) {
            Var v = ite.next();
            b.append(v.pretty());
        }
        while (ite.hasNext()) {
            Var v = ite.next();
            b.append(", ").append(v.pretty());
        }
        b.append(')');
        return b.toString();
    }
}
