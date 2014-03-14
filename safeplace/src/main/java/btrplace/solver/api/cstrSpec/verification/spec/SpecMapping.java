package btrplace.solver.api.cstrSpec.verification.spec;

import btrplace.model.Mapping;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.type.NodeStateType;
import btrplace.solver.api.cstrSpec.spec.type.VMStateType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class SpecMapping {

    private Map<VM, VMStateType.Type> vmState;

    private Map<Node, NodeStateType.Type> nodeState;

    private Map<VM, Node> location;

    public SpecMapping(Mapping ma) {
        vmState = new HashMap<>();
        location = new HashMap<>();
        nodeState = new HashMap<>();

        for (Node n : ma.getOnlineNodes()) {
            nodeState.put(n, NodeStateType.Type.online);
            for (VM v : ma.getRunningVMs(n)) {
                vmState.put(v, VMStateType.Type.running);
                location.put(v, n);
            }
            for (VM v : ma.getSleepingVMs(n)) {
                vmState.put(v, VMStateType.Type.sleeping);
                location.put(v, n);
            }
        }
        for (Node n : ma.getOfflineNodes()) {
            nodeState.put(n, NodeStateType.Type.offline);
        }
        for (VM v : ma.getReadyVMs()) {
            vmState.put(v, VMStateType.Type.ready);
        }
    }

    public VMStateType.Type state(VM vm) {
        return vmState.get(vm);
    }

    public NodeStateType.Type state(Node n) {
        return nodeState.get(n);
    }

    public void state(Node n, NodeStateType.Type t) {
        nodeState.put(n, t);
    }

    public void state(VM v, VMStateType.Type t) {
        vmState.put(v, t);
    }

    public Set<VM> VMs() {
        return vmState.keySet();
    }

    public Set<Node> nodes() {
        return nodeState.keySet();
    }


    public Node host(VM v) {
        return location.get(v);
    }

    public void unhost(VM v) {
        location.remove(v);
    }

    public void host(VM v, Node n) {
        location.put(v, n);
    }

    public Set<VM> runnings(Node n) {
        Set<VM> s = new HashSet<>();
        for (Map.Entry<VM, Node> e : location.entrySet()) {
            if (e.getValue().equals(n) && state(e.getKey()).equals(VMStateType.Type.running)) {
                s.add(e.getKey());
            }
        }
        return s;
    }

    public Set<VM> sleeping(Node n) {
        Set<VM> s = new HashSet<>();
        for (Map.Entry<VM, Node> e : location.entrySet()) {
            if (e.getValue().equals(n) && state(e.getKey()).equals(VMStateType.Type.sleeping)) {
                s.add(e.getKey());
            }
        }
        return s;
    }

    public Set<VM> ready() {
        Set<VM> s = new HashSet<>();
        for (Map.Entry<VM, VMStateType.Type> e : vmState.entrySet()) {
            if (e.getValue() == VMStateType.Type.ready) {
                s.add(e.getKey());
            }
        }
        return s;
    }

    public Set<VM> hosted(Node n) {
        Set<VM> s = new HashSet<>();
        for (Map.Entry<VM, Node> e : location.entrySet()) {
            if (e.getValue().equals(n)) {
                s.add(e.getKey());
            }
        }
        return s;
    }
}
