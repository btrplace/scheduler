package btrplace.solver.api.cstrSpec.fuzzer;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.plan.DefaultReconfigurationPlan;
import btrplace.plan.ReconfigurationPlan;
import btrplace.plan.event.*;
import btrplace.solver.api.cstrSpec.spec.SymbolsTable;
import btrplace.solver.api.cstrSpec.verification.spec.VerifDomain;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class ReconfigurationPlanFuzzer2 implements Iterable<ReconfigurationPlan>, Iterator<ReconfigurationPlan> {

    private Random rnd = new Random();

    private TransitionTable nodeTrans;

    private TransitionTable vmTrans;

    private int nbNodes = 3, nbVMs = 3;

    private int minDuration = 3;

    private int maxDuration = 3;

    private List<VerifDomain> doms = new ArrayList<>();

    private SymbolsTable syms;

    public ReconfigurationPlanFuzzer2() {
        try {
            nodeTrans = new TransitionTable(new InputStreamReader(getClass().getResourceAsStream("/node_transitions")));
            vmTrans = new TransitionTable(new InputStreamReader(getClass().getResourceAsStream("/vm_transitions")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        syms = SymbolsTable.newBundle();
    }

    public ReconfigurationPlanFuzzer2 dom(VerifDomain v) {
        for (Iterator<VerifDomain> ite = doms.iterator(); ite.hasNext(); ) {
            VerifDomain vv = ite.next();
            if (vv.type().equals(v.type())) {
                ite.remove();
                break;
            }
        }
        doms.add(v);
        return this;
    }

    public SymbolsTable symbolsTable() {
        return syms;
    }

    public List<VerifDomain> doms() {
        return Collections.unmodifiableList(doms);
    }

    public ReconfigurationPlanFuzzer2 nbVMs(int n) {
        nbVMs = n;
        return this;
    }

    public ReconfigurationPlanFuzzer2 nbNodes(int n) {
        nbNodes = n;
        return this;
    }

    public ReconfigurationPlanFuzzer2 vmTransitions(TransitionTable t) {
        vmTrans = t;
        return this;
    }

    public ReconfigurationPlanFuzzer2 nodeTransitions(TransitionTable t) {
        nodeTrans = t;
        return this;
    }

    public ReconfigurationPlanFuzzer2 actionDuration(int min, int max) {
        minDuration = min;
        maxDuration = max;
        return this;
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

    private int duration() {
        if (minDuration == maxDuration) {
            return minDuration;
        }
        return rnd.nextInt(maxDuration - minDuration + 1) + minDuration;
    }

    @Override
    public ReconfigurationPlan next() {
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
                    p.add(new BootNode(n, 0, duration()));
                    break;
            }
        }
        for (Node n : mo.getMapping().getOnlineNodes()) {
            double d = rnd.nextDouble();
            String dstState = nodeTrans.getDstState("on", d);
            switch (dstState) {
                case "off":
                    p.add(new ShutdownNode(n, 0, duration()));
                    break;
            }

            for (VM v : mo.getMapping().getRunningVMs(n)) {
                String dst = vmTrans.getDstState("running", d);
                switch (dst) {
                    case "running":
                        Node to = randomNode(allNodes);
                        if (!n.equals(to) && to != null) {
                            p.add(new MigrateVM(v, n, to, 0, duration()));
                        }
                        break;
                    case "sleeping":
                        p.add(new SuspendVM(v, n, n, 0, duration()));
                        break;
                    case "ready":
                        p.add(new ShutdownVM(v, n, 0, duration()));
                        break;
                    case "killed":
                        p.add(new KillVM(v, n, 0, duration()));
                        break;
                }
            }
            for (VM v : mo.getMapping().getSleepingVMs(n)) {
                String dst = vmTrans.getDstState("sleeping", d);
                switch (dst) {
                    case "running":
                        p.add(new ResumeVM(v, n, n, 0, duration()));
                        break;
                    case "sleeping":
                        break;
                    case "killed":
                        p.add(new KillVM(v, n, 0, duration()));
                        break;
                }
            }
            for (VM v : mo.getMapping().getReadyVMs()) {
                String dst = vmTrans.getDstState("ready", d);
                switch (dst) {
                    case "run":
                        Node to = randomNode(allNodes);
                        if (to != null) {
                            p.add(new BootVM(v, to, 0, duration()));
                        }
                        break;
                    case "ready":
                        break;
                    case "killed":
                        p.add(new KillVM(v, null, 0, duration()));
                }
            }
        }

        return delay(p);
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

    private Model newModel() {
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

    private ReconfigurationPlan delay(ReconfigurationPlan src) {
        int maxDuration = 0;
        for (Action a : src) {
            maxDuration += a.getEnd();
        }

        ReconfigurationPlan rp = new DefaultReconfigurationPlan(src.getOrigin());
        for (Action a : src) {
            int d = maxDuration - a.getEnd() + a.getStart();
            int st;
            if (d == 0) {
                st = 0;
            } else {
                st = rnd.nextInt(maxDuration - a.getEnd() + a.getStart());
            }
            int ed = st + (a.getEnd() - a.getStart());
            Action na = Actions.newAction(a, st, ed);
            rp.add(na);
        }
        return rp;
    }

    @Override
    public Iterator<ReconfigurationPlan> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
