package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.*;

/**
 * @author Fabien Hermenier
 */
public class ModelsGenerator extends DefaultGenerator<Model> {

    private NodeModelsGenerator ng;

    private VMModelsGenerator vg;

    private VM[] vms;

    public ModelsGenerator(int nbNodes, int nbVMs) {
        Node[] ns = new Node[nbNodes];
        vms = new VM[nbVMs];
        ElementBuilder eb = new DefaultElementBuilder();
        for (int i = 0; i < nbNodes; i++) {
            ns[i] = eb.newNode();
        }
        for (int i = 0; i < nbVMs; i++) {
            vms[i] = eb.newVM();
        }
        ng = new NodeModelsGenerator(ns);
    }

    @Override
    public boolean hasNext() {
        return ng.hasNext() || (vg == null || vg.hasNext());
    }

    @Override
    public Model next() {
        if (vg == null || !vg.hasNext()) {
            vg = new VMModelsGenerator(ng.next(), vms);
        }
        return vg.next();
    }

    @Override
    public void reset() {
        vg = null;
        ng.reset();
    }
}