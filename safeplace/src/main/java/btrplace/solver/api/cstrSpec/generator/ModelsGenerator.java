package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.*;
import btrplace.solver.api.cstrSpec.util.Maths;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public class ModelsGenerator implements Generator<Model> {

    private NodeModelsGenerator ng;

    private VMModelsGenerator vg;

    private VM[] vms;

    private int k, nbStates;

    public ModelsGenerator(int nbNodes, int nbVMs) {
        k = 0;
        nbStates = 0;
        for (int q = 0; q <= nbNodes; q++) {
            long vmp = (long) Math.pow(2 * q + 1, nbVMs);
            long np = Maths.C(nbNodes, q);
            long r = vmp * np;
            nbStates += r;
        }

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
        return k < nbStates;
    }

    @Override
    public Model next() {
        if (vg == null || !vg.hasNext()) {
            vg = new VMModelsGenerator(ng.next(), vms);
        }
        k++;
        return vg.next();
    }

    @Override
    public void reset() {
        vg = null;
        ng.reset();
    }

    @Override
    public int done() {
        return k;
    }

    @Override
    public int count() {
        return nbStates;
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