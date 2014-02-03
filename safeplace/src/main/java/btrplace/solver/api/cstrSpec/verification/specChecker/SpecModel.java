package btrplace.solver.api.cstrSpec.verification.specChecker;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.NodeStateType;
import btrplace.solver.api.cstrSpec.spec.type.VMStateType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class SpecModel {

    private Map<VM, VMStateType.Type> vmState;

    private Map<Node, NodeStateType.Type> nodeState;

    private Map<VM, Node> location;

    public SpecModel() {
        vmState = new HashMap<>();
        location = new HashMap<>();
        nodeState = new HashMap<>();
    }

    public NodeStateType.Type state(Node n) {
        return nodeState.get(n);
    }

    public VMStateType.Type state(VM n) {
        return vmState.get(n);
    }

    public void state(Node n, NodeStateType.Type t) {
        nodeState.put(n, t);
    }

    public void state(VM v, VMStateType.Type t) {
        vmState.put(v, t);
    }
}
