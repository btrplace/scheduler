/*
 * Copyright  2023 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.json.JSON;
import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.model.view.network.Network;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.constraint.mttr.CMinMTTR;
import org.btrplace.scheduler.choco.extensions.ChocoUtils;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Unit tests related to the opened issues
 *
 * @author Fabien Hermenier
 */
public class IssuesTest {

    /**
     * Another test related to issue #5.
     *
     */
    @Test
    public void testIssue5a() throws SchedulerException, ContradictionException {

        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        ShareableResource resources = new ShareableResource("vcpu", 1, 1);
        resources.setCapacity(n1, 2);
        resources.setCapacity(n2, 2);

        Mapping map = model.getMapping().on(n1, n2).off(n3).run(n1, vm1, vm2);
        model.attach(resources);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .build();

        List<IntVar> VMsOnAllNodes = rp.getNbRunningVMs();

        int NUMBER_OF_NODE = map.getAllNodes().size();

        // Each element is the number of VMs on each node
        IntVar[] vmsOnInvolvedNodes = new IntVar[NUMBER_OF_NODE];

        BoolVar[] busy = new BoolVar[NUMBER_OF_NODE];

        rp.getEnd().updateUpperBound(10, Cause.Null);
        int i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        for (Node n : map.getAllNodes()) {
            vmsOnInvolvedNodes[i] = rp.getModel().intVar("nVMs", -1, maxVMs, true);
            IntVar state = rp.getNodeAction(n).getState();
            // If the node is offline -> the temporary variable is -1, otherwise, it equals the number of VMs on that node
            Constraint elem = rp.getModel().element(vmsOnInvolvedNodes[i], new IntVar[]{rp.getModel().intVar(-1), VMsOnAllNodes.get(rp.getNode(n))}, state, 0);
            rp.getModel().post(elem);

            // IF the node is online and hosting VMs -> busy = 1.
            busy[i] = rp.getModel().boolVar("busy" + n);
            ChocoUtils.postIfOnlyIf(rp, busy[i], rp.getModel().arithm(vmsOnInvolvedNodes[i], ">=", 1));
            i++;
        }

        // idle is equals the number of vmsOnInvolvedNodes with value 0. (The node without VM)
        IntVar idle = rp.getModel().intVar("Nidles", 0, NUMBER_OF_NODE, true);
        rp.getModel().post(rp.getModel().count(0, vmsOnInvolvedNodes, idle));
        // idle should be less than Amount for MaxSN (0, in this case)
        rp.getModel().post(rp.getModel().arithm(idle, "<=", 0));

        // Extract all the state of the involved nodes (all nodes in this case)
        IntVar[] states = new IntVar[NUMBER_OF_NODE];
        int j = 0;
        for (Node n : map.getAllNodes()) {
            states[j++] = rp.getNodeAction(n).getState();
        }

        // In case the number of VMs is inferior to the number of online nodes, some nodes have to shutdown
        // to satisfy the constraint. This could be express as:
        // The addition of the idle nodes and busy nodes should be equals the number of online nodes.
        IntVar sumStates = rp.getModel().intVar("sumStates", 0, 1000, true);
        rp.getModel().post(rp.getModel().sum(states, "=", sumStates));
        IntVar sumBusy = rp.getModel().intVar("sumBusy", 0, 1000, true);
        rp.getModel().post(rp.getModel().sum(states, "=", sumBusy));
        IntVar sumIB = rp.getModel().intVar("ib", 0, 1000, true);
        @SuppressWarnings("unused")
        Task task = new Task(sumBusy, idle, sumIB);
        rp.getModel().post(rp.getModel().arithm(sumStates, "=", sumIB));//solver.eq(sumStates, sumIB));

        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
    }

    /**
     * Test a suspicious bug in issue #5
     */
    @Test
    public void testIssue5b() throws SchedulerException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();

        Mapping map = model.getMapping().on(n1, n2, n3)
                .run(n2, vm1, vm2, vm3, vm4);


        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .build();

        List<IntVar> nodes_state = rp.getNbRunningVMs();
        IntVar[] nodeVM = new IntVar[map.getAllNodes().size()];

        int i = 0;

