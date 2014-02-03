package btrplace.solver.api.cstrSpec.verification.specChecker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.VM;
import btrplace.model.constraint.SatConstraint;
import btrplace.model.constraint.checker.SatConstraintChecker;
import btrplace.plan.event.*;
import btrplace.solver.api.cstrSpec.spec.type.NodeStateType;
import btrplace.solver.api.cstrSpec.spec.type.VMStateType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class PlanChecker implements SatConstraintChecker {

    private Map<VM, VMStateType.Type> vmState;

    private Map<Node, NodeStateType.Type> nodeState;

    private Map<VM, Node> location;


    @Override
    public boolean startsWith(Model mo) {
        vmState = new HashMap<>();
        location = new HashMap<>();
        Mapping map = mo.getMapping();
        for (VM v : map.getReadyVMs()) {
            vmState.put(v, VMStateType.Type.ready);
        }

        nodeState = new HashMap<>();
        for (Node n : map.getOnlineNodes()) {
            nodeState.put(n, NodeStateType.Type.online);
            for (VM v : map.getRunningVMs(n)) {
                vmState.put(v, VMStateType.Type.running);
                location.put(v, n);
            }
            for (VM v : map.getSleepingVMs(n)) {
                vmState.put(v, VMStateType.Type.sleeping);
                location.put(v, n);
            }
        }
        for (Node n : map.getOfflineNodes()) {
            nodeState.put(n, NodeStateType.Type.offline);
        }
        return true;
    }

    @Override
    public boolean start(MigrateVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.running
                && nodeState.get(a.getSourceNode()) == NodeStateType.Type.online
                && nodeState.get(a.getDestinationNode()) == NodeStateType.Type.offline
                ) {
            vmState.put(a.getVM(), VMStateType.Type.migrating);
            return true;
        }
        return false;
    }

    @Override
    public void end(MigrateVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.migrating
                && nodeState.get(a.getSourceNode()) == NodeStateType.Type.online
                && nodeState.get(a.getDestinationNode()) == NodeStateType.Type.offline
                ) {
            vmState.put(a.getVM(), VMStateType.Type.running);
            location.put(a.getVM(), a.getDestinationNode());
        } else {
            throw new RuntimeException("Cannot commit " + a);
        }

    }

    @Override
    public boolean start(BootVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.ready
                && nodeState.get(a.getDestinationNode()) == NodeStateType.Type.online
                ) {
            vmState.put(a.getVM(), VMStateType.Type.booting);
            location.put(a.getVM(), a.getDestinationNode());
            return true;
        }
        return false;
    }

    @Override
    public void end(BootVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.booting
                && nodeState.get(a.getDestinationNode()) == NodeStateType.Type.online
                ) {
            vmState.put(a.getVM(), VMStateType.Type.running);
        } else {
            throw new RuntimeException("Cannot commit " + a);
        }
    }

    @Override
    public boolean start(BootNode a) {
        if (nodeState.get(a.getNode()) == NodeStateType.Type.offline) {
            nodeState.put(a.getNode(), NodeStateType.Type.booting);
            return true;
        }
        return false;
    }

    @Override
    public void end(BootNode a) {
        if (nodeState.get(a.getNode()) == NodeStateType.Type.booting) {
            nodeState.put(a.getNode(), NodeStateType.Type.online);
        } else {
            throw new RuntimeException("Cannot commit " + a);
        }
    }

    @Override
    public boolean start(ShutdownVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.running
                && nodeState.get(a.getNode()) == NodeStateType.Type.online
                ) {
            vmState.put(a.getVM(), VMStateType.Type.halting);
            return true;
        }
        return false;
    }

    @Override
    public void end(ShutdownVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.halting
                && nodeState.get(a.getNode()) == NodeStateType.Type.online
                ) {
            vmState.put(a.getVM(), VMStateType.Type.ready);
            location.remove(a.getVM());
        } else {
            throw new RuntimeException("Cannot commit " + a);
        }
    }

    @Override
    public boolean start(ShutdownNode a) {
        if (nodeState.get(a.getNode()) == NodeStateType.Type.online) {
            nodeState.put(a.getNode(), NodeStateType.Type.halting);
            //Check for a VM on it
            for (Map.Entry<VM, Node> e : location.entrySet()) {
                if (e.getValue().equals(a.getNode())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void end(ShutdownNode a) {
        if (nodeState.get(a.getNode()) == NodeStateType.Type.halting) {
            nodeState.put(a.getNode(), NodeStateType.Type.offline);
        } else {
            throw new RuntimeException("Cannot commit " + a);
        }
    }

    @Override
    public boolean start(ResumeVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.sleeping
                && nodeState.get(a.getDestinationNode()) == NodeStateType.Type.online
                ) {
            vmState.put(a.getVM(), VMStateType.Type.resuming);
            location.put(a.getVM(), a.getDestinationNode());
            return true;
        }
        return false;
    }

    @Override
    public void end(ResumeVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.resuming
                && nodeState.get(a.getDestinationNode()) == NodeStateType.Type.online
                ) {
            vmState.put(a.getVM(), VMStateType.Type.running);
            location.put(a.getVM(), a.getDestinationNode());

        } else {
            throw new RuntimeException("Cannot commit " + a);
        }
    }

    @Override
    public boolean start(SuspendVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.running
                && nodeState.get(a.getDestinationNode()) == NodeStateType.Type.online
                ) {
            vmState.put(a.getVM(), VMStateType.Type.suspending);
            return true;
        }
        return false;
    }

    @Override
    public void end(SuspendVM a) {
        if (vmState.get(a.getVM()) == VMStateType.Type.suspending
                && nodeState.get(a.getDestinationNode()) == NodeStateType.Type.online
                ) {
            vmState.put(a.getVM(), VMStateType.Type.sleeping);
        } else {
            throw new RuntimeException("Cannot commit " + a);
        }
    }

    @Override
    public boolean start(KillVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }

    @Override
    public void end(KillVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }

    @Override
    public boolean start(ForgeVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }

    @Override
    public void end(ForgeVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }

    @Override
    public boolean consume(SubstitutedVMEvent e) {
        throw new UnsupportedOperationException("Unsupported action " + e);
    }

    @Override
    public boolean consume(AllocateEvent e) {
        throw new UnsupportedOperationException("Unsupported action " + e);
    }

    @Override
    public boolean start(Allocate a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }

    @Override
    public void end(Allocate e) {
        throw new UnsupportedOperationException("Unsupported action " + e);
    }

    @Override
    public boolean endsWith(Model mo) {
        return true;
    }

    @Override
    public SatConstraint getConstraint() {
        return null;
        //throw new UnsupportedOperationException("No constraint");
    }
}
