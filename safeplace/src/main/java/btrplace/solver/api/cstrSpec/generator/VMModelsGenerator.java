package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class VMModelsGenerator implements Iterable<Model>, Iterator<Model> {

    private VM [] vms;

    private Node [] nodes;

    private Model base;

    private TuplesGenerator<Integer> tg;

    public static VM [] makeVMs(Model mo, int nbVMs) {
        VM [] vms = new VM[nbVMs];
        for (int i = 0; i < nbVMs; i++) {
            vms[i] = mo.newVM();
        }
        return vms;
    }
    public VMModelsGenerator(Model mo, int nbVMs) {
        this(mo, makeVMs(mo, nbVMs));
    }


    public VMModelsGenerator(Model mo, VM [] vms) {
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
        tg = new TuplesGenerator<>(Integer.class, states);
    }

    @Override
    public boolean hasNext() {
        return tg.hasNext();
    }

    @Override
    public Model next() {
        Integer [] values = tg.next();
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

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Model> iterator() {
        return this;
    }
}