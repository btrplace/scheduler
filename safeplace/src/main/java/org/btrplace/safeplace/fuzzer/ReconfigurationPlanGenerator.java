package org.btrplace.safeplace.fuzzer;

import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.plan.DefaultReconfigurationPlan;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.plan.event.*;

import java.util.*;

/**
 * Created by fhermeni on 25/07/2015.
 */
public class ReconfigurationPlanGenerator {

    private int min, max;

    public ReconfigurationPlanGenerator() {
        min = 1;
        max = 4;
    }


    public ReconfigurationPlan build(Model mo) {
        Random rnd = new Random();
        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);

        //Compute the action duration in advance
        Map<VM, Integer> vmDurations = new HashMap<>();
        int makespan = 0;
        for (VM v : mo.getMapping().getAllVMs()) {
            //boot,halt,migrate,suspend,resume, kill, + nothing
            if (rnd.nextInt(7) == 0) {
                continue; //no action
            }
            int d = rnd.nextInt(max - min) + min;
            makespan += d;
            vmDurations.put(v, d);
        }

        Map<Node, Integer> nodeDurations = new HashMap<>();
        for (Node n : mo.getMapping().getAllNodes()) {
            //online, offline, stay
            if (rnd.nextInt(3) == 0) {
                continue; //no action
            }
            int d = rnd.nextInt(max - min) + min;
            makespan += d;
            nodeDurations.put(n, d);
        }

        for (VM v : mo.getMapping().getAllVMs()) {
            if (!vmDurations.containsKey(v)) {
                continue;
            }
            int d = vmDurations.get(v);
            int begin = rnd.nextInt(makespan - d + 1);
            int end = begin + d;
            Action a = null;

            switch (rnd.nextInt(6)) {
                case 0: //migrate
                    a = new MigrateVM(v,
                            mo.getMapping().getVMLocation(v),
                            randomNode(rnd, mo.getMapping().getAllNodes()),
                            begin, end);
                    break;
                case 1:
                    //boot
                    a = new BootVM(v,
                            randomNode(rnd, mo.getMapping().getAllNodes()),
                            begin, end);
                    break;
                case 2:
                    //suspend
                    a = new SuspendVM(v,
                            mo.getMapping().getVMLocation(v),
                            randomNode(rnd, mo.getMapping().getAllNodes()),
                            begin, end);
                    break;
                case 3:
                    //resume
                    a = new ResumeVM(v,
                            mo.getMapping().getVMLocation(v),
                            randomNode(rnd, mo.getMapping().getAllNodes()),
                            begin, end);
                    break;
                case 4:
                    //shutdown
                    a = new ShutdownVM(v,
                            mo.getMapping().getVMLocation(v),
                            begin, end);
                    break;
                case 5:
                    //kill
                    a = new KillVM(v,
                            mo.getMapping().getVMLocation(v),
                            begin, end);
                    break;
            }
            p.add(a);
        }

        for (Node n : mo.getMapping().getAllNodes()) {
            if (!nodeDurations.containsKey(n)) {
                continue;
            }
            int d = nodeDurations.get(n);
            int end = rnd.nextInt(makespan + 1);
            int begin = end - d;
            Action a = null;

            switch (rnd.nextInt(2)) {
                case 0: //offline
                    a = new BootNode(n,
                            begin, end);
                    break;
                case 1:
                    //boot
                    a = new ShutdownNode(n,
                            begin, end);
                    break;
            }
            p.add(a);
        }
        return p;
    }

    public ReconfigurationPlanGenerator min(int m) {
        min = m;
        return this;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public ReconfigurationPlanGenerator max(int m) {
        max = m;
        return this;
    }

    private Node randomNode(Random rnd, Collection<Node> ns) {
        int x = rnd.nextInt(ns.size());
        Iterator<Node> ite = ns.iterator();
        Node n = null;
        while (x >= 0) {
            n = ite.next();
            x--;
        }
        return n;
    }
}
