package org.btrplace.safeplace.fuzzer;

import org.btrplace.model.DefaultModel;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;

import java.util.Collection;
import java.util.Random;

/**
 * Created by fhermeni on 07/09/2015.
 */
public class ModelGenerator {

    private int nbVMs, nbNodes;

    public ModelGenerator() {
        nbNodes = 1;
        nbVMs = 1;
    }


    public ModelGenerator vms(int n) {
        nbVMs = n;
        return this;
    }

    public ModelGenerator nodes(int n) {
        nbNodes = n;
        return this;
    }

    public int vms() {
        return nbVMs;
    }

    public int nodes() {
        return nbNodes;
    }

    public Model build() {
        Random rnd = new Random();
        Model mo = new DefaultModel();
        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();

            if (rnd.nextBoolean()) {
                mo.getMapping().addOnlineNode(n);
            } else {
                mo.getMapping().addOfflineNode(n);
            }
        }

        for (int i = 0; i < nbVMs; i++) {
            VM v = mo.newVM();
            switch (rnd.nextInt(3)) {
                case 0:
                    mo.getMapping().addReadyVM(v);
                    break;
                case 1:
                    mo.getMapping().addSleepingVM(v, oneOf(rnd, mo.getMapping().getOnlineNodes()));
                    break;
                case 2:
                    mo.getMapping().addRunningVM(v, oneOf(rnd, mo.getMapping().getOnlineNodes()));
                    break;
            }
        }
        return mo;
    }

    private Node oneOf(Random rnd, Collection<Node> nodes) {
        int cnt = rnd.nextInt(nodes.size()) + 1;

        for (Node n : nodes) {
            cnt--;
            if (cnt == 0) {
                return n;
            }
        }
        return null; //should not occur
    }
}
