package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

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
    public Set domain() {
        //All possible subsets or t. Ouch
        Object[] values = type.domain().toArray(new Object[type.domain().size()]);
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
                res.add(sub/*new Value(sub, type)*/);
            }
        }
        return res;
    }

    @Override
    public String label() {
        return new StringBuilder("set<").append(type.label()).append(">").toString();
    }

    @Override
    public boolean match(String n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value newValue(String n) {
        //Add a value inside the set
        //return type.newValue(n);
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return label();
    }

    public Type subType() {
        return type;
    }
}