        for (Node n : map.getAllNodes()) {
            nodeVM[i++] = nodes_state.get(rp.getNode(n));
        }
        IntVar idle = rp.getModel().intVar("Nidles", 0, map.getAllNodes().size(), true);

        rp.getModel().post(rp.getModel().count(0, nodeVM, idle));
        // Amount of maxSpareNode =  1
        rp.getModel().post(rp.getModel().arithm(idle, "<=", 1));

        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
    }

    /**
     * Test a suspicious bug in issue #5
     */
    @Test
    public void testIssue5c() throws SchedulerException {

        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();


        Mapping map = model.getMapping().on(n1, n2, n3)
                .run(n2, vm1, vm2, vm3, vm4);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .build();

        List<IntVar> nodes_state = rp.getNbRunningVMs();
        IntVar[] nodeVM = new IntVar[map.getAllNodes().size()];

        int i = 0;

        for (Node n : map.getAllNodes()) {
            nodeVM[i++] = nodes_state.get(rp.getNode(n));
            //rp.getNodeAction(n).getState().setVal(1);
        }
        IntVar idle = rp.getModel().intVar("Nidles", 0, map.getAllNodes().size(), true);

        rp.getModel().post(rp.getModel().count(0, nodeVM, idle));
        rp.getModel().post(rp.getModel().arithm(idle, "<=", 1));
        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
    }

    @Test
    public void testIssue10() throws SchedulerException, ContradictionException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();

        Mapping map = model.getMapping().on(n1, n2).off(n3).run(n1, vm1, vm2);
        //model.attach(resources);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model).build();
        rp.getNodeAction(n3).getState().instantiateTo(1, Cause.Null);  // n3 goes online
        rp.getModel().post(rp.getModel().arithm(rp.getEnd(), "<=", 10));
        int NUMBER_OF_NODE = map.getAllNodes().size();
        // Extract all the state of the involved nodes (all nodes in this case)
        List<IntVar> VMsOnAllNodes = rp.getNbRunningVMs();
        // Each element is the number of VMs on each node
        IntVar[] vmsOnInvolvedNodes = new IntVar[NUMBER_OF_NODE];
        BoolVar[] idles = new BoolVar[NUMBER_OF_NODE];
        int i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        for (Node n : map.getAllNodes()) {
            vmsOnInvolvedNodes[i] = rp.getModel().intVar("nVMs" + n, -1, maxVMs, true);
            IntVar state = rp.getNodeAction(n).getState();
            // If the node is offline -> the temporary variable is 1, otherwise, it equals the number of VMs on that node
            Constraint elem = rp.getModel().element(vmsOnInvolvedNodes[i], new IntVar[]{rp.getModel().intVar(-1), VMsOnAllNodes.get(rp.getNode(n))}, state, 0);
            rp.getModel().post(elem);
            // IF number of VMs on a node is 0 -> Idle
            idles[i] = rp.getModel().boolVar("idle" + n);
            ChocoUtils.postIfOnlyIf(rp, idles[i], rp.getModel().arithm(vmsOnInvolvedNodes[i], "=", 0));
            i++;
        }
        IntVar sum = rp.getModel().intVar("sum", 0, 1000, true);
        rp.getModel().post(rp.getModel().sum(idles, "=", sum));
        // idle should be less than Amount for MaxSN (0, in this case)
        rp.getModel().post(rp.getModel().arithm(sum, "=", 0));
        System.err.flush();
        CMinMTTR obj = new CMinMTTR();
        obj.inject(new DefaultParameters(), rp);
        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
    }

    /**
     * Unit test derived from Issue 16.
     */
    @Test
    public void test16b() throws SchedulerException {
        Model model = new DefaultModel();
        Node n1 = model.newNode();
        Node n2 = model.newNode();
        Node n3 = model.newNode();
        Node n4 = model.newNode();
        VM vm1 = model.newVM();
        VM vm2 = model.newVM();
        VM vm3 = model.newVM();
        VM vm4 = model.newVM();
        VM vm5 = model.newVM();
        VM vm6 = model.newVM();


        model.getMapping().on(n1, n2, n3, n4)
                .run(n1, vm1, vm2)
                .run(n2, vm3, vm4)
                .run(n3, vm5, vm6);

        Set<SatConstraint> ctrsC = new HashSet<>();
        Set<VM> vms1 = new HashSet<>(Arrays.asList(vm1, vm3, vm5));
        Set<VM> vms2 = new HashSet<>(Arrays.asList(vm2, vm4, vm6));

        ctrsC.add(new Spread(vms1));
        ctrsC.add(new Spread(vms2));
        ctrsC.add(new Fence(vm3, Collections.singleton(n1)));

        Offline off = new Offline(n2);
        ctrsC.add(off);
        ChocoScheduler cra = new DefaultChocoScheduler();
        ReconfigurationPlan dp = cra.solve(model, ctrsC);
        Assert.assertNotNull(dp);
    }

    @Test
    public void issue19() {
        Model m = new DefaultModel();
        ShareableResource cpu = new ShareableResource("cpu", 4, 1);
        Node n = m.newNode();
        Node n2 = m.newNode();
        m.attach(cpu);
        Assert.assertEquals(cpu.sumCapacities(Arrays.asList(n, n2), true), 8);
    }

    @Test
    public void issue33() throws SchedulerException, ContradictionException {
        Model mo = new DefaultModel();
        Node n = mo.newNode();
        VM v = mo.newVM();
        mo.getMapping().addOnlineNode(n);
        mo.getMapping().addRunningVM(v, n);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(mo)
                .setNextVMsStates(Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.singleton(v),
                        Collections.emptySet())
                .build();

        NodeTransition na = rp.getNodeAction(n);
        na.getStart().instantiateTo(0, Cause.Null);
        na.getEnd().instantiateTo(1, Cause.Null);
        VMTransition vma = rp.getVMAction(v);
        vma.getStart().instantiateTo(0, Cause.Null);
        vma.getEnd().instantiateTo(1, Cause.Null);

        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNull(plan);
    }

    @Test
    public void issue72() {
        String input = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[9,8,7,6,5,4,3,2,1,0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[19,18,17,16,15,14,13,12,11,10]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":32768,\"1\":32768},\"rcId\":\"mem\",\"id\":\"shareableResource\",\"defCapacity\":8192,\"vms\":{\"11\":1024,\"12\":1024,\"13\":1024,\"14\":1024,\"15\":1024,\"16\":1024,\"17\":1024,\"18\":1024,\"19\":1024,\"0\":1024,\"1\":1024,\"2\":1024,\"3\":1024,\"4\":1024,\"5\":1024,\"6\":1024,\"7\":1024,\"8\":1024,\"9\":1024,\"10\":1024}},{\"defConsumption\":0,\"nodes\":{\"0\":700,\"1\":700},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":8000,\"vms\":{\"11\":0,\"12\":0,\"13\":0,\"14\":0,\"15\":0,\"16\":70,\"17\":0,\"18\":60,\"19\":0,\"0\":100,\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"7\":0,\"8\":90,\"9\":0,\"10\":0}}]},\"constraints\":[],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(input));
        ChocoScheduler s = new DefaultChocoScheduler();
        s.setTimeLimit(-1);
        i.getModel().detach(ShareableResource.get(i.getModel(), "mem"));
        i.getModel().detach(ShareableResource.get(i.getModel(), "cpu"));
        List<SatConstraint> cstrs = new ArrayList<>();
        ReconfigurationPlan p = s.solve(i.getModel(), cstrs, i.getOptConstraint());
        Assert.assertTrue(p.getActions().isEmpty());
    }

    @Test
    public void issue72b() throws SchedulerException {
        Model mo = new DefaultModel();
        Mapping ma = mo.getMapping();
        ShareableResource rcCPU = new ShareableResource("cpu", 2, 0);
        mo.attach(rcCPU);
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            nodes.add(mo.newNode());
            ma.addOnlineNode(nodes.get(i));
        }
        for (int i = 0; i < 1; i++) {
            VM v = mo.newVM();
            ma.addRunningVM(v, nodes.get(0));
        }
        for (int i = 0; i < 2; i++) {
            VM v = mo.newVM();
            ma.addRunningVM(v, nodes.get(1));
        }
        DefaultParameters ps = new DefaultParameters();
        ReconfigurationPlan p = new DefaultChocoScheduler(ps).solve(mo, new ArrayList<>());
        Assert.assertNotNull(p);
    }

    @Test
    public void issue72c() {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[9,8,7,6,5,4,3,2,1,0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[19,18,17,16,15,14,13,12,11,10]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":32768,\"1\":32768},\"rcId\":\"mem\",\"id\":\"shareableResource\",\"defCapacity\":8192,\"vms\":{\"11\":1024,\"12\":1024,\"13\":1024,\"14\":1024,\"15\":1024,\"16\":1024,\"17\":1024,\"18\":1024,\"19\":1024,\"0\":1024,\"1\":1024,\"2\":1024,\"3\":1024,\"4\":1024,\"5\":1024,\"6\":1024,\"7\":1024,\"8\":1024,\"9\":1024,\"10\":1024}},{\"defConsumption\":0,\"nodes\":{\"0\":700,\"1\":700},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":8000,\"vms\":{\"11\":0,\"12\":0,\"13\":0,\"14\":0,\"15\":0,\"16\":50,\"17\":0,\"18\":0,\"19\":0,\"0\":0,\"1\":0,\"2\":0,\"3\":40,\"4\":0,\"5\":90,\"6\":0,\"7\":0,\"8\":0,\"9\":0,\"10\":0}}]},\"constraints\":[],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        s.doOptimize(true);
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
        Assert.assertTrue(p.getActions().isEmpty());
        s.doRepair(false);
        p = s.solve(i.getModel(), i.getSatConstraints(), i.getOptConstraint());
        Assert.assertTrue(p.getActions().isEmpty());
    }

    @Test
    public void issue72d() {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[9,8,7,6,5,4,3,2,1,0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[19,18,17,16,15,14,13,12,11,10]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":32768,\"1\":32768},\"rcId\":\"mem\",\"id\":\"shareableResource\",\"defCapacity\":8192,\"vms\":{\"11\":1024,\"12\":1024,\"13\":1024,\"14\":1024,\"15\":1024,\"16\":1024,\"17\":1024,\"18\":1024,\"19\":1024,\"0\":1024,\"1\":1024,\"2\":1024,\"3\":1024,\"4\":1024,\"5\":1024,\"6\":1024,\"7\":1024,\"8\":1024,\"9\":1024,\"10\":1024}},{\"defConsumption\":0,\"nodes\":{\"0\":700,\"1\":700},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":8000,\"vms\":{\"11\":0,\"12\":0,\"13\":0,\"14\":0,\"15\":0,\"16\":50,\"17\":0,\"18\":0,\"19\":0,\"0\":0,\"1\":0,\"2\":0,\"3\":40,\"4\":0,\"5\":90,\"6\":0,\"7\":0,\"8\":0,\"9\":0,\"10\":0}}]},\"constraints\":[],\"objective\":{\"id\":\"minimizeMTTR\"}}\n";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();

        s.doOptimize(true);
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);

        Assert.assertTrue(p.getActions().isEmpty());
        s.doRepair(true);
        p = s.solve(i);
        Assert.assertTrue(p.getActions().isEmpty());
    }

    @Test
    public void testIssue86() {
        Model mo = new DefaultModel();
        mo.getMapping().addReadyVM(mo.newVM());
        mo.getMapping().addReadyVM(mo.newVM());
        mo.getMapping().addReadyVM(mo.newVM());
        mo.getMapping().addOnlineNode(mo.newNode());
        mo.getMapping().addOnlineNode(mo.newNode());
        mo.getMapping().addOnlineNode(mo.newNode());

        List<SatConstraint> cstrs = mo.getMapping().getAllVMs().stream().map(Running::new).collect(Collectors.toList());
        cstrs.add(new Spread(mo.getMapping().getAllVMs(), false));
        cstrs.addAll(mo.getMapping().getOnlineNodes().stream().map(n -> new RunningCapacity(n, 1)).collect(Collectors.toList()));
        Instance i = new Instance(mo, cstrs, new MinMTTR());
        ChocoScheduler s = new DefaultChocoScheduler();
        s.doOptimize(false);
        s.doRepair(false);
        ReconfigurationPlan p = s.solve(i.getModel(), i.getSatConstraints(), i.getOptConstraint());
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getActions().size(), 3);
        s.doRepair(true);
        p = s.solve(i.getModel(), i.getSatConstraints(), i.getOptConstraint());
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getActions().size(), 3);
    }

    @Test
    public void issue87() {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[3,2],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[1]},\"2\":{\"sleepingVMs\":[],\"runningVMs\":[4]},\"3\":{\"sleepingVMs\":[],\"runningVMs\":[]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[]},\"constraints\":[{\"vm\":0,\"continuous\":false,\"id\":\"running\"},{\"vm\":1,\"continuous\":false,\"id\":\"running\"},{\"vm\":2,\"continuous\":false,\"id\":\"running\"},{\"vm\":3,\"continuous\":false,\"id\":\"running\"},{\"vm\":4,\"continuous\":false,\"id\":\"running\"},{\"amount\":1,\"nodes\":[0],\"continuous\":false,\"id\":\"runningCapacity\"},{\"amount\":1,\"nodes\":[1],\"continuous\":false,\"id\":\"runningCapacity\"},{\"amount\":1,\"nodes\":[2],\"continuous\":false,\"id\":\"runningCapacity\"},{\"amount\":2,\"nodes\":[3],\"continuous\":false,\"id\":\"runningCapacity\"}],\"objective\":{\"id\":\"minimizeMTTR\"}}\n";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        s.doOptimize(true);
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 2);
    }

    public static void main(String[] args) throws Exception {
        testIssue89();
    }

    @Test
    public static void testIssue89() {
        final Model model = new DefaultModel();
        final Mapping mapping = model.getMapping();

        final Node node0 = model.newNode(0);
        final int[] ids0 = {1, 45, 43, 40, 39, 38, 82, 80, 79, 78, 30, 75, 18, 16, 15, 14, 60, 9, 55, 54, 50, 48};
        final Node node1 = model.newNode(1);
        final int[] ids1 = {84, 83, 81, 77, 73, 71, 64, 63, 62, 57, 53, 52, 47, 46, 44, 41, 34, 31, 28, 25, 13, 8, 6, 4, 3, 0};
        final Node node2 = model.newNode(2);
        final int[] ids2 = {21, 67, 42, 36, 35, 33, 76, 74, 23, 69, 68, 20, 61, 12, 11, 10, 5, 51};
        final Node node3 = model.newNode(3);
        final int[] ids3 = {2, 66, 86, 85, 37, 32, 29, 27, 26, 72, 24, 70, 22, 19, 65, 17, 59, 58, 56, 7, 49};

        final ShareableResource cpu = new ShareableResource("cpu", 45, 1);
        final ShareableResource mem = new ShareableResource("mem", 90, 2);

        populateNodeVm(model, mapping, node0, ids0);
        populateNodeVm(model, mapping, node1, ids1);
        populateNodeVm(model, mapping, node2, ids2);
        populateNodeVm(model, mapping, node3, ids3);
        model.attach(cpu);
        model.attach(mem);

        final Collection<SatConstraint> satConstraints =
                new ArrayList<>();
        // We want to cause Node 3 to go offline to see how the VMs hosted on that
        // node will get rebalanced.
        satConstraints.add(new Offline(node3));
        final OptConstraint optConstraint = new MinMTTR();
        DefaultChocoScheduler scheduler = new DefaultChocoScheduler();
        scheduler.doOptimize(false);
        scheduler.doRepair(true);
        scheduler.setTimeLimit(60000);
        ReconfigurationPlan plan = scheduler.solve(
                model, satConstraints, optConstraint);
        System.out.println(scheduler.getStatistics());
        Assert.assertTrue(plan.isApplyable());


        satConstraints.clear();
        // This is somewhat similar to making Node 3 going offline by ensuring that
        // all VMs can no longer get hosted on that node.
        satConstraints.addAll(mapping.getAllVMs().stream().map(vm -> new Ban(vm, Collections.singletonList(node3))).collect(Collectors.toList()));

        scheduler = new DefaultChocoScheduler();
        scheduler.doOptimize(false);
        scheduler.doRepair(true);
        plan = scheduler.solve(model, satConstraints, optConstraint);
        Assert.assertTrue(plan.isApplyable());
    }

    private static void populateNodeVm(final Model model,
                                       final Mapping mapping, final Node node, final int[] ids) {

        mapping.addOnlineNode(node);
        for (final int id : ids) {
            final VM vm = model.newVM(id);
            mapping.addRunningVM(vm, node);
        }
    }

    /**
     * A helper to check that an instance has a solution.
     *
     * @param file the serialised instance. Must be in 'src/test/resources/'
     */
    private static void computable(String file) {
        Instance i = JSON.readInstance(new File("src/test/resources/" + file));
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan p = s.solve(i);
        System.out.println(s.getStatistics());
        Assert.assertNotNull(p);
        System.out.println(p.getSize() + " action(s)");
    }

    @Test
    public void testIssue93() {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[1]},\"2\":{\"sleepingVMs\":[],\"runningVMs\":[2]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{\"0\":{\"memUsed\":204},\"1\":{\"memUsed\":204},\"2\":{\"memUsed\":204}}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":8,\"1\":10,\"2\":10},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":0,\"vms\":{\"0\":8,\"1\":1,\"2\":1}},{\"routing\":{\"type\":\"default\"},\"switches\":[{\"id\":0,\"capacity\":-1}],\"links\":[{\"physicalElement\":{\"id\":2,\"type\":\"node\"},\"id\":0,\"capacity\":1000,\"switch\":0},{\"physicalElement\":{\"id\":1,\"type\":\"node\"},\"id\":1,\"capacity\":1000,\"switch\":0},{\"physicalElement\":{\"id\":0,\"type\":\"node\"},\"id\":2,\"capacity\":1000,\"switch\":0}],\"id\":\"net\"}]},\"constraints\":[{\"rc\":\"cpu\",\"amount\":6,\"nodes\":[0],\"continuous\":false,\"id\":\"resourceCapacity\"}],\"objective\":{\"id\":\"minimizeMTTR\"}}\n";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        s.doOptimize(true);
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
    }

    @Test
    public void testIssue100() {
        computable("issue-100.json.gz");
    }

    @Test
    public void testIssue101() {
        computable("issue-101.json.gz");
    }

    @Test
    public void testFoo() {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[1]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":2,\"nodes\":{},\"rcId\":\"CPU\",\"id\":\"shareableResource\",\"defCapacity\":7,\"vms\":{}}]},\"constraints\":[{\"nodes\":[0],\"vm\":0,\"continuous\":false,\"id\":\"ban\"},{\"nodes\":[1],\"vm\":1,\"continuous\":false,\"id\":\"ban\"},{\"rc\":\"CPU\",\"amount\":4,\"vm\":0,\"id\":\"preserve\"},{\"rc\":\"CPU\",\"amount\":4,\"vm\":1,\"id\":\"preserve\"}],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
    }

    @Test
    public void testIssue131() {
        for (int id = 0; id <= 4; id++) {
            System.out.println("--- " + id + " ---");
            computable("issue-131-" + id + ".json.gz");
        }
    }

    @Test
    public void testIssue117() {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[5,4],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[1]},\"2\":{\"sleepingVMs\":[],\"runningVMs\":[3,2]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{\"4\":{\"placeHolder\":true},\"5\":{\"placeHolder\":true}}},\"views\":[{\"defConsumption\":2,\"nodes\":{},\"rcId\":\"mem\",\"id\":\"shareableResource\",\"defCapacity\":6,\"vms\":{\"1\":4,\"4\":4,\"5\":2}},{\"defConsumption\":2,\"nodes\":{},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":6,\"vms\":{\"0\":4,\"1\":4,\"4\":4,\"5\":2}}]},\"constraints\":[{\"vm\":4,\"continuous\":false,\"id\":\"running\"},{\"continuous\":false,\"id\":\"spread\",\"vms\":[1,3,4]},{\"vm\":5,\"continuous\":false,\"id\":\"running\"}],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan plan = s.solve(i);
        Assert.assertNotNull(plan);
    }

    @Test
    public void testIssue171() {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[3],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[2]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[1]},\"2\":{\"sleepingVMs\":[],\"runningVMs\":[0]},\"3\":{\"sleepingVMs\":[],\"runningVMs\":[]},\"4\":{\"sleepingVMs\":[],\"runningVMs\":[]},\"5\":{\"sleepingVMs\":[],\"runningVMs\":[]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":4,\"1\":4,\"2\":4,\"3\":4,\"4\":2147483,\"5\":2147483},\"rcId\":\"memory\",\"id\":\"shareableResource\",\"defCapacity\":0,\"vms\":{\"0\":4,\"1\":4,\"2\":4}}]},\"constraints\":[{\"vm\":0,\"id\":\"running\"},{\"vm\":1,\"id\":\"running\"},{\"vm\":2,\"id\":\"running\"},{\"node\":5,\"id\":\"online\"},{\"node\":4,\"id\":\"online\"},{\"node\":3,\"id\":\"online\"},{\"node\":2,\"id\":\"online\"},{\"node\":1,\"id\":\"online\"},{\"node\":0,\"id\":\"online\"},{\"continuous\":true,\"id\":\"spread\",\"vms\":[1,2]},{\"continuous\":false,\"id\":\"gather\",\"vms\":[2,3]},{\"vm\":3,\"id\":\"running\"}],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(buf));
        i.getModel().getMapping().remove(new Node(3));
        ShareableResource mem = ShareableResource.get(i.getModel(), "memory");
        mem.unset(new Node(5));
        mem.unset(new Node(4));
        mem.unset(new Node(3));
        i.getSatConstraints().removeIf(Online.class::isInstance);
        i.getSatConstraints().removeIf(Spread.class::isInstance);
        i.getSatConstraints().removeIf(Gather.class::isInstance);
        i.getSatConstraints().removeIf(r -> r instanceof Running && !r.getInvolvedVMs().contains(new VM(3)));
        i.getModel().getMapping().remove(new VM(0));
        i.getModel().getMapping().remove(new Node(2));
        i.getModel().getMapping().remove(new Node(5));
        i.getModel().getMapping().remove(new Node(4));
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan plan = s.solve(i);
        Assert.assertNotNull(plan);
    }

    /**
     * Boot plenty of VMs.
     */
    @Test
    public void testIssue176() {
        Model mo = new DefaultModel();
        ShareableResource slots = new ShareableResource("slots");
        for (int i = 0; i < 4; i++) {
            final Node no = mo.newNode();
            mo.getMapping().on(no);
            slots.setCapacity(no, 100);
            for (int j = 0; j < 5; j++) {
                mo.getMapping().addRunningVM(mo.newVM(), no);
            }
        }
        // 95 VMs * nbNodes can be hosted.
        for (int i = 0; i < 95 * mo.getMapping().getNbNodes(); i++) {
            mo.getMapping().addReadyVM(mo.newVM());
        }

        final List<SatConstraint> cstrs = new ArrayList<>(Running.newRunning(mo.getMapping().getReadyVMs()));
        final Instance ii = new Instance(mo, cstrs, new MinMigrations());
        ChocoScheduler sched = new DefaultChocoScheduler();
        Assert.assertNotNull(sched.solve(ii));
    }

    @Test
    public void testIssue241() {
        DefaultModel model = new DefaultModel();
        Mapping map = model.getMapping();

        ArrayList<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Node node = model.newNode();
            nodes.add(node);
            map.addOnlineNode(node);
            for (int j = 0; j < 2; j++) {
                VM vm = model.newVM();
                model.getAttributes().put(vm, "memUsed", 2);
                map.addRunningVM(vm, node);
            }
        }

        ChocoScheduler sched = new DefaultChocoScheduler();
        Network.createDefaultNetwork(model);
        ArrayList<SatConstraint> constraints = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            constraints.add(new Offline(nodes.get(i)));
        }
        ReconfigurationPlan plan = sched.solve(model, constraints);
        Assert.assertNotNull(plan);
        System.out.println(plan);
        System.out.println(sched.getStatistics());
    }
}
