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

    private TupleGenerator<Integer> tg;

    public VMModelsGenerator(Model mo, int nbVMs) {
        this.base = mo;
        this.vms = new VM[nbVMs];
        nodes = mo.getMapping().getAllNodes().toArray(new Node[mo.getMapping().getNbNodes()]);
        for (int i = 0; i < nbVMs; i++) {
            this.vms[i] = mo.newVM();
        }

        List<List<Integer>> states = new ArrayList<>();
        List<Integer> st = new ArrayList<>();
        for (int i = 0; i < 2 * mo.getMapping().getOnlineNodes().size() + 1; i++) {
            st.add(i);
        }
        for (int i = 0; i < nbVMs; i++) {
            states.add(st);
        }
        tg = new TupleGenerator<>(Integer.class, states);
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
            } else if (st < vms.length + 1) {
                Node n = nodes[st - 1];
                if (!m.getMapping().addRunningVM(v, n)) {
                    throw new UnsupportedOperationException();
                }
            } else {
                Node n = nodes[st - 1 - vms.length];
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