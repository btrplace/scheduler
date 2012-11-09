package btrplace.plan;

import btrplace.instance.Instance;
import btrplace.plan.actions.Action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Default implementation for {@link ReconfigurationPlan}.
 *
 * @author Fabien Hermenier
 */
public class DefaultReconfigurationPlan implements ReconfigurationPlan {

    private Instance src;

    private List<Action> actions;

    public DefaultReconfigurationPlan(Instance src) {
        this.src = src;
        this.actions = new ArrayList<Action>();
    }

    @Override
    public Instance getSource() {
        return src;
    }

    @Override
    public void add(Action a) {
        this.actions.add(a);
    }

    @Override
    public int size() {
        return actions.size();
    }

    @Override
    public int getDuration() {
        int m = 0;
        for (Action a : actions) {
            if (a.getEnd() > m) {
                m = a.getEnd();
            }
        }
        return m;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public Iterator<Action> iterator() {
        return actions.iterator();
    }
}
