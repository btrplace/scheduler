package btrplace.samples;

import btrplace.model.*;
import btrplace.model.constraint.Offline;
import btrplace.model.constraint.Overbook;
import btrplace.model.constraint.Preserve;
import btrplace.model.constraint.Spread;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.ChocoReconfigurationAlgorithm;
import btrplace.solver.choco.DefaultChocoReconfigurationAlgorithm;

import java.util.*;

/**
 * Simple tutorial about the usage of Btrplace.
 * The document associated to the tutorial is available
 * on <a href="https://github.com/fhermeni/btrplace-solver/wiki/GettingStarted"></a>btrplace website.
 *
 * @author Fabien Hermenier
 */
public class GettingStarted implements Sample {

    private static UUID vm1 = new UUID(0, 1);
    private static UUID vm2 = new UUID(0, 2);
    private static UUID vm3 = new UUID(0, 3);
    private static UUID vm4 = new UUID(0, 4);
    private static UUID vm5 = new UUID(0, 5);
    private static UUID vm6 = new UUID(0, 6);

    private static UUID n1 = new UUID(1, 1);
    private static UUID n2 = new UUID(1, 2);
    private static UUID n3 = new UUID(1, 3);
    private static UUID n4 = new UUID(1, 4);

    @Override
    public boolean run() throws Exception {

        Mapping map = makeMapping();

        //Now, we declare views related to
        //the memory and the cpu resources
        ShareableResource rcCPU = makeCPUResourceView();
        ShareableResource rcMem = makeMemResourceView();

        //We create a model that aggregates the mapping and
        //the views
        Model model = new DefaultModel(map);
        model.attach(rcCPU);
        model.attach(rcMem);

        Set<SatConstraint> cstrs = makeConstraints();

        ChocoReconfigurationAlgorithm ra = new DefaultChocoReconfigurationAlgorithm();
        ra.setMaxEnd(6);
        ReconfigurationPlan plan = ra.solve(model, cstrs);
        System.out.println(plan);
        return plan != null;
    }

    /**
     * Make the element mapping that depicts
     * the element state and the VM positions.
     */
    public Mapping makeMapping() {
        Mapping map = new DefaultMapping();

        //4 online nodes
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);

        //Running 6 VMs
        map.addRunningVM(vm3, n1);
        map.addRunningVM(vm2, n1);

        map.addRunningVM(vm1, n3);
        map.addRunningVM(vm5, n3);
        map.addRunningVM(vm4, n3);

        map.addRunningVM(vm6, n4);

        return map;
    }

    /**
     * Declare the physical number of CPUs available on the nodes
     * and the number of virtual CPUs that are currently used by the VMs.
     */
    private ShareableResource makeCPUResourceView() {
        ShareableResource rc = new ShareableResource("cpu");
        rc.set(n1, 8);
        rc.set(n2, 8);
        rc.set(n3, 8);
        rc.set(n4, 8);

        rc.set(vm1, 2);
        rc.set(vm2, 3);
        rc.set(vm3, 4);
        rc.set(vm4, 3);
        rc.set(vm5, 3);
        rc.set(vm6, 5);

        return rc;
    }

    /**
     * Declare the physical number of CPUs available on the nodes
     * and the number of virtual CPUs that are currently used by the VMs.
     */
    private ShareableResource makeMemResourceView() {
        ShareableResource rc = new ShareableResource("mem");
        rc.set(n1, 7);
        rc.set(n2, 7);
        rc.set(n3, 7);
        rc.set(n4, 7);

        rc.set(vm1, 2);
        rc.set(vm2, 2);
        rc.set(vm3, 4);
        rc.set(vm4, 3);
        rc.set(vm5, 2);
        rc.set(vm6, 4);

        return rc;
    }

    private Set<SatConstraint> makeConstraints() {
        Set<SatConstraint> cstrs = new HashSet<SatConstraint>();

        //We disallow CPU and memory overbooking, so each unit of virtual resource
        //consumes one unit of physical resource on every nodes
        Set<UUID> allNodes = new HashSet<UUID>(Arrays.asList(n1, n2, n3, n4));
        cstrs.add(new Overbook(allNodes, "cpu", 1));
        cstrs.add(new Overbook(allNodes, "mem", 1));

        //VMs VM2 and VM3 must be running on distinct nodes
        cstrs.add(new Spread(new HashSet<UUID>(Arrays.asList(vm2, vm3))));

        //VM VM1 must have at least 3 virtual CPU dedicated to it
        cstrs.add(new Preserve(Collections.singleton(vm1), "cpu", 3));

        //node N4 must be offline
        cstrs.add(new Offline(Collections.singleton(n4)));

        return cstrs;
    }

    public static void main(String[] args) throws Exception {
        GettingStarted s = new GettingStarted();
        System.exit(s.run() ? 0 : 1);
    }
}
