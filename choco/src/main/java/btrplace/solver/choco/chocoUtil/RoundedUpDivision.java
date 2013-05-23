package btrplace.solver.choco.chocoUtil;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A constraint to enforce {@code a == b / q} where {@code q} is a real and {@code a} and {@code b} are
 * both integers.
 * The division is rounded up to the smallest integer.
 * <p/>
 * In practice, the constraint maintains:
 * <ul>
 * <li>{@code a = Math.ceil(b / q)}</li>
 * <li>{@code b = ((a - 1 )* q) % 1 == 0 ? [(a - 1)*q + 1; Math.floor(a * q)] : [Math.ceil((a -1)*q); Math.floor(a * q)]}</li>
 * </ul>
 *
 * @author Fabien Hermenier
 */
public class RoundedUpDivision extends AbstractBinIntSConstraint {

    private double q;

    private IntDomainVar a, b;

    /**
     * Make a new constraint.
     */
    public RoundedUpDivision(IntDomainVar a, IntDomainVar b, double q) {
        super(a, b);
        this.a = a;
        this.b = b;
        this.q = q;
    }

    private int div(int b) {
        return (int) Math.ceil((double) b / q);
    }

    private int multLB(int a) {
        if ((a - 1 * q) % 1 == 0) {
            return (int) ((a - 1) * q + 1);
        }
        return (int) Math.ceil(q * (a - 1));
    }

    @Override
    public void awake() throws ContradictionException {
        awakeOnInf(0);
        awakeOnSup(0);
        awakeOnInf(1);
        awakeOnSup(1);
    }

    @Override
    public void propagate() throws ContradictionException {
        if (a.getInf() != div(b.getInf())
                || a.getSup() != div(b.getSup())) {
            fail();
        }
    }

    @Override
    public void awakeOnInf(int i) throws ContradictionException {
        if (i == 1) {
            a.setInf(div(b.getInf()));
        } else {
            b.setInf(multLB(a.getInf()));
        }
        constAwake(false);
    }

    @Override
    public void awakeOnSup(int i) throws ContradictionException {
        if (i == 1) {
            a.setSup(div(b.getSup()));
        } else {
            b.setSup((int) Math.floor(q * a.getSup()));
        }
        constAwake(false);
    }

    @Override
    public void awakeOnInst(int i) throws ContradictionException {
        if (i == 1) {
            a.setVal(div(b.getVal()));
        } else {
            b.setInf(multLB(a.getInf()));
            b.setSup((int) Math.floor(q * a.getSup()));
        }
        constAwake(false);
    }

    @Override
    public boolean isSatisfied(int[] vals) {
        return vals[0] == (int) Math.ceil((double) vals[1] / q);
    }

    @Override
    public String pretty() {
        return new StringBuilder(a.pretty()).append(" = ").append(b.pretty()).append('/').append(q).toString();
    }

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK | IntVarEvent.INCINF_MASK | IntVarEvent.DECSUP_MASK;
    }
}
