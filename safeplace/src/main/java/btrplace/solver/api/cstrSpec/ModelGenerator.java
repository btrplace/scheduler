package btrplace.solver.api.cstrSpec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ModelGenerator {

    private Model makeNodeModel(List<Node> nodes, int id) {
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

    private List<Model> fillWithVMs(Model base, List<VM> vms, List<Node> nodes) {
        List<Model> res = new ArrayList<>();
        int nbVMStates = (int) Math.pow(1 + base.getMapping().getOnlineNodes().size() * 2, vms.size());
        int [] vmIndexes = new int[vms.size()];
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
                    m.getMapping().addReadyVM(vm);
                } else if (v < m.getMapping().getOnlineNodes().size() + 1) {
                    Node n = nodes.get(v - 1);
                    m.getMapping().addRunningVM(vm, n);
                } else {
                    Node n = nodes.get(v - 1 - m.getMapping().getOnlineNodes().size());
                    m.getMapping().addSleepingVM(vm, n);
                }
                for (int w = 0; w < vms.size(); w++) {
                    vmIndexes[w]++;
                    if (vmIndexes[w] < m.getMapping().getOnlineNodes().size() * 2 + 1 ) {
                        break;
                    }
                    vmIndexes[w] = 0;
                }
            }
            if (m.getMapping().getAllVMs().size() != 3 || m.getMapping().getAllNodes().size() != 3) {
                throw new RuntimeException();
            }
            res.add(m);
        }
        return res;
    }

    public List<Model> all(int nbVMs, int nbNodes) {
        List<VM> vms = new ArrayList<>(nbVMs);
        List<Node> nodes = new ArrayList<>(nbNodes);
        List<Model> res = new ArrayList<>();
        Model mo = new DefaultModel();
        for (int i = 0; i < nbVMs; i++) {
            vms.add(mo.newVM());
        }
        for (int i = 0; i < nbNodes; i++) {
            nodes.add(mo.newNode());
        }

        //Every node state: 2**nbNodes models
        for (int i = 0; i < Math.pow(2, nbNodes); i++) {
            Model baseModel = makeNodeModel(nodes, i);
            res.addAll(fillWithVMs(baseModel, vms, nodes));
        }
        return res;
    }
}
