package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.*;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.term.UserVar;
import btrplace.solver.api.cstrSpec.spec.type.ColType;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.Type;
import btrplace.solver.api.cstrSpec.spec.type.VMType;
import btrplace.solver.api.cstrSpec.util.Generator;
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

    private int nbVMs, nbNodes;

    public ModelsGenerator(int nbNodes, int nbVMs) {
        k = 0;
        this.nbNodes = nbNodes;
        this.nbVMs = nbVMs;
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

    public int getNbVMs() {
        return nbVMs;
    }

    public int getNbNodes() {
        return nbNodes;
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

    public static ModelsGenerator makeFromSpec(Constraint cstr) {
        if (cstr.isCore()) {
            return new ModelsGenerator(1, 1);
        }
        int nbVMs = 0;
        int nbNodes = 0;
        for (UserVar v : cstr.getParameters()) {
            if (v.type() == VMType.getInstance()) {
                nbVMs++;
            } else if (v.type() == NodeType.getInstance()) {
                nbNodes++;
            } else if (v.type() instanceof ColType) {
                //At least 2 elements of the type
                int n = 0;
                Type t = v.type();
                while (t instanceof ColType) {
                    n += 2;
                    t = ((ColType) t).enclosingType();
                }
                if (t == VMType.getInstance()) {
                    nbVMs += n;
                } else if (t == NodeType.getInstance()) {
                    nbNodes += n;
                }

            }
        }

        //At least as much nodes that VMs
        return new ModelsGenerator(Math.max(nbNodes, nbVMs), nbVMs);
    }
}