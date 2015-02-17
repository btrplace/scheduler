package org.btrplace.scheduler.choco.constraint.mttr;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.view.CShareableResource;
import org.chocosolver.solver.search.strategy.selectors.IntValueSelector;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

/**
 * Created by fhermeni on 17/02/2015.
 */
public class RandOverQuartilePlacement implements IntValueSelector {

    private boolean stay;

    private ReconfigurationProblem rp;

    private Random rnd;

    private Comparator<Node> comp;

    private int quartile;

    private Map<IntVar, VM> map;

    /**
     * Make a new heuristic.
     *
     * @param p           the problem to rely on
     * @param pVarMapping   the VM associated to each variable
     * @param cmp the comparator to use to sort the candidate nodes
     * @param q the quartile to consider. From 1 to 4
     * @param stayFirst   {@code true} to force an already VM to stay on its current node if possible
     */
    public RandOverQuartilePlacement(ReconfigurationProblem p, Map<IntVar, VM> pVarMapping, Comparator<Node> cmp, int q, boolean stayFirst) {
        rp = p;
        comp = cmp;
        quartile = q;
        stay = stayFirst;
        map = pVarMapping;
        rnd = new Random();
    }



    @Override
    public int selectValue(IntVar x) {
        if (stay) {
            //System.out.println("stay");
            VM vm = map.get(x);
            if (VMPlacementUtils.canStay(rp, vm)) {
                return rp.getNode(rp.getSourceModel().getMapping().getVMLocation(vm));
            }
        }

        if (!x.isInstantiated()) {
            Node [] candidates = extractNodes(x);
            Arrays.sort(candidates, comp);
            CShareableResource rc = (CShareableResource) rp.getView(ShareableResource.VIEW_ID_BASE + "core");
            /*for (int i = 0; i < candidates.length; i++) {
                System.out.print(" " + candidates[i] + ": " + rc.getPhysicalUsage(rp.getNode(candidates[i])));
            }
            System.out.println();*/
            int s = candidates.length / 4;
            if (s == 0) {
                Node got = candidates[rnd.nextInt(candidates.length)];
                //System.err.println("?(default) " + got);
                //too small to extract quartiles
                return rp.getNode(got);
            }
            int from = s * (quartile - 1);
            //pretty(candidates, from, s);
            Node got = candidates[rnd.nextInt(s) + from];
            //System.out.println("?" + got);
            return rp.getNode(got);
        }
        return x.getValue();
    }

    private void pretty(Node[] candidates, int from, int s) {
        for (int i = from; i < s; i++) {
            System.out.print(" " +candidates[i].id());
        }
        System.out.println();
    }

    private Node[] extractNodes(IntVar x) {
        Node[]ns = new Node[x.getDomainSize()];
        DisposableValueIterator ite = x.getValueIterator(true);
        try {
            int i = 0;
            while (ite.hasNext()) {
                ns[i++] = rp.getNode(ite.next());
            }
        } finally {
            ite.dispose();
        }
        return ns;
    }


    private void sort(Integer [] hosts, CShareableResource rc) {
        Arrays.sort(hosts, new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {
                return rc.getPhysicalUsage(i).getLB() - rc.getPhysicalUsage(j).getLB();
            }
        });
    }
}
