package btrplace.solver.api.cstrSpec;

import btrplace.model.*;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public class NodeModelGenerator implements Iterable<Model>, Iterator<Model> {

    private Node[] nodes;

    private int nbStates;

    private int k;

    public NodeModelGenerator(ElementBuilder eb, int nbNodes) {
        this.nodes = new Node[nbNodes];
        nbStates = (int) Math.pow(2, nbNodes);
        for (int i = 0; i < nbNodes; i++) {
            this.nodes[i] = eb.newNode();
        }

    }

    @Override
    public boolean hasNext() {
        return k < nbStates;
    }

    @Override
    public Model next() {
        int st = k;
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        for (Node n : nodes) {
            if (st % 2 == 0) {
                m.addOnlineNode(n);
            } else {
                m.addOfflineNode(n);
            }
            st = st >> 1;
        }
        k++;
        return mo;
    }

    @Override
    public Iterator<Model> iterator() {
        return this;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
