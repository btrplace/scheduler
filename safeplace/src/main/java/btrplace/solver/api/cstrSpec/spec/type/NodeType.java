package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public class NodeType extends Atomic {

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
    public Constant newValue(String n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean comparable(Type t) {
        return true;
    }
}
