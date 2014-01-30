package btrplace.solver.api.cstrSpec.spec.type;

/**
 * @author Fabien Hermenier
 */
public class SetType extends ColType {


    public SetType(Type t) {
        super(t);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SetType setType = (SetType) o;
        if (type == null) {
            return true;
        }
        return type.equals(setType.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    /*public Set domain(Model mo) {
        //All possible subsets of t. Ouch
        Object[] values = type.domain(mo).toArray(new Object[type.domain(mo).size()]);
        int nbElements = (int) Math.pow(2, values.length);
        //System.err.println(nbElements);
        Set<Object> res = new HashSet<>();
        for (int i = 0; i < nbElements; i++) {
            Set sub = new HashSet<>();
            long x = i;
            //decompose x bit per bit
            for (Object value : values) {
                if (x % 2 == 0) {
                    sub.add(value);
                }
                x = x >>> 1;
            }
            if (!sub.isEmpty()) {
                res.add(sub);
            }
        }
        return res;
    }                */

    @Override
    public String label() {
        StringBuilder b = new StringBuilder("set<");
        if (type == null) {
            b.append('?');
        } else {
            b.append(type.label());
        }
        return b.append('>').toString();
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public Type inside() {
        return type;
    }

    public Type enclosingType() {
        return type;
    }

    @Override
    public boolean comparable(Type t) {
        return t.equals(NoneType.getInstance()) || equals(t);
    }
}
