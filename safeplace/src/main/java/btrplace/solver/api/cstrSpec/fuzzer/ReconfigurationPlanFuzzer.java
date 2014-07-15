package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanFuzzer {

    private Random rnd = new Random();

    private TransitionTable nodeTrans;

    private TransitionTable vmTrans;

    private int nbNodes, nbVMs;

    private int duration = 3;

    public ReconfigurationPlanFuzzer(TransitionTable nodeTrans, TransitionTable vmTrans, int nbNodes, int nbVMs) {
        this.nodeTrans = nodeTrans;
        this.vmTrans = vmTrans;
        this.nbNodes = nbNodes;
        this.nbVMs = nbVMs;
    }

    private Node randomNode(Collection<Node> ns) {
        int x = rnd.nextInt(ns.size());
        Iterator<Node> ite = ns.iterator();
        Node n = null;
        while (x >= 0) {
            n = ite.next();
            x--;
        }
        return n;
    }

    public ReconfigurationPlan newPlan() {
        //initial state
        Model mo = newModel();

        Set<Node> allNodes = mo.getMapping().getAllNodes();

        ReconfigurationPlan p = new DefaultReconfigurationPlan(mo);
        //destination state
        for (Node n : mo.getMapping().getOfflineNodes()) {
            double d = rnd.nextDouble();
            String dstState = nodeTrans.getDstState("off", d);
            switch (dstState) {
                case "on":
                    p.add(new BootNode(n, 0, duration));
                    break;
            }
        }
        for (Node n : mo.getMapping().getOnlineNodes()) {
            double d = rnd.nextDouble();
            String dstState = nodeTrans.getDstState("on", d);
            switch (dstState) {
                case "off":
                    p.add(new ShutdownNode(n, 0, duration));
                    break;
            }

            for (VM v : mo.getMapping().getRunningVMs(n)) {
                String dst = vmTrans.getDstState("running", d);
                switch (dst) {
                    case "running":
                        Node to = randomNode(allNodes);
                        if (!n.equals(to) && to != null) {
                            p.add(new MigrateVM(v, n, to, 0, duration));
                        }
                        break;
                    case "sleeping":
                        p.add(new SuspendVM(v, n, n, 0, duration));
                        break;
                    case "ready":
                        p.add(new ShutdownVM(v, n, 0, duration));
                        break;
                    case "killed":
                        p.add(new KillVM(v, n, 0, duration));
                        break;
                }
            }
            for (VM v : mo.getMapping().getSleepingVMs(n)) {
                String dst = vmTrans.getDstState("sleeping", d);
                switch (dst) {
                    case "running":
                        p.add(new ResumeVM(v, n, n, 0, duration));
                        break;
                    case "sleeping":
                        break;
                    case "killed":
                        p.add(new KillVM(v, n, 0, duration));
                        break;
                }
            }
            for (VM v : mo.getMapping().getReadyVMs()) {
                String dst = vmTrans.getDstState("ready", d);
                switch (dst) {
                    case "run":
                        Node to = randomNode(allNodes);
                        if (to != null) {
                            p.add(new BootVM(v, to, 0, duration));
                        }
                        break;
                    case "ready":
                        break;
                    case "killed":
                        p.add(new KillVM(v, null, 0, duration));
                }
            }
        }

        return p;
    }

    private boolean runOrReady(VM v, Model mo) {
        if (mo.getMapping().getOnlineNodes().isEmpty()) {
            return mo.getMapping().addReadyVM(v);
        } else {
            Node n = randomNode(mo.getMapping().getOnlineNodes());
            return mo.getMapping().addRunningVM(v, n);
        }
    }

    private boolean sleepOrReady(VM v, Model mo) {
        if (mo.getMapping().getOnlineNodes().isEmpty()) {
            return mo.getMapping().addReadyVM(v);
        } else {
            Node n = randomNode(mo.getMapping().getOnlineNodes());
            return mo.getMapping().addSleepingVM(v, n);
        }
    }

    public Model newModel() {
        Model mo = new DefaultModel();
        for (int i = 0; i < nbNodes; i++) {
            Node n = mo.newNode();

            double d = rnd.nextDouble();
            String init = nodeTrans.getInitState(d);
            switch (init) {
                case "on":
                    mo.getMapping().addOnlineNode(n);
                    break;
                case "off":
                    mo.getMapping().addOfflineNode(n);
                    break;
                default:
                    throw new IllegalStateException("Initial node state: '" + init + "'");
            }
        }

        for (int i = 0; i < nbVMs; i++) {
            VM v = mo.newVM();
            double d = rnd.nextDouble();
            String init = vmTrans.getInitState(d);
            switch (init) {
                case "ready":
                    mo.getMapping().addReadyVM(v);
                    break;
                case "running":
                    if (!runOrReady(v, mo)) {
                        throw new IllegalStateException();
                    }
                    break;
                case "sleeping":
                    if (!sleepOrReady(v, mo)) {
                        throw new IllegalStateException();
                    }
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return mo;
    }
}
