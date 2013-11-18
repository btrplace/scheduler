package btrplace.solver.api.cstrSpec.invariant.type;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.invariant.Value;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeType implements Type {

    private static NodeType instance = new NodeType();


    private NodeType() {
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
    public Set<Node> domain(Model mo) {
        return mo.getMapping().getAllNodes();
    }

    @Override
    public Value newValue(String n) {
        throw new UnsupportedOperationException();
    }
}
