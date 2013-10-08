package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.func.*;

/**
 * @author Fabien Hermenier
 */
public class Functions {

    private Satisfy sat;
    public Functions(Satisfy st) {
        this.sat = st;
    }

    public Function get(String id) {
        switch(id) {
            case "vmState": return new VMState(sat.pop());
            case "nodeState": return new NodeState(sat.pop());
            case "colocated": return new Colocated(sat.pop());
            case "host": return new Host(sat.pop());
            case "card": return new Card(sat.pop());
            case "hosted" : return new Hoster(sat.pop());
            default: throw new RuntimeException("Cannot resolve function '" + id + "'");
        }
    }
}
