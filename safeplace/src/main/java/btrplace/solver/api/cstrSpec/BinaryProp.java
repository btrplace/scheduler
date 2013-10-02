package btrplace.solver.api.cstrSpec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A sequence of propositions having a same operator.
 * @author Fabien Hermenier
 */
public abstract class BinaryProp implements Proposition, Iterable<Proposition> {

    protected List<Proposition> props;

    public BinaryProp() {
        props = new ArrayList<>();
    }

    @Override
    public Iterator<Proposition> iterator() {
        return props.iterator();
    }

    public abstract BinaryProp add(Proposition p);

    public Proposition get(int i) {
        return props.get(i);
    }

    public int size() {
        return props.size();
    }
}
