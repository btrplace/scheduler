package btrplace.json.plan;

import btrplace.json.JSONConverterException;
import btrplace.plan.event.Action;
import btrplace.plan.event.*;
import btrplace.test.PremadeElements;
import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ActionConverter}.
 *
 * @author Fabien Hermenier
 */
public class ActionConverterTest implements PremadeElements {

    @Test
    public void testMigrate() throws JSONConverterException {
        MigrateVM a = new MigrateVM(vm1, n1, n2, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testBootVM() throws JSONConverterException {
        BootVM a = new BootVM(vm1, n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testKillVM() throws JSONConverterException {
        KillVM a = new KillVM(vm1, n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testAllocate() throws JSONConverterException {
        Allocate a = new Allocate(vm1, n1, "foo", 4, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }


    @Test
    public void testSuspendVM() throws JSONConverterException {
        SuspendVM a = new SuspendVM(vm1, n1, n2, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testResumeVM() throws JSONConverterException {
        ResumeVM a = new ResumeVM(vm1, n1, n2, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }


    @Test
    public void testForgeVM() throws JSONConverterException {
        ForgeVM a = new ForgeVM(vm1, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }


    @Test
    public void testShutdownVM() throws JSONConverterException {
        ShutdownVM a = new ShutdownVM(vm1, n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testShutdownNode() throws JSONConverterException {
        ShutdownNode a = new ShutdownNode(n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test
    public void testBootNode() throws JSONConverterException {
        BootNode a = new BootNode(n1, 3, 5);
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        Assert.assertEquals(a, ac.fromJSON(o));
    }

    @Test(dependsOnMethods = "testBootNode")
    public void testEvents() throws JSONConverterException {
        BootNode a = new BootNode(n1, 3, 5);
        a.addEvent(Action.Hook.pre, new AllocateEvent(vm1, "foo", 3));
        a.addEvent(Action.Hook.post, new AllocateEvent(vm2, "bar", 5));
        a.addEvent(Action.Hook.post, new AllocateEvent(vm3, "baz", 2));
        ActionConverter ac = new ActionConverter();
        JSONObject o = ac.toJSON(a);
        System.out.println(a + "\n" + ac.fromJSON(o));
        Assert.assertEquals(a, ac.fromJSON(o));
    }

}
