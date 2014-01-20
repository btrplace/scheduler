/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco.chocoUtil;


import solver.constraints.IntConstraint;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

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
public class RoundedUpDivision extends IntConstraint<IntVar> {

    private double q;

    private IntVar a, b;

    /**
     * Make a new constraint.
     */
    public RoundedUpDivision(IntVar a, IntVar b, double q) {
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
        if (a.getLB() != div(b.getLB())
                || a.getUB() != div(b.getUB())) {
            fail();
        }
    }

    @Override
    public void awakeOnInf(int i) throws ContradictionException {
        if (i == 1) {
            a.setInf(div(b.getLB()));
        } else {
            b.setInf(multLB(a.getLB()));
        }
        constAwake(false);
    }

    @Override
    public void awakeOnSup(int i) throws ContradictionException {
        if (i == 1) {
            a.setSup(div(b.getUB()));
        } else {
            b.setSup((int) Math.floor(q * a.getUB()));
        }
        constAwake(false);
    }

    @Override
    public void awakeOnInst(int i) throws ContradictionException {
        if (i == 1) {
            a.setVal(div(b.getValue()));
        } else {
            b.setInf(multLB(a.getLB()));
            b.setSup((int) Math.floor(q * a.getUB()));
        }
        constAwake(false);
    }

    @Override
    public boolean isSatisfied(int[] values) {
        return values[0] == (int) Math.ceil((double) values[1] / q);
    }

    @Override
    public String pretty() {
        return new StringBuilder(a.toString()).append(" = ").append(b.toString()).append('/').append(q).toString();
    }

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK | IntVarEvent.INCINF_MASK | IntVarEvent.DECSUP_MASK;
    }
}
