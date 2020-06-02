/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.json.plan;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONs;
import org.btrplace.json.model.ModelConverter;
import org.btrplace.model.DefaultModel;
import org.btrplace.model.Mapping;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.Action;
import org.btrplace.plan.event.ActionVisitor;
import org.btrplace.plan.event.Allocate;
import org.btrplace.plan.event.AllocateEvent;
import org.btrplace.plan.event.BootNode;
import org.btrplace.plan.event.BootVM;
import org.btrplace.plan.event.MigrateVM;
import org.btrplace.plan.event.ResumeVM;
import org.btrplace.plan.event.ShutdownNode;
import org.btrplace.plan.event.ShutdownVM;
import org.btrplace.plan.event.SubstitutedVMEvent;
import org.btrplace.plan.event.SuspendVM;
import org.btrplace.plan.event.VMEvent;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Objects;

/**
 * Unit tests for {@link ReconfigurationPlanConverter}.
 *
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanConverterTest {

    @Test
    public void testAccessors() {
        ModelConverter mc = new ModelConverter();
        ReconfigurationPlanConverter rcp = new ReconfigurationPlanConverter(mc);
        Assert.assertEquals(rcp.getModelConverter(), mc);
    }

    @Test
    public void testBundle() throws JSONConverterException {
        Model mo = new DefaultModel();
        VM vm1 = mo.newVM();
        VM vm2 = mo.newVM();
        VM vm3 = mo.newVM();
        VM vm4 = mo.newVM();
        VM vm5 = mo.newVM();
        Node n1 = mo.newNode();
        Node n2 = mo.newNode();
        Node n3 = mo.newNode();

        Mapping map = mo.getMapping();
        map.addOnlineNode(n1);
        map.addOfflineNode(n2);
        map.addOnlineNode(n3);
        map.addReadyVM(vm1);
        map.addRunningVM(vm2, n1);
        map.addRunningVM(vm3, n1);
        map.addSleepingVM(vm4, n3);
        map.addRunningVM(vm5, n3);

        ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootVM(vm1, n3, 1, 2));

        final MigrateVM mm = new MigrateVM(vm3, n3, n2, 4, 5, 4000);
        mm.addEvent(Action.Hook.PRE, new AllocateEvent(vm3, "bar", 5));
        mm.addEvent(Action.Hook.POST, new SubstitutedVMEvent(vm4, vm5));
        mm.addEvent(Action.Hook.POST, new AllocateEvent(vm5, "baz", 4));
        plan.add(mm);
        plan.add(new ShutdownVM(vm5, n3, 3, 4));
        plan.add(new Allocate(vm1, n3, "foo", 5, 3, 5));
        plan.add(new SuspendVM(vm3, n3, n3, 3, 5));
        plan.add(new ResumeVM(vm4, n3, n3, 4, 8));

        plan.add(new BootNode(n2, 2, 5));
        plan.add(new ShutdownNode(n1, 5, 10));

        ReconfigurationPlanConverter rcp =
            ReconfigurationPlanConverter.newBundle();
        String j = rcp.toJSONString(plan);
        ReconfigurationPlan p2 = rcp.fromJSON(j);
        Assert.assertEquals(p2, plan);
    }

    @Test(expectedExceptions = JSONConverterException.class)
    public void testMissingConverter() throws JSONConverterException {
        Model mo = new DefaultModel();
        final Node n0 = mo.newNode();
        mo.getMapping().off(n0);
        final ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(new BootNode(n0, 0, 1));
        ReconfigurationPlanConverter rcp = new ReconfigurationPlanConverter();
        rcp.toJSON(plan);
    }

    @Test
    public void testCustomEvent() throws JSONConverterException {
        Model mo = new DefaultModel();
        final Node n0 = mo.newNode();
        final VM vm = mo.newVM();
        mo.getMapping().on(n0).run(n0, vm);
        final BootVM boot = new BootVM(vm, n0, 0, 1);
        final CustomVMEvent e1 = new CustomVMEvent(vm, 7);
        final CustomVMEvent e2 = new CustomVMEvent(vm, 10);
        boot.addEvent(Action.Hook.PRE, e1);
        boot.addEvent(Action.Hook.POST, e2);

        final ReconfigurationPlan plan = new DefaultReconfigurationPlan(mo);
        plan.add(boot);

        ReconfigurationPlanConverter rcp =
            ReconfigurationPlanConverter.newBundle();
        rcp.register(new CustomVMEventConverter());
        final String json = rcp.toJSON(plan).toJSONString();
        System.out.println(json);
        final ReconfigurationPlan p2 = rcp.fromJSON(json);
        Assert.assertEquals(plan, p2);
    }

    private final static class CustomVMEvent implements VMEvent {

        private final VM vm;

        private final int qty;

        public CustomVMEvent(final VM vm, final int qty) {
            this.vm = vm;
            this.qty = qty;
        }

        @Override
        public VM getVM() {
            return vm;
        }

        @Override
        public boolean apply(final Model m) {
            return true;
        }

        @Override
        public Object visit(final ActionVisitor v) {
            return null;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final CustomVMEvent that = (CustomVMEvent) o;
            return Objects.equals(vm, that.vm) && Objects.equals(qty, that.qty);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vm, qty);
        }
    }

    private final static class CustomVMEventConverter implements EventConverter<CustomVMEvent> {
        @Override
        public String id() {
            return "customID";
        }

        @Override
        public Class<CustomVMEvent> supportedEvent() {
            return CustomVMEvent.class;
        }

        @Override
        public void fillJSON(final CustomVMEvent ev, final JSONObject ob) {
            ob.put(ActionConverter.VM_LABEL, JSONs.elementToJSON(ev.vm));
            ob.put("qty", ev.qty);
        }

        @Override
        public CustomVMEvent fromJSON(final Model mo, final JSONObject ob)
            throws JSONConverterException {

            return new CustomVMEvent(
                JSONs.requiredVM(mo, ob, ActionConverter.VM_LABEL),
                JSONs.requiredInt(ob, "qty"));
        }
    }
}
