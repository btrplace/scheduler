package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;

import java.util.Random;

/**
 * @author Fabien Hermenier
 */
public class ShareableResourceFuzzer implements ModelViewFuzzer<ShareableResource> {

    private String id;

    private Random rnd;

    private int minCons, maxCons, minCapa, maxCapa;

    public ShareableResourceFuzzer(String rc, int minCons, int maxCons, int minCapa, int maxCapa) {
        id = rc;
        rnd = new Random();
        this.minCons = minCons;
        this.minCapa = minCapa;
        this.maxCapa = maxCapa;
        this.maxCons = maxCons;
    }

    @Override
    public void decorate(ReconfigurationPlan p) {
        ShareableResource rc = new ShareableResource(id);
        for (VM v : p.getOrigin().getMapping().getAllVMs()) {
            int c = rnd.nextInt(maxCons - minCons + 1) + minCons;
            rc.setConsumption(v, c);
        }

        for (Node n : p.getOrigin().getMapping().getAllNodes()) {
            int c = rnd.nextInt(maxCapa - minCapa + 1) + minCapa;
            rc.setCapacity(n, c);
        }

        p.getOrigin().attach(rc);
    }
}
