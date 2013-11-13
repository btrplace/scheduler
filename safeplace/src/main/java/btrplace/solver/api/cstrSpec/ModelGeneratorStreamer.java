package btrplace.solver.api.cstrSpec;

import btrplace.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ModelGeneratorStreamer implements Iterator<Model> {

    private Node [] nodes;
    private VM [] vms;

    private ElementBuilder eb;

    private int nbStates;

    private int k;

    private List<List<Integer>> states;

    public ModelGeneratorStreamer(int nbNodes, int nbVMs) {
        this.nodes = new Node[nbNodes];
        this.vms = new VM[nbVMs];
        this.eb = new DefaultElementBuilder();
        for (int i = 0; i < nbNodes; i++) {
            this.nodes[i] = eb.newNode();
        }
        for (int i = 0; i < nbVMs; i++) {
            this.vms[i] = eb.newVM();
        }
    }

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Model next() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private Model makeNodeModel(Node [] nodes, int id) {
        Model m = new DefaultModel();
        for (Node n : nodes) {
            Node nn = m.newNode(n.id());
            if (id % 2 == 0) {
                m.getMapping().addOnlineNode(nn);
            } else {
                m.getMapping().addOfflineNode(nn);
            }
            id = id >> 1;
        }
        return m;
    }

    private List<Model> fillWithVMs(Model base, VM [] vms, Node [] nodes) {
        List<Model> res = new ArrayList<>();
        int nbVMStates = (int) Math.pow(1 + base.getMapping().getOnlineNodes().size() * 2, vms.length);
        int [] vmIndexes = new int[vms.length];
        Node [] ons = base.getMapping().getOnlineNodes().toArray(new Node[base.getMapping().getOnlineNodes().size()]);
        for (int k = 0; k < nbVMStates; k++) {
            int z = 0;
            Model m = base.clone();
            for (VM vm : vms) {
                //Value 0 -> readyVM
                //Value 1->nbOnlines -> running on node v - 1
                //Value nbOnlines -> 2* nbOnlines + 1 > sleeping on node
                int v = vmIndexes[z++];
                //System.err.println(Arrays.toString(vmIndexes));
                if (v == 0) {
                    if (!m.getMapping().addReadyVM(vm)) {
                        System.err.println(Arrays.toString(vmIndexes));
                        System.err.println("Unable to make '" + vm + "' ready:\n" + m);
                    }
                } else if (v < ons.length + 1) {
                    Node n = ons[v - 1];
                    if (!m.getMapping().addRunningVM(vm, n)) {
                        System.err.println(Arrays.toString(vmIndexes));
                        System.err.println("Unable to make '" + vm + "' running on " + n + ":\n" + m);
                    }
                } else {
                    Node n = ons[v - 1 - ons.length];
                    if (!m.getMapping().addSleepingVM(vm, n)) {
                        System.err.println(Arrays.toString(vmIndexes));
                        System.err.println("Unable to make '" + vm + "' sleeping on " + n + ":\n" + m);
                    }
                }
                for (int w = 0; w < vms.length; w++) {
                    vmIndexes[w]++;
                    if (vmIndexes[w] < ons.length * 2 + 1 ) {
                        break;
                    }
                    vmIndexes[w] = 0;
                }
            }
            res.add(m);
        }
        return res;
    }

    public List<Model> all(int nbVMs, int nbNodes) {
        List<Model> res = new ArrayList<>();

        //Every node state: 2**nbNodes models
        for (int i = 0; i < Math.pow(2, nbNodes); i++) {
            Model baseModel = makeNodeModel(nodes, i);
            res.addAll(fillWithVMs(baseModel, vms, nodes));
        }
        return res;
    }
}