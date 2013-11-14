package btrplace.solver.api.cstrSpec.generator;

import btrplace.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class NodeModelsGenerator extends DefaultGenerator<Model> {

    private Node[] nodes;

    public NodeModelsGenerator(Node[] ns) {
        this.nodes = ns;
        List<Integer> state = new ArrayList<>(2);
        state.add(0);
        state.add(1);
        List<List<Integer>> domains = new ArrayList<>();
        for (int i = 0; i < ns.length; i++) {
            domains.add(state);
        }
        tg = new AllTuplesGenerator<>(Integer.class, domains);
    }

    public static Node[] makeNodes(ElementBuilder eb, int nb) {
        Node[] nodes = new Node[nb];
        for (int i = 0; i < nb; i++) {
            nodes[i] = eb.newNode();
        }

        return nodes;
    }

    public NodeModelsGenerator(ElementBuilder eb, int nbNodes) {
        this(makeNodes(eb, nbNodes));
    }

    @Override
    public Model next() {
        Model mo = new DefaultModel();
        Mapping m = mo.getMapping();
        Integer[] st = (Integer[]) tg.next();
        for (int i = 0; i < st.length; i++) {
            int v = st[i];
            if (v == 0) {
                m.addOnlineNode(nodes[i]);
            } else {
                m.addOfflineNode(nodes[i]);
            }
        }
        return mo;
    }
}
