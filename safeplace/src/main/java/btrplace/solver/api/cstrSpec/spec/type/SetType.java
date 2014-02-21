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
