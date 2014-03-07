package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.Model;
import btrplace.plan.ReconfigurationPlan;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanFuzzer {

    private List<ReconfigurationPlanFuzzerListener> listeners;

    private int minDuration = 1, maxDuration = 3;

    private int countDurations = -1, countDelays = -1;

    private int nbNodes, nbVMs;

    public ReconfigurationPlanFuzzer(int nbVMs, int nbNodes) {
        listeners = new ArrayList<>();
        this.nbNodes = nbNodes;
        this.nbVMs = nbVMs;
    }

    public ReconfigurationPlanFuzzer minDuration(int d) {
        minDuration = d;
        return this;
    }

    public ReconfigurationPlanFuzzer maxDuration(int d) {
        maxDuration = d;
        return this;
    }

    public ReconfigurationPlanFuzzer nbDurations(int d) {
        countDurations = d;
        return this;
    }

    public ReconfigurationPlanFuzzer allDurations() {
        countDurations = -1;
        return this;
    }

    public ReconfigurationPlanFuzzer nbDelays(int d) {
        countDelays = d;
        return this;
    }

    public ReconfigurationPlanFuzzer allDelays() {
        countDelays = -1;
        return this;
    }

    public void addListener(ReconfigurationPlanFuzzerListener l) {
        this.listeners.add(l);
    }

    public int nbSourceModel() {
        return new ModelsGenerator(nbNodes, nbVMs).count();
    }

    public int maxNbOfPlansPerModel() {
        return nbNodes * nbNodes * (int) Math.pow(nbVMs, 4);
    }

    public int maxNbOfDurationsPerPlan() {
        int nbActions = nbNodes * nbVMs;
        return (int) Math.pow((maxDuration - minDuration) + 1, nbActions);
    }

    public void go() {
        ModelsGenerator mg = new ModelsGenerator(nbNodes, nbVMs);
        for (Model mo : mg) {
            //System.out.println("New model");
            ReconfigurationPlansGenerator rpgen = new ReconfigurationPlansGenerator(mo);
            for (ReconfigurationPlan p : rpgen) {
                //System.out.println("New basic plan");
                DurationsGenerator dg;
                int nbDurations = countDurations;
                if (nbDurations == -1) {
                    dg = new DurationsGenerator(p, minDuration, maxDuration);
                    nbDurations = dg.count();
                } else {
                    dg = new DurationsGenerator(p, minDuration, maxDuration, true);
                }
                for (int i = 0; i < nbDurations; i++) {
                    //System.out.println("New duration " + (i+1) + "/" + countDurations);
                    ReconfigurationPlan pd = dg.next();

                    DelaysGenerator delayG;
                    int nbDelays = countDelays;
                    if (nbDelays == -1) {
                        delayG = new DelaysGenerator(pd, false);
                        nbDelays = delayG.count();
                    } else {
                        delayG = new DelaysGenerator(pd, true);
                    }
                    for (int j = 0; j < nbDelays; j++) {
                        //System.out.println("New delay " + (j+1) + "/" + countDelays);
                        ReconfigurationPlan gp = delayG.next();
                        for (ReconfigurationPlanFuzzerListener l : listeners) {
                            l.recv(gp);
                        }
                    }
                }
            }
        }
    }
}
