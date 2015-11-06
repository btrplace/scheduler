package org.btrplace.model.view;

import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.*;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 * Created by vins on 11/01/15.
 */
public class EnergyView implements ModelView, Cloneable {

    /**
     * The view identifier.
     */
    public static final String VIEW_ID = "EnergyView";

    public static final int DEFAULT_NODE_CONSUMPTION = 120; // 120 Watts
    public static final int DEFAULT_VM_CONSUMPTION = 20; // 20 Watts
    public static final int DEFAULT_BOOT_OVERHEAD = 20; // Percentage of the Node consumption

    // Values from HTDC'11 paper
    public static final double MIGRATION_ENERGY_ALPHA = 0.512;
    public static final double MIGRATION_ENERGY_BETA = 20.165;

    private String viewId;
    private int maxPower;
    Map<Node, Integer> nodeIdlePower;
    Map<VM, Integer> vmPower;
    private List<TimeIntervalBudget> tibList;
    private int boot_overhead = DEFAULT_BOOT_OVERHEAD;
    private MigrationEnergy migEnergyModel = new MigrationEnergy(MIGRATION_ENERGY_ALPHA, MIGRATION_ENERGY_BETA);


    public EnergyView(int maxPower) {
        this.viewId = VIEW_ID;
        this.maxPower = maxPower;
        nodeIdlePower = new HashMap<>();
        vmPower = new HashMap<>();
        tibList = new ArrayList<>();
    }

    public List<TimeIntervalBudget> getTibList() {
        return tibList;
    }

    public void setMigEnergyModel(int a, int b) { migEnergyModel.setAlpha(a); migEnergyModel.setBeta(b); }

    public int getMigrationOverhead(int bw, int time) { return migEnergyModel.getConsumption(bw, time); }

    public int getBootOverhead() { return boot_overhead; }

    public void setBootOverhead(int percentage) { boot_overhead = percentage; }

    public void setConsumption(Node n, int power) {
        nodeIdlePower.put(n, power);
    }

    public void setConsumption(VM vm, int power) {
        vmPower.put(vm, power);
    }

    public int getConsumption(Node n) {
        return nodeIdlePower.getOrDefault(n, DEFAULT_NODE_CONSUMPTION);
    }

    public int getConsumption(VM vm) {
        return vmPower.getOrDefault(vm, DEFAULT_VM_CONSUMPTION);
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getMaxPower(int t) {

        int maxPower = getMaxPower();

        tibList.sort((tib, tib2) -> (tib.getBudget() - tib2.getBudget()));

        for (EnergyView.TimeIntervalBudget tib : tibList) {
            if (tib.getStart() <= t && tib.getEnd() > t) {
                if (maxPower > tib.getBudget()) {
                    maxPower = tib.getBudget();
                    break;
                }
            }
        }

        return maxPower;
    }

    public void addBudget(int start, int end, int power) {
        tibList.add(new TimeIntervalBudget(start, end, power));
    }

    public boolean plotConsumption(ReconfigurationPlan p, String outputFile) {

        char SEPARATOR = ';';

        // Get actions
        Set<Action> actionsSet = p.getActions();
        if(actionsSet.isEmpty()) return false;

        // From set to list
        List<Action> actions = new ArrayList<>();
        actions.addAll(actionsSet);

        int duration = p.getDuration();
        if (duration <= 0) return false;

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

            // Sort the actions per start and end times
            actions.sort(new Comparator<Action>() {
                @Override
                public int compare(Action action, Action action2) {
                    int result = action.getStart() - action2.getStart();
                    if (result == 0) {
                        result = action.getEnd() - action2.getEnd();
                    }
                    return result;
                }
            });

            // Write header
            writer.write("TIME" + SEPARATOR + "POWER" + SEPARATOR + "BUDGET");

            int power;

            // Add initial power usage
            Set<Node> nodes = p.getOrigin().getMapping().getOnlineNodes();
            Set<VM> vms = p.getOrigin().getMapping().getRunningVMs();

            for (int t=-1; t<=duration; t++) {

                writer.newLine();
                power = 0;

                for (Action a : actions) {

                    int start = a.getStart(), end = a.getEnd();

                    if (start <= t && end > t) {
                        if (a instanceof MigrateVM) {
                            power += getMigrationOverhead(((MigrateVM) a).getBandwidth(), 1);
                        }
                        if (a instanceof BootNode) {
                            power += (getBootOverhead()*getConsumption(((BootNode) a).getNode())/100);
                        }
                    }
                    if (t == start) {
                        if (a instanceof BootNode) {
                            nodes.add(((BootNode) a).getNode());
                        }
                        if (a instanceof BootVM) {
                            vms.add(((BootVM) a).getVM());
                        }
                    }
                    if (t == end) {
                        if (a instanceof ShutdownNode) {
                            nodes.remove(((ShutdownNode) a).getNode());
                        }
                        if (a instanceof ShutdownVM) {
                            vms.remove(((ShutdownVM) a).getVM());
                        }
                    }
                }

                for (Node n : nodes) { power += getConsumption(n); }

                for (VM vm : vms) { power += getConsumption(vm); }

                writer.write(
                        Integer.toString(t) + SEPARATOR +
                        Integer.toString(power) + SEPARATOR +
                        Integer.toString(getMaxPower(t))
                );
            }

            writer.flush();

        }catch(IOException ex){}
        finally{
            try {
                writer.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    @Override
    public String getIdentifier() {
        return viewId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.viewId.equals(((EnergyView) o).getIdentifier());
    }

    @Override
    public ModelView clone() {
        EnergyView ev = new EnergyView(maxPower);
        return ev;
    }

    @Override
    public boolean substituteVM(VM curId, VM nextId) {
        return false;
    }

    public class TimeIntervalBudget {
        private int start, end, budget;
        public TimeIntervalBudget(int s, int e, int b) { start = s; end = e; budget= b; }
        public int getStart() { return start; }
        public int getEnd() { return end; }
        public int getBudget() { return budget; }
    }

    public class MigrationEnergy {
        private double alpha, beta;
        public MigrationEnergy(double a, double b) { alpha = a; beta = b; }
        public double getAlpha() { return alpha; }
        public double getBeta() { return beta; }
        public void setAlpha(int a) { alpha = a; }
        public void setBeta(int b) { beta = b; }
        //public int getConsumption(int bw, int time) { return (int) Math.round((alpha*((bw/8)*time))+beta); }
        public int getConsumption(int bw, int time) { return (int) Math.round(((bw/16)*time)+beta); }
    }
}
