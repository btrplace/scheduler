package btrplace.plan;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.plan.event.BootNode;
import btrplace.plan.event.BootVM;
import btrplace.plan.event.MigrateVM;
import btrplace.plan.event.ShutdownNode;
import btrplace.test.PremadeElements;
import junit.framework.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link DependenciesExtractor}.
 *
 * @author Fabien Hermenier
 */
public class DependenciesExtractorTest implements PremadeElements {

    MigrateVM m1 = new MigrateVM(vm1, n2, n1, 0, 5);
    MigrateVM m2 = new MigrateVM(vm2, n3, n4, 0, 5);
    BootNode b1 = new BootNode(n5, 0, 5);
    BootVM r1 = new BootVM(vm3, n5, 5, 7);
    ShutdownNode s1 = new ShutdownNode(n6, 3, 7);
    MigrateVM m3 = new MigrateVM(vm4, n6, n2, 0, 2);
    MigrateVM m4 = new MigrateVM(vm5, n6, n2, 7, 9);

    /**
     * Disjoint reconfiguration graph, so no dependencies
     */
    @Test
    public void testDisjointGraphs() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n3);
        map.addOnlineNode(n4);
        map.addOnlineNode(n6);
        map.addRunningVM(vm1, n1);
        map.addRunningVM(vm2, n3);
        Model mo = new DefaultModel(map);
        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Assert.assertTrue(ex.visit(m1));
        Assert.assertTrue(ex.visit(m2));
        Assert.assertTrue(ex.visit(s1));
        Assert.assertTrue(ex.getDependencies(m1).isEmpty());
        Assert.assertTrue(ex.getDependencies(m2).isEmpty());
        Assert.assertTrue(ex.getDependencies(s1).isEmpty());
    }

    @Test
    public void testSimpleDependencies() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n5);
        map.addReadyVM(vm3);
        Model mo = new DefaultModel(map);
        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Assert.assertTrue(ex.visit(b1));
        Assert.assertTrue(ex.visit(r1));
        Assert.assertTrue(ex.getDependencies(b1).isEmpty());
        Assert.assertEquals(ex.getDependencies(r1).size(), 1);
        Assert.assertTrue(ex.getDependencies(r1).contains(b1));
    }

    @Test
    public void testNoDependencyDueToTiming() {
        Mapping map = new DefaultMapping();
        map.addOnlineNode(n1);
        map.addOnlineNode(n2);
        map.addOnlineNode(n6);
        map.addRunningVM(vm1, n2);
        map.addRunningVM(vm4, n6);
        Model mo = new DefaultModel(map);
        DependenciesExtractor ex = new DependenciesExtractor(mo);
        Assert.assertTrue(ex.visit(m1));
        Assert.assertTrue(ex.visit(m3));
        Assert.assertTrue(ex.visit(m4));

        Assert.assertTrue(ex.getDependencies(m1).toString(), ex.getDependencies(m1).isEmpty());
        Assert.assertTrue(ex.getDependencies(m3).toString(), ex.getDependencies(m3).isEmpty());
        Assert.assertEquals(ex.getDependencies(m4).toString(), ex.getDependencies(m4).size(), 1);
        Assert.assertTrue(ex.getDependencies(m4).toString(), ex.getDependencies(m4).contains(m1));

    }

}
