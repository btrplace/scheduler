package btrplace.plan.actions;

import btrplace.instance.Configuration;
import btrplace.instance.Instance;

import java.util.UUID;

/**
 * Migrate a running VM from one online node to another one.
 *
 * @author Fabien Hermenier
 */
public class Migrate implements Action {

    private UUID vm;

    private UUID src, dst;

    private int st, ed;

    /**
     * Make a new action.
     *
     * @param vm  the VM to migrate
     * @param src the node the VM is currently running on
     * @param dst the node where to place the VM
     * @param st  the moment the action will start
     * @param ed  the moment the action will stop
     */
    public Migrate(UUID vm, UUID src, UUID dst, int st, int ed) {
        this.vm = vm;
        this.src = src;
        this.dst = dst;
        this.st = st;
        this.ed = ed;
    }

    @Override
    public boolean apply(Instance i) {
        Configuration c = i.getConfiguration();
        if (c.getOnlineNodes().contains(src)
                && c.getOnlineNodes().contains(dst)
                && c.getRunningVMs().contains(vm)) {
            c.setVMRunOn(vm, dst);
            return true;
        }
        return false;
    }

    @Override
    public int getStart() {
        return st;
    }

    @Override
    public int getEnd() {
        return ed;
    }
}
