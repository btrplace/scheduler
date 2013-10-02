package btrplace.solver.api.cstrSpec.type;

import btrplace.solver.api.cstrSpec.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Node implements Type {

    private static Node instance = new Node();

    private Set<Value> values;
    private Node() {
        values = new HashSet<>();
    }

    public static Node getInstance() {
        return instance;
    }

    @Override
    public Set getPossibleValues() {
        return values;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean isIn(String n) {
        return false;
    }

    @Override
    public String label() {
        return "node";
    }

    @Override
    public Value newValue(String n) {
        Value v = new Value(n, this);
        if (values.add(v)) {
            return v;
        }
        return null;

    }

}
