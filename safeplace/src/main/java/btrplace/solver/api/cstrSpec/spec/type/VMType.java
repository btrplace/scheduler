package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.solver.api.cstrSpec.spec.term.Constant;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VMType extends Atomic {

    private static VMType instance = new VMType();

    private VMType() {

    }

    public static VMType getInstance() {
        return instance;
    }

    @Override
    public Set<VM> domain(Model mo) {
        return mo.getMapping().getAllVMs();
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        return false;
    }

    @Override
    public String label() {
        return "vm";
    }


    @Override
    public Constant newValue(String n) {
        throw new RuntimeException();
    }

}
