package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.invariant.func.*;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Functions {

    public Function get(String id, List<Term> stack) {
        switch (id) {
            case "vmState":
                return new VMState(stack);
            case "nodeState":
                return new NodeState(stack);
            case "colocated":
                return new Colocated(stack);
            case "host":
                return new Host(stack);
            case "card":
                return new Card(stack);
            case "hosted":
                return new Hosted(stack);
            case "cons":
                return new Cons(stack);
            case "capa":
                return new Capa(stack);
            default:
                throw new RuntimeException("Cannot resolve function '" + id + "'");
        }
    }
}
