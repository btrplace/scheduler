package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public class TimeType extends Atomic {

    private static TimeType instance = new TimeType();

    @Override
    public boolean match(String n) {
        if (n.charAt(0) == 't') {
            try {
                Integer.parseInt(n.substring(1));
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    @Override
    public Constant newValue(String n) {
        if (match(n)) {
            try {
                return new Constant(Integer.parseInt(n.substring(1)), this);
            } catch (Exception e) {
            }
        }
        return null;
    }

    @Override
    public String label() {
        return "time";
    }

    @Override
    public String toString() {
        return label();
    }

    public static TimeType getInstance() {
        return instance;
    }

    @Override
    public boolean comparable(Type t) {
        return t.equals(NoneType.getInstance()) || equals(t);
    }

}
