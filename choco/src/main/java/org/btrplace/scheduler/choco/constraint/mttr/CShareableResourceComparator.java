package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.model.Node;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.CShareableResource;

import java.util.Comparator;

/**
 * Created by fhermeni on 17/02/2015.
 */
public class CShareableResourceComparator implements Comparator<Node> {

    private ReconfigurationProblem rp;

    private CShareableResource rc;

    private int ordering;

    public CShareableResourceComparator(ReconfigurationProblem rp, CShareableResource rc, boolean asc) {
        this.rp = rp;
        this.rc = rc;
        ordering = asc ? 1 : -1;
    }
    @Override
    public int compare(Node n1, Node n2) {
        int i = rp.getNode(n1);
        int j = rp.getNode(n2);
        return ordering * (rc.getPhysicalUsage(i).getLB() - rc.getPhysicalUsage(j).getLB());
    }
}
