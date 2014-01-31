package btrplace.solver.api.cstrSpec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Fabien Hermenier
 */
public class TestSpec {

    SpecReader ex = new SpecReader();

    @Test
    public void testFoo() {
        for (int i = 1; i <= 5; i++) {
            int nbNodes = i;
            int nbVMs = i;
            long nbModels = 0;
            for (int q = 0; q <= nbNodes; q++) {
                long vmp = (long) Math.pow(2 * q + 1, nbVMs);
                long np = C(nbNodes, q);
                long r = vmp * np;
                nbModels += r;
            }
            System.err.println("Nb of models having " + nbNodes + " nodes and " + nbVMs + " VMs: " + nbModels);
        }
    }

    public static long C(int n, int k) {
        return facto(n) / (facto(k) * facto(n - k));
    }

    public static long facto(int n) {
        long r = 1;
        while (n > 1) {
            r *= n--;
        }
        return r;
    }

    private Specification getSpecification() throws Exception {
        return ex.getSpecification(new File("src/test/resources/v1_1.cspec"));
    }

    @Test
    public void testV1() throws Exception {
        Specification spec = getSpecification();
        System.out.println(spec.pretty());
        System.out.flush();
        Assert.assertEquals(spec.getConstraints().size(), 29);
    }

    @Test
    public void testInstantiate() throws Exception {
        Specification spec = getSpecification();

        Model mo = new DefaultModel();
        mo.attach(new ShareableResource("cpu", 4, 4));
        for (int i = 0; i < 4; i++) {
            Node n = mo.newNode();
            VM v = mo.newVM();
            if (i % 2 == 0) {
                mo.getMapping().addOnlineNode(n);
                mo.getMapping().addRunningVM(v, n);
            } else {
                mo.getMapping().addOfflineNode(n);
                mo.getMapping().addReadyVM(v);
            }

        }


        for (Constraint c : spec.getConstraints()) {
            if (c.isCore()) {
                continue;
            }
            SatConstraint s = null;
            switch (c.id()) {
                case "spread":
                case "gather":
                case "running":
                case "sleeping":
                case "ready":
                case "killed":
                case "lonely":
                case "root":
                    s = c.instantiate(Arrays.asList(mo.getMapping().getAllVMs()));
                    break;
                case "sequentialVMTransitions":
                    s = c.instantiate(Arrays.asList(new ArrayList<>(mo.getMapping().getAllVMs())));
                    break;
                case "online":
                case "offline":
                case "quarantine":
                    s = c.instantiate(Arrays.asList(mo.getMapping().getAllNodes()));
                    break;
                case "ban":
                case "fence":
                    s = c.instantiate(Arrays.asList(mo.getMapping().getAllVMs(), mo.getMapping().getAllNodes()));
                    break;
                case "maxOnline":
                case "cumulatedRunningCapacity":
                case "singleRunningCapacity":
                    s = c.instantiate(Arrays.asList(mo.getMapping().getAllNodes(), 3));
                    break;
                case "cumulatedResourceCapacity":
                case "singleResourceCapacity":
                    s = c.instantiate(Arrays.asList(mo.getMapping().getAllNodes(), "cpu", 3));
                    break;
                case "overbook":
                    s = c.instantiate(Arrays.asList(mo.getMapping().getAllNodes(), "cpu", 1.2));
                    break;
                case "preserve":
                    s = c.instantiate(Arrays.asList(mo.getMapping().getAllVMs(), "cpu", 2));
                    break;
                case "split":
                    s = c.instantiate(Arrays.asList(
                            Arrays.asList(mo.getMapping().getReadyVMs(), mo.getMapping().getRunningVMs())
                    ));
                    break;
                case "splitAmong":
                    s = c.instantiate(Arrays.asList(
                            Arrays.asList(mo.getMapping().getRunningVMs(), mo.getMapping().getReadyVMs()),
                            Arrays.asList(mo.getMapping().getOfflineNodes(), mo.getMapping().getOnlineNodes())
                    ));
                    break;
                case "among":
                    s = c.instantiate(Arrays.asList(
                            mo.getMapping().getAllVMs(),
                            Arrays.asList(mo.getMapping().getOfflineNodes(), mo.getMapping().getOnlineNodes())
                    ));
                    break;
                default:
                    System.err.println("Unsupported constraint specification: " + c.id());
            }
            Assert.assertNotNull(s);
            System.out.println(s);
            System.out.flush();

        }
    }
}
