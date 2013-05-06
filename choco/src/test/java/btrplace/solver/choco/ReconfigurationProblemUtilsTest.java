package btrplace.solver.choco;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.solver.SolverException;
import btrplace.test.PremadeElements;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link btrplace.solver.choco.ReconfigurationProblemUtils}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationProblemUtilsTest implements PremadeElements {

    @Test
    public void testGoodVMsExistence() throws SolverException {
        List<UUID> vms = Arrays.asList(vm1, vm2, vm3, vm4);
        ReconfigurationProblem rp = mock(ReconfigurationProblem.class);
        when(rp.getFutureKilledVMs()).thenReturn(Collections.singleton(vm1));
        when(rp.getFutureReadyVMs()).thenReturn(Collections.singleton(vm2));
        when(rp.getFutureRunningVMs()).thenReturn(Collections.singleton(vm3));
        when(rp.getFutureSleepingVMs()).thenReturn(Collections.singleton(vm4));
        ReconfigurationProblemUtils.checkVMsExistence(rp, vms);
    }

    @Test(expectedExceptions = {SolverException.class}, dependsOnMethods = {"testGoodVMsExistence"})
    public void testBadVMsExistence() throws SolverException {
        List<UUID> vms = Arrays.asList(vm1, vm2);
        ReconfigurationProblem rp = mock(ReconfigurationProblem.class);
        when(rp.getFutureKilledVMs()).thenReturn(Collections.singleton(vm1));
        ReconfigurationProblemUtils.checkVMsExistence(rp, vms);
    }

    @Test
    public void testGoodNodesExistence() throws SolverException {
        List<UUID> ns = Arrays.asList(n1, n2);
        ReconfigurationProblem rp = mock(ReconfigurationProblem.class);
        Mapping ma = new MappingBuilder().on(n1).off(n2).build();
        Model mo = new DefaultModel(ma);
        when(rp.getSourceModel()).thenReturn(mo);
        ReconfigurationProblemUtils.checkNodesExistence(rp, ns);
    }

    @Test(expectedExceptions = {SolverException.class}, dependsOnMethods = {"testGoodNodesExistence"})
    public void testBadNodesExistence() throws SolverException {
        List<UUID> ns = Arrays.asList(n1, n2);
        ReconfigurationProblem rp = mock(ReconfigurationProblem.class);
        Mapping ma = new MappingBuilder().on(n1).build();
        Model mo = new DefaultModel(ma);
        when(rp.getSourceModel()).thenReturn(mo);
        ReconfigurationProblemUtils.checkNodesExistence(rp, ns);
    }
}
