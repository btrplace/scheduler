package btrplace.solver.choco.objective.minMTTR;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.DefaultReconfigurationProblemBuilder;
import btrplace.solver.choco.MappingBuilder;
import btrplace.solver.choco.ReconfigurationProblem;
import btrplace.solver.choco.chocoUtil.ChocoUtils;
import btrplace.test.PremadeElements;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.ElementV;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public class MinMTTRTest implements PremadeElements {

    @Test
    public void testObjectiveFuntion() throws SolverException, ContradictionException {
        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 2);
        resources.set(n2, 2);
        Mapping map = new MappingBuilder().on(n1, n2).off(n3).run(n1, vm1, vm2).build();
        Model model = new DefaultModel(map);
        model.attach(resources);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model).labelVariables().build();
        CPSolver solver = rp.getSolver();
        rp.getNodeAction(n3).getState().setVal(1);  // n3 goes online
        solver.post(solver.leq(rp.getEnd(), 10));
        int NUMBER_OF_NODE = map.getAllNodes().size();
        // Extract all the state of the involved nodes (all nodes in this case)
        IntDomainVar[] VMsOnAllNodes = rp.getNbRunningVMs();
        // Each element is the number of VMs on each node
        IntDomainVar[] vmsOnInvolvedNodes = new IntDomainVar[NUMBER_OF_NODE];
        IntDomainVar[] idles = new IntDomainVar[NUMBER_OF_NODE];
        int i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        for (UUID n : map.getAllNodes()) {
            vmsOnInvolvedNodes[i] = solver.createBoundIntVar("nVMs"+n, -1, maxVMs);
            IntDomainVar state = rp.getNodeAction(n).getState();
            // If the node is offline -> the temporary variable is 1, otherwise, it equals the number of VMs on that node
            IntDomainVar[] c = new IntDomainVar[]{solver.makeConstantIntVar(-1), VMsOnAllNodes[rp.getNode(n)],
                    state, vmsOnInvolvedNodes[i]};
            solver.post(new ElementV(c, 0, solver.getEnvironment()));
            // IF number of VMs on a node is 0 -> Idle
            idles[i] = solver.createBooleanVar("idle" + n);
            ChocoUtils.postIfOnlyIf(solver, idles[i], solver.eq(vmsOnInvolvedNodes[i], 0));
            i++;
        }
        IntExp Sidle = solver.sum(idles);
        // idle should be less than Amount for MaxSN (0, in this case)
        solver.post(solver.leq(Sidle, 0));
        System.err.flush();
        MinMTTR obj = new MinMTTR();
        obj.inject(rp);
        ChocoLogging.setVerbosity(Verbosity.SEARCH);
        //System.err.println(solver.pretty());
        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
        System.out.println(plan);
        System.out.println(plan.getResult());

    }

}
