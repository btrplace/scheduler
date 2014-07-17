package btrplace.solver.api.cstrSpec.backend;

import btrplace.solver.api.cstrSpec.reducer.Reducer;
import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ReducedDefiantStore extends NoDuplicatedStore {

    private List<Reducer> reducers;

    public ReducedDefiantStore() {
        reducers = new ArrayList<>();
    }

    public ReducedDefiantStore reduceWith(Reducer r) {
        reducers.add(r);
        return this;
    }

    @Override
    public void addDefiant(TestCase c) {
        TestCase x = c;
        try {
            for (Reducer r : reducers) {
                x = r.reduce(x);
            }
            super.addDefiant(x);
        } catch (Exception e) {
            super.addDefiant(c);
        }
    }
}
