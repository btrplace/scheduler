package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.And;
import btrplace.solver.api.cstrSpec.Or;
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
    public Set<Value> domain() {
        //All possible subsets or t. Ouch
        //throw new UnsupportedOperationException();
        Value[] values = type.domain().toArray(new Value[type.domain().size()]);
        int nbElements = (int) Math.pow(2, values.length);
        //System.err.println(nbElements);
        Set<Value> res = new HashSet<>();
        for (int i = 0; i < nbElements; i++) {
            Set<Value> sub = new HashSet<>();
            long x = i;
            //decompose x bit per bit
            for (int idx = 0; idx < values.length; idx++) {
                if (x%2 == 0) {
                    sub.add(values[idx]);
                }
                x = x >>> 1;
            }
            if (!sub.isEmpty()) {
                res.add(new Value(sub, type));
            }
        }
        return res;
    }

    @Override
    public String label() {
        return new StringBuilder("set<").append(type.label()).append(">").toString();
    }

    @Override
    public boolean isIn(String n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value newValue(String n) {
        //Add a value inside the set
        return type.newValue(n);
        //throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return label();
    }

    public Type subType() {
        return type;
    }
}
