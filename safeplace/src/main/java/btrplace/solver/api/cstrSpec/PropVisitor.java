package btrplace.solver.api.cstrSpec;

/**
 * @author Fabien Hermenier
 */
public interface PropVisitor {

    public void visit(And a);

    public void visit(Or a);

    public void visit(AtomicProp p);
}
