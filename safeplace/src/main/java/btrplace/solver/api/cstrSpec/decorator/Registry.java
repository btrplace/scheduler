package btrplace.solver.api.cstrSpec.decorator;

import btrplace.model.ElementBuilder;
import btrplace.model.Node;
import btrplace.model.VM;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class Registry {

    private Map<String, VM> vms;

    private Map<String, Node> nodes;

    private ElementBuilder eb;

    public Registry(ElementBuilder b) {
        vms = new HashMap<>();
        nodes = new HashMap<>();
        this.eb = b;
    }

    public Node getNode(String id) {
        Node n = nodes.get(id);
        if (n == null) {
            n = eb.newNode();
            nodes.put(id, n);
        }
        return n;
    }

    public VM getVM(String id) {
        VM vm = vms.get(id);
        if (vm == null) {
            vm = eb.newVM();
            vms.put(id, vm);
        }
        return vm;
    }
}
