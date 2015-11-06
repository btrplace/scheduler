package org.btrplace.scheduler.choco.view;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.model.view.EnergyView;
import org.btrplace.model.view.ModelView;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.transition.BootableNode;
import org.btrplace.scheduler.choco.transition.NodeTransition;
import org.btrplace.scheduler.choco.transition.RelocatableVM;
import org.btrplace.scheduler.choco.transition.VMTransition;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

import java.util.*;

/**
 * Created by vins on 11/01/15.
 */
public class CEnergyView implements ChocoView {

    /**
     * The view identifier.
     */
    public static final String VIEW_ID = "EnergyView";

    private EnergyView ev;
    private ReconfigurationProblem rp;
    private Solver solver;
    private Model source;
    private List<Task> tasks;
    private List<IntVar> heights;
    private CPowerView cPowerView;
    private int maxDiscretePower = 0;
    private Map<String, List> energy;
    private boolean energyComputed = false;


    public CEnergyView(ReconfigurationProblem p, EnergyView v) throws SchedulerException {
        ev = v;
        rp = p;
        solver = p.getSolver();
        source = p.getSourceModel();
        tasks = new ArrayList<>();
        heights = new ArrayList<>();
        energy = new HashMap<>();

        // Retrieve or create the PowerView
        cPowerView = (CPowerView) rp.getView(CPowerView.VIEW_ID);
        if (cPowerView == null) {
            cPowerView = new CPowerView(rp);
            if (!rp.addView(cPowerView)) {
                throw new SchedulerException(rp.getSourceModel(), "Unable to attach view '" + CPowerView.VIEW_ID + "'");
            }
        }
    }

    public void cap(int start, int end, int power) { ev.addBudget(start, end, power); }

    public void cap(int power) {
        maxDiscretePower = power;
    }

    private void addTask(int start, int end, int power) {

        IntVar s, d, e;
        s = VF.fixed(start, solver);
        e = VF.fixed(end, solver);
        d = rp.makeUnboundedDuration();
        //solver.post(IntConstraintFactory.arithm(d, "<=", rp.getEnd()));

        tasks.add(VariableFactory.task(s, d, e));
        heights.add(VF.fixed(power, solver));
    }

    public Map<String, List> computeEnergy() {

        // Compute energy only once
        if (energyComputed) {
            return energy;
        }

        List<Task> tasks = new ArrayList<>();
        List<IntVar> heights = new ArrayList<>();

        // Add nodes/vms consumptions for continuous model
        for (Node n : rp.getNodes()) {  // Add nodes consumption
            int nodePower = ev.getConsumption(n);
            NodeTransition nt = rp.getNodeAction(n);

            // Add boot peak consumption
            if (nt instanceof BootableNode) {
                tasks.add(VariableFactory.task(nt.getStart(), nt.getDuration(), nt.getEnd()));
                heights.add(VF.fixed(nodePower * ev.getBootOverhead() / 100, solver));
            }

            IntVar duration = rp.makeUnboundedDuration(rp.makeVarLabel("Dur(", n, ")"));
            //solver.post(IntConstraintFactory.arithm(duration, "<=", rp.getEnd()));
            tasks.add(VariableFactory.task(cPowerView.getPowerStart(rp.getNode(n)), duration,
                    cPowerView.getPowerEnd(rp.getNode(n))));
            heights.add(VF.fixed("energy(" + n + ")", ev.getConsumption(n), solver));
        }
        for (VM v : rp.getVMs()) {  // Add VMs consumption
            int vmPower = ev.getConsumption(v);
            VMState currentState = rp.getSourceModel().getMapping().getState(v);
            VMState futureState = rp.getFutureState(v);
            VMTransition vmt = rp.getVMAction(v);

            IntVar duration = rp.makeUnboundedDuration(rp.makeVarLabel("Dur(", v, ")"));
            //solver.post(IntConstraintFactory.arithm(duration, "<=", rp.getEnd()));

            // Relocate or Migrate
            if (currentState.equals(VMState.RUNNING) && futureState.equals(VMState.RUNNING)) {

                //  In the case of a live migration, add the transfer overhead
                if (vmt instanceof RelocatableVM) {// || vmt instanceof RelocatableVM) {
                    tasks.add(VariableFactory.task(vmt.getStart(), vmt.getDuration(), vmt.getEnd()));

                    // Original formula:  Energy = (Alpha * ((BW/8) * 1)) + Beta

                    // Correspond to: (Energy - Beta)
                    IntVar migEnergyWithoutBeta = VF.bounded("migrationEnergyWithoutBeta(" + v + ")",
                            0,
                            (int) Math.round(((((RelocatableVM) vmt).getBandwidth().getUB() / 8) * EnergyView.MIGRATION_ENERGY_ALPHA) + 1),
                            solver
                    );
                    // BW * (Alpha/8) = (Energy - Beta)
                    //solver.post(ICF.times(((MigrateVMTransition) vmt).getBandwidth(), (int) Math.round(EnergyView.MIGRATION_ENERGY_ALPHA)/8, migEnergyWithoutBeta));
                    // Simplification (Alpha ~= 0.5):  BW * (Alpha/8) => BW/16
                    solver.post(ICF.eucl_div(((RelocatableVM) vmt).getBandwidth(), VF.fixed(16, solver), migEnergyWithoutBeta));
                    // Correspond to: Energy
                    IntVar migEnergy = VF.bounded("energy(" + vmt + ")",
                            (int) Math.round(EnergyView.MIGRATION_ENERGY_BETA - 1),
                            (int) Math.round(migEnergyWithoutBeta.getUB() + EnergyView.MIGRATION_ENERGY_BETA),
                            solver
                    );
                    // Energy = (Energy - Beta) + Beta
                    solver.post(ICF.arithm(migEnergy, "=", migEnergyWithoutBeta, "+", (int) Math.round(EnergyView.MIGRATION_ENERGY_BETA)));
                    heights.add(migEnergy);
                }

                tasks.add(VariableFactory.task(rp.getStart(), duration, rp.getEnd()));
                heights.add(VF.fixed("energy(" + v + ")", vmPower, solver));

                // Boot / Resume
            } else if ((currentState.equals(VMState.READY) && futureState.equals(VMState.RUNNING)) ||
                    (currentState.equals(VMState.SLEEPING) && futureState.equals(VMState.RUNNING))) {
                tasks.add(VariableFactory.task(vmt.getStart(), duration, rp.getEnd()));
                heights.add(VF.fixed("energy(" + v + ")", vmPower, solver));

                // Halt / Kill / Sleep
            } else if ((currentState.equals(VMState.RUNNING) && futureState.equals(VMState.READY)) ||
                    (currentState.equals(VMState.RUNNING) && futureState.equals(VMState.KILLED)) ||
                    (currentState.equals(VMState.RUNNING) && futureState.equals(VMState.SLEEPING))) {
                tasks.add(VariableFactory.task(rp.getStart(), duration, rp.getEnd()));
                heights.add(VF.fixed("energy(" + v + ")", vmPower, solver));

                // Resume
            } else if (currentState.equals(VMState.SLEEPING) && futureState.equals(VMState.RUNNING)) {
                tasks.add(VariableFactory.task(vmt.getStart(), duration, rp.getEnd()));
                heights.add(VF.fixed("energy(" + v + ")", vmPower, solver));
            }
        }

        energy.put("tasks", tasks);
        energy.put("heights", heights);

        energyComputed = true;

        return energy;
    }

