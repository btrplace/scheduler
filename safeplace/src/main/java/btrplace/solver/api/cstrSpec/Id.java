package btrplace.solver.api.cstrSpec;

import java.util.Collections;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Id implements Term {

    private String id;

    public Id(String id) {
        this.id = id;
    }

    public String toString(){
        return id;
    }

    public String getName() {
        return id;
    }

    @Override
    public Set domain() {
        return Collections.singleton(id);
    }
}
