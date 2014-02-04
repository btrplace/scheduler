package btrplace.solver.api.cstrSpec.spec.type;

import btrplace.solver.api.cstrSpec.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public class ColType implements Type {

    protected Type type;

    public ColType(Type t) {
        type = t;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ColType)) {
            return false;
        }
        if (type == null) {
            return true;
        }
        ColType colType = (ColType) o;

        return type.equals(colType.type);

    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String label() {
        StringBuilder b = new StringBuilder("col<");
        if (type == null) {
            b.append('?');
        } else {
            b.append(type.label());
        }
        return b.append('>').toString();
    }

    @Override
    public boolean match(String n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Constant newValue(String n) {
        //Add a value inside the set
        //return type.newValue(n);
        throw new UnsupportedOperationException();
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
