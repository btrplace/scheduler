package btrplace.plan;

import btrplace.model.Model;
import btrplace.plan.event.ActionVisitor;

import java.util.UUID;

/**
 * A fake action for test purposes.
 *
 * @author Fabien Hermenier
 */
public class MockAction extends Action {

    public int count = 0;

    public UUID u;

    public MockAction(UUID u, int st, int ed) {
        super(st, ed);
        this.u = u;
    }

    @Override
    public boolean applyAction(Model i) {
        count++;
        return true;
    }

    @Override
    public String pretty() {
        return "pretty()";
    }

    @Override
    public Object visit(ActionVisitor v) {
        throw new UnsupportedOperationException();
    }
}
