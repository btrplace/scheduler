package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.model.Node;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.chocosolver.solver.variables.IntVar;

import java.util.Comparator;

/**
 * Created by fhermeni on 18/02/2015.
 */
public class CapacityComparator implements Comparator<Node> {

    private ReconfigurationProblem rp;

    private int order;
    public CapacityComparator(ReconfigurationProblem rp, boolean asc) {
            this.rp = rp;
            order = asc ? 1 : -1;
        }

    @Override
    public int compare(Node n1, Node n2) {
        IntVar c1 = rp.getNbRunningVMs()[rp.getNode(n1)];
        IntVar c2 = rp.getNbRunningVMs()[rp.getNode(n2)];
        return order * (c1.getLB() - c2.getLB());
    }
}
