package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.*;

import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public class NodeModelsGenerator implements Iterable<Model>, Iterator<Model> {

    private Node[] nodes;

    private int nbStates;

    private int k;

    public NodeModelsGenerator(Node [] ns) {
        this.nodes = ns;
        nbStates = (int)Math.pow(2, ns.length);
    }

    public static Node [] makeNodes(ElementBuilder eb, int nb) {
        Node [] nodes = new Node[nb];
        for (int i = 0; i < nb; i++) {
            nodes[i] = eb.newNode();
        }

        return nodes;
    }

    public NodeModelsGenerator(ElementBuilder eb, int nbNodes) {
        this(makeNodes(eb, nbNodes));
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

    public void reset() {
        k = 0;
    }
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
