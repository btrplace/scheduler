package btrplace.solver.api.cstrSpec.type;

import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeType implements Type {

    private static NodeType instance = new NodeType();

    private Set<Node> values;

    private NodeType() {
        Set<Node> s = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            s.add(new Node(i));
        }

        values = Collections.unmodifiableSet(s);
    }

    public static NodeType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        return false;
    }

    @Override
    public String label() {
        return "node";
    }

    @Override
    public Set<Node> domain() {
        return values;
    }

    @Override
    public Value newValue(String n) {
        throw new UnsupportedOperationException();
    }
}
