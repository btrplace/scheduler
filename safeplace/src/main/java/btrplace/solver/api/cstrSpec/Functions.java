package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.func.*;

import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Functions {

   public Function get(String id, List<Term> stack) {
        switch(id) {
            case "vmState": return new VMState(stack);
            case "nodeState": return new NodeState(stack);
            case "colocated": return new Colocated(stack);
            case "host": return new Host(stack);
            case "card": return new Card(stack);
            case "hosted" : return new Hosted(stack);
            default: throw new RuntimeException("Cannot resolve function '" + id + "'");
        }
    }
}
