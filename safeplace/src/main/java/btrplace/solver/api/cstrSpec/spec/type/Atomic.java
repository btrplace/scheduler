package btrplace.solver.api.cstrSpec.spec.type;

/**
 * @author Fabien Hermenier
 */
public abstract class Atomic implements Type {

    @Override
    public Type inside() {
        throw new UnsupportedOperationException(this + " is an atomic type");
    }

    @Override
    public Type include() {
        throw new UnsupportedOperationException(this + " is an atomic type");
    }
}
