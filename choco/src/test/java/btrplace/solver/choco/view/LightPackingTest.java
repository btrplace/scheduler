package btrplace.solver.choco.view;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

public class LightPackingTest {

    @Test
    public void test() throws SolverException {
        ChocoReconfigurationAlgorithm cra = new DefaultChocoReconfigurationAlgorithm();
        cra.addSolverViewBuilder(new LightPacking.Builder());
        cra.setVerbosity(1);

        Model mo = new DefaultModel();
        int nbNodes = 1000;
        int nbVMs = nbNodes * 10;

        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();
            mo.getMapping().addOnlineNode(n);
        }

        for (int i=0; i < nbVMs; i++) {
            VM vm = mo.newVM();
            mo.getMapping().addReadyVM(vm);
        }

        ShareableResource cpu = new ShareableResource("cpu", 16, 1);
        ShareableResource mem = new ShareableResource("mem", 32, 1);

        mo.attach(cpu);
        mo.attach(mem);

        List<?> l = Running.newRunning(mo.getMapping().getAllVMs());

        @SuppressWarnings("unchecked")
        ReconfigurationPlan p = cra.solve(mo, (java.util.Collection<SatConstraint>)l);
        assertNotNull(p);
        System.out.println(cra.getStatistics());
        fail();

    }


}