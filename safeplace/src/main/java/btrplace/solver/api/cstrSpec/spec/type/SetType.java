package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SetType implements Type {

    private Type type;

    public SetType(Type t) {
        type = t;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SetType setType = (SetType) o;
        if (type == null) {
            return true;
        }
        return type.equals(setType.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public Set domain(Model mo) {
        //All possible subsets of t. Ouch
        Object[] values = type.domain(mo).toArray(new Object[type.domain(mo).size()]);
        int nbElements = (int) Math.pow(2, values.length);
        //System.err.println(nbElements);
        Set<Object> res = new HashSet<>();
        for (int i = 0; i < nbElements; i++) {
            Set sub = new HashSet<>();
            long x = i;
            //decompose x bit per bit
            for (Object value : values) {
                if (x % 2 == 0) {
                    sub.add(value);
                }
                x = x >>> 1;
            }
            if (!sub.isEmpty()) {
                res.add(sub);
            }
        }
        return res;
    }

    @Override
    public String label() {
        StringBuilder b = new StringBuilder("set<");
        if (type == null) {
            b.append('?');
        } else {
            b.append(type.label());
        }
        return b.append('>').toString();
    }

    @Override
    public boolean match(String n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Constant newValue(String n) {
        //Add a value inside the set
        //return type.newValue(n);
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public Type inside() {
        return type;
    }

    public Type enclosingType() {
        return type;
    }

    @Override
    public Type include() {
        return this;
    }
}
