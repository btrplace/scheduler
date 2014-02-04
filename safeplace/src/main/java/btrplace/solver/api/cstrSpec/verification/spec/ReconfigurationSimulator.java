package btrplace.solver.api.cstrSpec.verification.spec;

import btrplace.model.constraint.SatConstraint;
import btrplace.plan.event.*;
import btrplace.solver.api.cstrSpec.spec.type.NodeStateType;
import btrplace.solver.api.cstrSpec.spec.type.VMStateType;

/**
 * @author Fabien Hermenier
 */
public class ReconfigurationSimulator {

    private SpecModel mo;

    public ReconfigurationSimulator(SpecModel mo) {
        this.mo = mo;
    }

    public SpecModel currentModel() {
        return mo;
    }

    public void startsWith(SpecModel mo) {
        this.mo = mo;
    }


    public void start(MigrateVM a) {
        mo.state(a.getVM(), VMStateType.Type.migrating);
    }


    public void end(MigrateVM a) {
        mo.state(a.getVM(), VMStateType.Type.running);
    }


    public void start(BootVM a) {
        mo.state(a.getVM(), VMStateType.Type.booting);
        mo.host(a.getVM(), a.getDestinationNode());
    }


    public void end(BootVM a) {
        mo.state(a.getVM(), VMStateType.Type.running);
    }


    public void start(BootNode a) {
        mo.state(a.getNode(), NodeStateType.Type.booting);
    }


    public void end(BootNode a) {
        mo.state(a.getNode(), NodeStateType.Type.online);
    }


    public void start(ShutdownVM a) {
        mo.state(a.getVM(), VMStateType.Type.halting);
    }


    public void end(ShutdownVM a) {
        mo.state(a.getVM(), VMStateType.Type.ready);
        mo.unhost(a.getVM());
    }


    public void start(ShutdownNode a) {
        mo.state(a.getNode(), NodeStateType.Type.halting);
    }


    public void end(ShutdownNode a) {
        mo.state(a.getNode(), NodeStateType.Type.offline);
    }


    public void start(ResumeVM a) {
        mo.state(a.getVM(), VMStateType.Type.resuming);
        mo.host(a.getVM(), a.getDestinationNode());
    }


    public void end(ResumeVM a) {
        mo.state(a.getVM(), VMStateType.Type.running);
    }


    public void start(SuspendVM a) {
        mo.state(a.getVM(), VMStateType.Type.suspending);
    }


    public void end(SuspendVM a) {
        mo.state(a.getVM(), VMStateType.Type.sleeping);
    }


    public boolean start(KillVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }


    public void end(KillVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }


    public boolean start(ForgeVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }


    public void end(ForgeVM a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }


    public boolean consume(SubstitutedVMEvent e) {
        throw new UnsupportedOperationException("Unsupported action " + e);
    }


    public boolean consume(AllocateEvent e) {
        throw new UnsupportedOperationException("Unsupported action " + e);
    }


    public boolean start(Allocate a) {
        throw new UnsupportedOperationException("Unsupported action " + a);
    }


    public void end(Allocate e) {
        throw new UnsupportedOperationException("Unsupported action " + e);
    }


    public boolean endsWith(SpecModel mo) {
        return true;
    }


    public SatConstraint getConstraint() {
        return null;
        //throw new UnsupportedOperationException("No constraint");
    }
}
