package btrplace.solver.api.cstrSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabien Hermenier
 */
public class StateExtractor {

    private Map<Proposition, Proposition> cache;

    public StateExtractor() {
        cache = new HashMap<>();
    }

    public Or extract(Or props) {

        int nbStates = (int) Math.pow(2, props.size());
        Or states = new Or();
        for (int i = 0; i < nbStates; i++) {
            And st = new And();
            long x = i;
            int idx = 0;
            while (idx < props.size()) {
                st.add(get(x%2 != 0, props.get(idx)));
                ++idx;
                x = x >>> 1;
            }

            And expanded = expand(st);
            //System.err.println("expanded: " + expanded);
/*            for (Proposition p : expanded.expand()) {
                System.err.println("\t" + p);
                states.add(p);
            }*/
        }
        return states;
    }

    private And expand(And an) {
        And expanded = new And();
        for (Proposition p : an) {
            expanded.add(p);
        }
        return expanded;
    }

    private Proposition get(boolean not, Proposition k) {
        if (!cache.containsKey(k)) {
            cache.put(k, k.not());
        }
        return not ? cache.get(k) : k;
    }
}
