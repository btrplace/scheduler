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

    private Map<VM, Node> activeOn;

    private Map<Node, Set<VM>> host;

    public SpecMapping(Mapping ma) {
        vmState = new HashMap<>(ma.getNbVMs());
        activeOn = new HashMap<>(ma.getNbVMs());
        nodeState = new HashMap<>(ma.getNbNodes());
        host = new HashMap<>();
        for (Node n : ma.getOnlineNodes()) {
            nodeState.put(n, NodeStateType.Type.online);
            host.put(n, new HashSet<VM>());
            for (VM v : ma.getRunningVMs(n)) {
                vmState.put(v, VMStateType.Type.running);
                activeOn.put(v, n);
                host.get(n).add(v);
            }
            for (VM v : ma.getSleepingVMs(n)) {
                vmState.put(v, VMStateType.Type.sleeping);
                activeOn.put(v, n);
                host.get(n).add(v);
            }
        }
        for (Node n : ma.getOfflineNodes()) {
            nodeState.put(n, NodeStateType.Type.offline);
            host.put(n, new HashSet<VM>());
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
        return activeOn.get(v);
    }

    public void unhost(Node n, VM v) {
        host.get(n).remove(v);
    }

    public void host(VM v, Node n) {
        host.get(n).add(v);
    }

    public void activateOn(VM v, Node n) {
        host(v, n);
        activeOn.put(v, n);
    }

    public void desactivate(VM v) {
        activeOn.remove(v);
    }

    public Set<VM> runnings(Node n) {
        Set<VM> s = new HashSet<>();
        for (VM v : host.get(n)) {
            if (state(v).equals(VMStateType.Type.running)) {
                s.add(v);
            }
        }
        return s;
    }

    public Set<VM> sleeping(Node n) {
        Set<VM> s = new HashSet<>();
        for (VM v : host.get(n)) {
            if (state(v).equals(VMStateType.Type.sleeping)) {
                s.add(v);
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
        return host.get(n);
        /*Set<VM> s = new HashSet<>();
        for (VM v : host.get(n)) {
            if (activeOn.get(v).equals(n)) {
                s.add(v);
            }
        }
        return s;*/
    }
}
