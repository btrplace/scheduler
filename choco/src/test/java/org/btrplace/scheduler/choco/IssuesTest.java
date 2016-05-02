/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.json.JSON;
import org.btrplace.model.*;
import org.btrplace.model.constraint.*;
import org.btrplace.model.view.ShareableResource;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.constraint.mttr.CMinMTTR;
import org.btrplace.scheduler.choco.extensions.ChocoUtils;
import org.btrplace.scheduler.choco.runner.SolvingStatistics;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
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
     * @throws org.btrplace.scheduler.SchedulerException
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

        Solver solver = rp.getSolver();
        List<IntVar> VMsOnAllNodes = rp.getNbRunningVMs();

        int NUMBER_OF_NODE = map.getAllNodes().size();

        // Each element is the number of VMs on each node
        IntVar[] vmsOnInvolvedNodes = new IntVar[NUMBER_OF_NODE];

        BoolVar[] busy = new BoolVar[NUMBER_OF_NODE];

        rp.getEnd().updateUpperBound(10, Cause.Null);
        int i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        for (Node n : map.getAllNodes()) {
            vmsOnInvolvedNodes[i] = VF.bounded("nVMs", -1, maxVMs, rp.getSolver());
            IntVar state = rp.getNodeAction(n).getState();
            // If the node is offline -> the temporary variable is -1, otherwise, it equals the number of VMs on that node
            Constraint elem = IntConstraintFactory.element(vmsOnInvolvedNodes[i], new IntVar[]{VF.fixed(-1, solver), VMsOnAllNodes.get(rp.getNode(n))}, state, 0);
            solver.post(elem);

            // IF the node is online and hosting VMs -> busy = 1.
            busy[i] = VF.bool("busy" + n, rp.getSolver());
            ChocoUtils.postIfOnlyIf(solver, busy[i], IntConstraintFactory.arithm(vmsOnInvolvedNodes[i], ">=", 1));
            i++;
        }

        // idle is equals the number of vmsOnInvolvedNodes with value 0. (The node without VM)
        IntVar idle = VF.bounded("Nidles", 0, NUMBER_OF_NODE, solver);
        solver.post(IntConstraintFactory.count(0, vmsOnInvolvedNodes, idle));
        // idle should be less than Amount for MaxSN (0, in this case)
        solver.post(IntConstraintFactory.arithm(idle, "<=", 0));

        // Extract all the state of the involved nodes (all nodes in this case)
        IntVar[] states = new IntVar[NUMBER_OF_NODE];
        int j = 0;
        for (Node n : map.getAllNodes()) {
            states[j++] = rp.getNodeAction(n).getState();
        }

        // In case the number of VMs is inferior to the number of online nodes, some nodes have to shutdown
        // to satisfy the constraint. This could be express as:
        // The addition of the idle nodes and busy nodes should be equals the number of online nodes.
        IntVar sumStates = VF.bounded("sumStates", 0, 1000, solver);
        solver.post(IntConstraintFactory.sum(states, sumStates));
        IntVar sumBusy = VF.bounded("sumBusy", 0, 1000, solver);
        solver.post(IntConstraintFactory.sum(states, sumBusy));
        IntVar sumIB = VF.bounded("ib", 0, 1000, solver);
        VF.task(sumBusy, idle, sumIB);
        //solver.post(IntConstraintFactory.arithm(sumBusy, "+", idle));
        solver.post(IntConstraintFactory.arithm(sumStates, "=", sumIB));//solver.eq(sumStates, sumIB));

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
        Solver solver = rp.getSolver();
        IntVar idle = VF.bounded("Nidles", 0, map.getAllNodes().size(), solver);

        solver.post(IntConstraintFactory.count(0, nodeVM, idle));
        // Amount of maxSpareNode =  1
        solver.post(IntConstraintFactory.arithm(idle, "<=", 1));

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
        Solver solver = rp.getSolver();
        IntVar idle = VF.bounded("Nidles", 0, map.getAllNodes().size(), solver);

        solver.post(IntConstraintFactory.count(0, nodeVM, idle));
        solver.post(IntConstraintFactory.arithm(idle, "<=", 1));
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
        Solver solver = rp.getSolver();
        rp.getNodeAction(n3).getState().instantiateTo(1, Cause.Null);  // n3 goes online
        solver.post(IntConstraintFactory.arithm(rp.getEnd(), "<=", 10));
        int NUMBER_OF_NODE = map.getAllNodes().size();
        // Extract all the state of the involved nodes (all nodes in this case)
        List<IntVar> VMsOnAllNodes = rp.getNbRunningVMs();
        // Each element is the number of VMs on each node
        IntVar[] vmsOnInvolvedNodes = new IntVar[NUMBER_OF_NODE];
        BoolVar[] idles = new BoolVar[NUMBER_OF_NODE];
        int i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        for (Node n : map.getAllNodes()) {
            vmsOnInvolvedNodes[i] = VF.bounded("nVMs" + n, -1, maxVMs, solver);
            IntVar state = rp.getNodeAction(n).getState();
            // If the node is offline -> the temporary variable is 1, otherwise, it equals the number of VMs on that node
            Constraint elem = IntConstraintFactory.element(vmsOnInvolvedNodes[i], new IntVar[]{VF.fixed(-1, solver), VMsOnAllNodes.get(rp.getNode(n))}, state, 0);
            solver.post(elem);
            // IF number of VMs on a node is 0 -> Idle
            idles[i] = VF.bool("idle" + n, solver);
            ChocoUtils.postIfOnlyIf(solver, idles[i], IntConstraintFactory.arithm(vmsOnInvolvedNodes[i], "=", 0));
            i++;
        }
        IntVar sum = VF.bounded("sum", 0, 1000, solver);
        solver.post(IntConstraintFactory.sum(idles, sum));
        // idle should be less than Amount for MaxSN (0, in this case)
        solver.post(IntConstraintFactory.arithm(sum, "=", 0));
        System.err.flush();
        CMinMTTR obj = new CMinMTTR();
        obj.inject(new DefaultParameters(), rp);
        //System.err.println(solver.toString());
        //ChocoLogging.setLoggingMaxDepth(100);
        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
        System.out.println(plan);
        System.out.println(plan.getResult());
    }

    /**
     * Unit test derived from Issue 16.
     *
     * @throws org.btrplace.scheduler.SchedulerException
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
        Assert.assertEquals(plan, null);
    }

    @Test
    public void issue72() throws Exception {
        String input = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[9,8,7,6,5,4,3,2,1,0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[19,18,17,16,15,14,13,12,11,10]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":32768,\"1\":32768},\"rcId\":\"mem\",\"id\":\"shareableResource\",\"defCapacity\":8192,\"vms\":{\"11\":1024,\"12\":1024,\"13\":1024,\"14\":1024,\"15\":1024,\"16\":1024,\"17\":1024,\"18\":1024,\"19\":1024,\"0\":1024,\"1\":1024,\"2\":1024,\"3\":1024,\"4\":1024,\"5\":1024,\"6\":1024,\"7\":1024,\"8\":1024,\"9\":1024,\"10\":1024}},{\"defConsumption\":0,\"nodes\":{\"0\":700,\"1\":700},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":8000,\"vms\":{\"11\":0,\"12\":0,\"13\":0,\"14\":0,\"15\":0,\"16\":70,\"17\":0,\"18\":60,\"19\":0,\"0\":100,\"1\":0,\"2\":0,\"3\":0,\"4\":0,\"5\":0,\"6\":0,\"7\":0,\"8\":90,\"9\":0,\"10\":0}}]},\"constraints\":[],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(input));
        ChocoScheduler s = new DefaultChocoScheduler();
        s.setTimeLimit(-1);
        i.getModel().detach(ShareableResource.get(i.getModel(), "mem"));
        i.getModel().detach(ShareableResource.get(i.getModel(), "cpu"));
        List<SatConstraint> cstrs = new ArrayList<>();
        ReconfigurationPlan p = s.solve(i.getModel(), cstrs, i.getOptConstraint());
        System.out.println(p);

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
    public void issue72c() throws Exception {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[9,8,7,6,5,4,3,2,1,0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[19,18,17,16,15,14,13,12,11,10]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":32768,\"1\":32768},\"rcId\":\"mem\",\"id\":\"shareableResource\",\"defCapacity\":8192,\"vms\":{\"11\":1024,\"12\":1024,\"13\":1024,\"14\":1024,\"15\":1024,\"16\":1024,\"17\":1024,\"18\":1024,\"19\":1024,\"0\":1024,\"1\":1024,\"2\":1024,\"3\":1024,\"4\":1024,\"5\":1024,\"6\":1024,\"7\":1024,\"8\":1024,\"9\":1024,\"10\":1024}},{\"defConsumption\":0,\"nodes\":{\"0\":700,\"1\":700},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":8000,\"vms\":{\"11\":0,\"12\":0,\"13\":0,\"14\":0,\"15\":0,\"16\":50,\"17\":0,\"18\":0,\"19\":0,\"0\":0,\"1\":0,\"2\":0,\"3\":40,\"4\":0,\"5\":90,\"6\":0,\"7\":0,\"8\":0,\"9\":0,\"10\":0}}]},\"constraints\":[],\"objective\":{\"id\":\"minimizeMTTR\"}}";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        System.out.println(i.getModel());
        s.doOptimize(false);
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertTrue(p.getActions().isEmpty());
        s.doRepair(true);
        p = s.solve(i.getModel(), i.getSatConstraints(), i.getOptConstraint());
        Assert.assertTrue(p.getActions().isEmpty());
    }

    @Test
    public void issue72d() throws Exception {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[9,8,7,6,5,4,3,2,1,0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[19,18,17,16,15,14,13,12,11,10]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":32768,\"1\":32768},\"rcId\":\"mem\",\"id\":\"shareableResource\",\"defCapacity\":8192,\"vms\":{\"11\":1024,\"12\":1024,\"13\":1024,\"14\":1024,\"15\":1024,\"16\":1024,\"17\":1024,\"18\":1024,\"19\":1024,\"0\":1024,\"1\":1024,\"2\":1024,\"3\":1024,\"4\":1024,\"5\":1024,\"6\":1024,\"7\":1024,\"8\":1024,\"9\":1024,\"10\":1024}},{\"defConsumption\":0,\"nodes\":{\"0\":700,\"1\":700},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":8000,\"vms\":{\"11\":0,\"12\":0,\"13\":0,\"14\":0,\"15\":0,\"16\":50,\"17\":0,\"18\":0,\"19\":0,\"0\":0,\"1\":0,\"2\":0,\"3\":40,\"4\":0,\"5\":90,\"6\":0,\"7\":0,\"8\":0,\"9\":0,\"10\":0}}]},\"constraints\":[],\"objective\":{\"id\":\"minimizeMTTR\"}}\n";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        System.out.println(i.getModel());
        s.doOptimize(true);
        ReconfigurationPlan p = s.solve(i);
        Assert.assertNotNull(p);
        System.out.println(p);
        Assert.assertTrue(p.getActions().isEmpty());
        s.doRepair(true);
        p = s.solve(i);
        Assert.assertTrue(p.getActions().isEmpty());
    }

    @Test
    public void testIssue86() throws Exception {
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
        System.out.println(i.getModel());
        s.doOptimize(false);
        s.doRepair(false);
        ReconfigurationPlan p = s.solve(i.getModel(), i.getSatConstraints(), i.getOptConstraint());
        Assert.assertNotNull(p);
        Assert.assertEquals(3, p.getActions().size());
        System.out.println(p);
        s.doRepair(true);
        p = s.solve(i.getModel(), i.getSatConstraints(), i.getOptConstraint());
        Assert.assertNotNull(p);
        Assert.assertEquals(3, p.getActions().size());
    }

    @Test
    public void issue87() throws Exception {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[3,2],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[1]},\"2\":{\"sleepingVMs\":[],\"runningVMs\":[4]},\"3\":{\"sleepingVMs\":[],\"runningVMs\":[]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{}},\"views\":[]},\"constraints\":[{\"vm\":0,\"continuous\":false,\"id\":\"running\"},{\"vm\":1,\"continuous\":false,\"id\":\"running\"},{\"vm\":2,\"continuous\":false,\"id\":\"running\"},{\"vm\":3,\"continuous\":false,\"id\":\"running\"},{\"vm\":4,\"continuous\":false,\"id\":\"running\"},{\"amount\":1,\"nodes\":[0],\"continuous\":false,\"id\":\"runningCapacity\"},{\"amount\":1,\"nodes\":[1],\"continuous\":false,\"id\":\"runningCapacity\"},{\"amount\":1,\"nodes\":[2],\"continuous\":false,\"id\":\"runningCapacity\"},{\"amount\":2,\"nodes\":[3],\"continuous\":false,\"id\":\"runningCapacity\"}],\"objective\":{\"id\":\"minimizeMTTR\"}}\n";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        s.doOptimize(true);
        ReconfigurationPlan p = s.solve(i);
        System.out.println(s.getStatistics());
        Assert.assertNotNull(p);
        Assert.assertEquals(p.getSize(), 2);
        System.out.println(p);
    }

    public static void main(String[] args) throws Exception {
        testIssue89();
    }
    @Test
    public static void testIssue89() throws Exception {
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

    @Test
    public void testIssue93() throws Exception {
        String buf = "{\"model\":{\"mapping\":{\"readyVMs\":[],\"onlineNodes\":{\"0\":{\"sleepingVMs\":[],\"runningVMs\":[0]},\"1\":{\"sleepingVMs\":[],\"runningVMs\":[1]},\"2\":{\"sleepingVMs\":[],\"runningVMs\":[2]}},\"offlineNodes\":[]},\"attributes\":{\"nodes\":{},\"vms\":{\"0\":{\"memUsed\":204},\"1\":{\"memUsed\":204},\"2\":{\"memUsed\":204}}},\"views\":[{\"defConsumption\":0,\"nodes\":{\"0\":8,\"1\":10,\"2\":10},\"rcId\":\"cpu\",\"id\":\"shareableResource\",\"defCapacity\":0,\"vms\":{\"0\":8,\"1\":1,\"2\":1}},{\"routing\":{\"type\":\"default\"},\"switches\":[{\"id\":0,\"capacity\":-1}],\"links\":[{\"physicalElement\":{\"id\":2,\"type\":\"node\"},\"id\":0,\"capacity\":1000,\"switch\":0},{\"physicalElement\":{\"id\":1,\"type\":\"node\"},\"id\":1,\"capacity\":1000,\"switch\":0},{\"physicalElement\":{\"id\":0,\"type\":\"node\"},\"id\":2,\"capacity\":1000,\"switch\":0}],\"id\":\"net\"}]},\"constraints\":[{\"rc\":\"cpu\",\"amount\":6,\"nodes\":[0],\"continuous\":false,\"id\":\"resourceCapacity\"}],\"objective\":{\"id\":\"minimizeMTTR\"}}\n";
        Instance i = JSON.readInstance(new StringReader(buf));
        ChocoScheduler s = new DefaultChocoScheduler();
        s.doOptimize(true);
        ReconfigurationPlan p = s.solve(i);
        System.out.println(s.getStatistics());
        Assert.assertNotNull(p);
        System.out.println(p);
    }

    @Test
    public void testIssue100() throws Exception {
        Instance i = JSON.readInstance(new File("src/test/resources/issue-100.json"));
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan p = s.solve(i);
        SolvingStatistics stats = s.getStatistics();
        Assert.assertNotNull(p);
        System.out.println(stats);
    }

    @Test
    public void testIssue101() throws Exception {
        Instance i = JSON.readInstance(new File("src/test/resources/issue-101.json"));
        ChocoScheduler s = new DefaultChocoScheduler();
        ReconfigurationPlan p = s.solve(i);
        SolvingStatistics stats = s.getStatistics();
        System.out.println(stats);
        Assert.assertNotNull(p);
        System.out.println(p);
    }
}
