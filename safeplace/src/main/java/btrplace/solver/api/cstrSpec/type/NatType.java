package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NatType implements Type {

    private int inf, sup;

    private static final NatType instance = new NatType(0, 5);

    private NatType(int lb, int ub) {
        this.inf = lb;
        this.sup = ub;
    }

    @Override
    public Set<Value> domain() {
        Set<Value> s = new HashSet<>();
        for (int i = inf; i <= sup; i++) {
            s.add(new Value(i, this));
        }
        return s;
    }

    public static NatType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        try {
            Integer i = Integer.parseInt(n);
            if (i >= inf && i <= sup) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }

    @Override
    public String label() {
        return "nat";
    }

    @Override
    public Value newValue(String n) {
        return new Value(Integer.parseInt(n), this);
    }
}