    @Override
    public String getIdentifier() {
        return ev.getIdentifier();
    }

    @Override
    public boolean beforeSolve(ReconfigurationProblem rp) throws SchedulerException {

        Model mo = rp.getSourceModel();

        List<EnergyView.TimeIntervalBudget> tibList = ev.getTibList();

        // Add time-interval power budgets
        if (!tibList.isEmpty()) {

            // Get min and max t of all budgets
            tibList.sort((tib, tib2) -> (tib.getStart() - tib2.getStart()));
            int start = tibList.get(0).getStart();
            tibList.sort((tib, tib2) -> (tib2.getEnd() - tib.getEnd()));
            int end = tibList.get(0).getEnd();

            int previousBudget = ev.getMaxPower(start), currentBudget;
            int startBudget = start;

            for (int i=start+1; i<=end; i++) {
                currentBudget = ev.getMaxPower(i);

                if (currentBudget != previousBudget) {
                    // Create the 'ghost' task
                    addTask(startBudget, i, ev.getMaxPower() - previousBudget);

                    // Init new budget
                    startBudget = i;

                    // Update 'future' previous budget
                    previousBudget = currentBudget;
                }
            }
        }

        // Add nodes/vms consumptions for continuous model
        if (!energyComputed) { computeEnergy(); }
        if (!energy.isEmpty()) {
            tasks.addAll(energy.get("tasks"));
            heights.addAll(energy.get("heights"));
        }

        // Post the resulting cumulative constraint
        if (!tasks.isEmpty()) {
            solver.post(ICF.cumulative(
                    tasks.toArray(new Task[tasks.size()]),
                    heights.toArray(new IntVar[heights.size()]),
                    VF.fixed(ev.getMaxPower(), solver),
                    true
            ));
        }
        tasks.clear();
        heights.clear();

        // Add constraints for discrete model
        if (maxDiscretePower > 0) {
            List<IntVar> powList = new ArrayList<>();
            for (Node n : rp.getNodes()) {  // Nodes consumption
                int idlePower = ev.getConsumption(n);
                IntVar cons = VF.bounded(rp.makeVarLabel("powerConsumption(" + n + ")"), 0, idlePower, rp.getSolver());
                LCF.ifThenElse(rp.getNodeAction(n).getState(),
                        ICF.arithm(cons, "=", idlePower),
                        ICF.arithm(cons, "=", 0));
                powList.add(cons);
            }
            int vmsPow = 0;
            for (VM v : rp.getFutureRunningVMs()) {  // VMs consumption
                vmsPow += ev.getConsumption(v);
            }
            // Post the constraint
            solver.post(ICF.sum(powList.toArray(new IntVar[powList.size()]), "<=", VF.fixed(maxDiscretePower - vmsPow, solver)));
        }

        return true;
    }

    @Override
    public boolean insertActions(ReconfigurationProblem rp, Solution s, ReconfigurationPlan p) {
        return true;
    }

    @Override
    public boolean cloneVM(VM vm, VM clone) {
        return true;
    }

    /**
     * Builder associated to the constraint.
     */
    public static class Builder implements ChocoModelViewBuilder {
        @Override
        public Class<? extends ModelView> getKey() {
            return EnergyView.class;
        }

        @Override
        public SolverViewBuilder build(final ModelView v) throws SchedulerException {
            //return new DelegatedBuilder(v.getIdentifier(), Arrays.asList(CPowerView.VIEW_ID)) {
            return new DelegatedBuilder(v.getIdentifier(), Collections.emptyList()) {
                @Override
                public ChocoView build(ReconfigurationProblem r) throws SchedulerException {
                    return new CEnergyView(r, (EnergyView) v);
                }
            };
        }
    }
}
