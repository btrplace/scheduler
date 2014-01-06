package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.util.AllTuplesGenerator;
import btrplace.solver.api.cstrSpec.util.DefaultGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class VMModelsGenerator extends DefaultGenerator<Model> {

    private VM[] vms;

    private Node[] nodes;

    private Model base;

    public static VM[] makeVMs(Model mo, int nbVMs) {
        VM[] vms = new VM[nbVMs];
        for (int i = 0; i < nbVMs; i++) {
            vms[i] = mo.newVM();
        }
        return vms;
    }

    public VMModelsGenerator(Model mo, int nbVMs) {
        this(mo, makeVMs(mo, nbVMs));
    }


    public VMModelsGenerator(Model mo, VM[] vms) {
        this.base = mo;
        this.vms = vms;

        this.nodes = mo.getMapping().getOnlineNodes().toArray(new Node[mo.getMapping().getOnlineNodes().size()]);
        List<List<Integer>> states = new ArrayList<>();
        List<Integer> st = new ArrayList<>();
        int nbOns = mo.getMapping().getOnlineNodes().size();
        for (int i = 0; i < 2 * nbOns + 1; i++) {
            st.add(i);
        }
        for (int i = 0; i < vms.length; i++) {
            states.add(st);
        }
        tg = new AllTuplesGenerator<>(Integer.class, states);
    }

    @Override
    public Model next() {
        Integer[] values = (Integer[]) tg.next();
        Model m = base.clone();
        for (int i = 0; i < values.length; i++) {
            int st = values[i];
            VM v = vms[i];
            if (st == 0) {
                if (!m.getMapping().addReadyVM(v)) {
                    throw new UnsupportedOperationException();
                }
            } else if (st < nodes.length + 1) {
                Node n = nodes[st - 1];
                if (!m.getMapping().addRunningVM(v, n)) {
                    throw new UnsupportedOperationException();
                }
            } else {
                Node n = nodes[st - 1 - nodes.length];
                if (!m.getMapping().addSleepingVM(v, n)) {
                    throw new UnsupportedOperationException();
                }
            }
        }
        return m;
    }
}