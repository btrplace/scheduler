package org.btrplace.scheduler.choco.view;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.ModelView;
import org.btrplace.model.view.network.Link;
import org.btrplace.model.view.network.Network;
import org.btrplace.model.view.network.Switch;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Arithmetic;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.nary.cumulative.Cumulative;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by vkherbac on 30/12/14.
 */
public class CNetwork implements ChocoView {

    /**
     * The view identifier.
     */
    public static final String VIEW_ID = "NetworkView";

    private Network net;
    private ReconfigurationProblem rp;
    private Solver solver;
    private Model source;
    List<Task> tasksList;
    List<IntVar> heightsList;

    public CNetwork(ReconfigurationProblem p, Network n) throws SchedulerException {
        net = n;
        rp = p;
        solver = p.getSolver();
        source = p.getSourceModel();
        tasksList = new ArrayList<>();
        heightsList = new ArrayList<>();
    }

    @Override
    public String getIdentifier() { return net.getIdentifier(); }

    @Override
    public boolean beforeSolve(ReconfigurationProblem rp) throws SchedulerException {
        
        Model mo = rp.getSourceModel();
        Solver s = rp.getSolver();
        
        // Pre-compute duration and bandwidth for each VM migration
        for (VMTransition migration : rp.getVMActions()) {

            if (migration instanceof RelocatableVM) {
                
                VM vm = migration.getVM();
                IntVar bandwidth = ((RelocatableVM) migration).getBandwidth();
                IntVar duration = migration.getDuration();
                
                Node src = rp.getSourceModel().getMapping().getVMLocation(vm);
                Node dst;
                if (!migration.getDSlice().getHoster().isInstantiated()) {
                    //throw new SchedulerException(null, "Destination node for VM '" + vm + "' should be known !");
                    return true;
                }
                else {
                    dst = rp.getNode(migration.getDSlice().getHoster().getValue());
                }

                // Check if all attributes are defined
                if (mo.getAttributes().isSet(vm, "memUsed") &&
                        mo.getAttributes().isSet(vm, "dirtyRate") &&
                        mo.getAttributes().isSet(vm, "maxDirtySize") &&
                        mo.getAttributes().isSet(vm, "maxDirtyDuration")) {

                    double dirtyRate;
                    int memUsed, maxDirtyDuration, maxDirtySize;

                    // Get attribute vars
                    memUsed = mo.getAttributes().getInteger(vm, "memUsed");
                    dirtyRate = mo.getAttributes().getDouble(vm, "dirtyRate");
                    maxDirtySize = mo.getAttributes().getInteger(vm, "maxDirtySize");
                    maxDirtyDuration = mo.getAttributes().getInteger(vm, "maxDirtyDuration");

                    // Enumerated BW
                    int maxBW = net.getRouting().getMaxBW(src, dst);
                    int step = maxBW;
                    List<Integer> bwEnum = new ArrayList<>();
                    for (int i = step; i <= maxBW; i += step) {
                        if (i > Math.round(maxDirtySize / maxDirtyDuration)) {
                            bwEnum.add(i);
                        }
                    }

                    // Enumerated duration
                    double durationMin, durationColdPages, durationHotPages, durationTotal;
                    List<Integer> durEnum = new ArrayList<>();
                    for (Integer bw : bwEnum) {

                        // Cheat a bit, real is less than theoretical !
                        double bandwidth_octet = bw / 9;

                        // Estimate duration
                        durationMin = memUsed / bandwidth_octet;
                        if (durationMin > maxDirtyDuration) {

                            durationColdPages = ((maxDirtySize + ((durationMin - maxDirtyDuration) * dirtyRate)) /
                                    (bandwidth_octet - dirtyRate));
                            durationHotPages = ((maxDirtySize / bandwidth_octet) * ((maxDirtySize / maxDirtyDuration) /
                                    (bandwidth_octet - (maxDirtySize / maxDirtyDuration))));
                            durationTotal = durationMin + durationColdPages + durationHotPages;
                        } else {
                            durationTotal = durationMin + (((maxDirtySize / maxDirtyDuration) * durationMin) /
                                    (bandwidth_octet - (maxDirtySize / maxDirtyDuration)));
                        }
                        durEnum.add((int) Math.round(durationTotal));
                    }

                    /* Create the enumerated vars
                    bandwidth = VF.enumerated("bandwidth_enum", bwEnum.stream().mapToInt(i -> i).toArray(), s);
                    duration = VF.enumerated("duration_enum", durEnum.stream().mapToInt(i -> i).toArray(), s);*/

                    /* Associate vars using Tuples
                    Tuples tpl = new Tuples(true);
                    for (int i = 0; i < bwEnum.size(); i++) {
                        tpl.add(bwEnum.get(i), durEnum.get(i));
                    }
                    s.post(ICF.table(bandwidth, duration, tpl, ""));*/

                    /* Set the vars in the VM transition
                    ((MigrateVMTransition) migration).setBandwidth(bandwidth);
                    ((MigrateVMTransition) migration).setDuration(duration);*/

                    // Assign values to unbounded vars
                    s.post(new Arithmetic(duration, Operator.EQ, durEnum.get(0)));
                    s.post(new Arithmetic(bandwidth, Operator.EQ, bwEnum.get(0)));
                    
                } else {
                    throw new SchedulerException(null, "Unable to retrieve attributes for the vm '" + vm + "'");
                }
            }
        }
        
        // Links limitation
        for (Link l : net.getLinks()) {

            for (VM vm : rp.getVMs()) {
                VMTransition a = rp.getVMAction(vm);

                if (a != null && a instanceof RelocatableVM &&
                        (a.getCSlice().getHoster().getValue() != a.getDSlice().getHoster().getValue())) {

                    Node src = source.getMapping().getVMLocation(vm);
                    Node dst = rp.getNode(a.getDSlice().getHoster().getValue());
                    
                    List<Link> path = net.getRouting().getPath(src, dst);

                    // If the link is on migration path
                    if (path.contains(l)) {
                        tasksList.add(new Task(a.getStart(), a.getDuration(), a.getEnd()));
                        heightsList.add(((RelocatableVM) a).getBandwidth());
                    }
                }
            }
            if (!tasksList.isEmpty()) {
                solver.post(new Cumulative(
                        tasksList.toArray(new Task[tasksList.size()]),
                        heightsList.toArray(new IntVar[heightsList.size()]),
                        VF.fixed(l.getCapacity(), solver),
                        true
                        ,Cumulative.Filter.TIME
                        //,Cumulative.Filter.SWEEP
                        //,Cumulative.Filter.SWEEP_HEI_SORT
                        ,Cumulative.Filter.NRJ
                        ,Cumulative.Filter.HEIGHTS
                ));
            }
            tasksList.clear();
            heightsList.clear();
        }

        // Switches capacity limitation
        for(Switch sw : net.getSwitches()) {

            // Only if the capacity is limited
            if (sw.getCapacity() > 0) {

                for (VM vm : rp.getVMs()) {
                    VMTransition a = rp.getVMAction(vm);

                    if (a != null && a instanceof RelocatableVM &&
                            (a.getCSlice().getHoster().getValue() != a.getDSlice().getHoster().getValue())) {

                        Node src = source.getMapping().getVMLocation(vm);
                        Node dst = rp.getNode(a.getDSlice().getHoster().getValue());

                        if (!Collections.disjoint(net.getConnectedLinks(sw), net.getRouting().getPath(src, dst))) {
                            tasksList.add(new Task(a.getStart(), a.getDuration(), a.getEnd()));
                            heightsList.add(((RelocatableVM) a).getBandwidth());
                        }
                    }
                }

                solver.post(ICF.cumulative(
                        tasksList.toArray(new Task[tasksList.size()]),
                        heightsList.toArray(new IntVar[heightsList.size()]),
                        VF.fixed(sw.getCapacity(), solver),
                        true
                ));

                tasksList.clear();
                heightsList.clear();
            }
        }

        return true;
    }

    @Override
    public boolean insertActions(ReconfigurationProblem rp, Solution s, ReconfigurationPlan p) { return true; }

    @Override
    public boolean cloneVM(VM vm, VM clone) { return true; }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoModelViewBuilder {
        @Override
        public Class<? extends ModelView> getKey() {
            return Network.class;
        }

        @Override
        public SolverViewBuilder build(final ModelView v) throws SchedulerException {
            return new DelegatedBuilder(v.getIdentifier(), Collections.emptyList()) {
                @Override
                public ChocoView build(ReconfigurationProblem r) throws SchedulerException {
                    return new CNetwork(r, (Network) v);
                }
            };
        }
    }
}
