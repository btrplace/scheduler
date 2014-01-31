package btrplace.solver.api.cstrSpec.reducer;

import btrplace.model.*;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.spec.SpecReader;
import btrplace.solver.api.cstrSpec.spec.term.Constant;
import btrplace.solver.api.cstrSpec.spec.type.NodeType;
import btrplace.solver.api.cstrSpec.spec.type.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ElementsReducerTest {

    public Constraint makeConstraint(String id) {

        SpecReader ex = new SpecReader();
        try {
            for (Constraint x : ex.getSpecification(new File("src/test/resources/v1.cspec")).getConstraints()) {
                if (x.id().equals(id)) {
                    return x;
                }
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage(), e);
        }
        return null;
    }

    @Test
    public void test() throws Exception {
        Model mo = new DefaultModel();
        Node n0 = mo.newNode();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();

        VM vm0 = mo.newVM();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();

        Mapping m = mo.getMapping();
        m.addOnlineNode(n0);
        m.addOnlineNode(n1);
        m.addOfflineNode(n2);
        m.addRunningVM(vm0, n0);
        m.addReadyVM(vm1);
        m.addSleepingVM(vm2, n1);

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        p.add(new ShutdownNode(n1, 0, 3));
        p.add(new BootVM(vm1, n0, 2, 7));
        p.add(new BootNode(n2, 0, 3));
        p.add(new MigrateVM(vm0, n0, n2, 4, 10));

        Constraint cstr = makeConstraint("offline");

        List<Constant> in = new ArrayList<>();
        in.add(new Constant(Collections.singletonList(n1), new SetType(NodeType.getInstance())));


        ElementsReducer tcr = new ElementsReducer();
        //System.out.println(p.getOrigin().getMapping());
        System.out.println(p);
        ReconfigurationPlan reduced = tcr.reduceVMs(p, cstr, in);
        System.out.println("from:\n" + p.getOrigin().getMapping());
        System.out.println(p);
        System.out.println("VMs Reduced to:\n" + reduced.getOrigin().getMapping() + "\n");
        System.out.println(reduced);

        reduced = tcr.reduceNodes(reduced, cstr, in);
        System.out.println("Nodes Reduced to:\n" + reduced.getOrigin().getMapping() + "\n");
        System.out.println(reduced);

        Assert.fail();
    }
}
