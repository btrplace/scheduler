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
import util.iterators.DisposableIntIterator;

public final class MyElement extends IntConstraint<IntVar> {

    private int[] lval;

    private int cste;

    /**
     * To indicate the ordering of the index value
     */
    public static enum Sort {
        /**
         * Values are unordered
         */
        none,
        /**
         * Values are in the increasing order
         */
        ascending,
        /**
         * Values are in the decreasing order
         */
        descending,
        /**
         * Let the constraint detect the ordering, if any
         */
        detect
    }

    /**
     * The current ordering
     */
    private Sort sortType;

    public MyElement(IntVar index, int[] values, IntVar var, int offset, Sort s) {
        super(index, var);
        this.lval = values;
        this.cste = offset;
        this.sortType = s;
    }

    public MyElement(IntVar index, int[] values, IntVar var) {
        this(index, values, var, 0, Sort.none);
    }

    @Override
    public String toString() {
        return "Element";
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx == 0) {
            return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
        }
        return IntVarEvent.REMVAL_MASK;
    }

    /**
     * <i>Propagation:</i>
     * Propagating the constraint until local consistency is reached.
     *
     * @throws choco.kernel.solver.ContradictionException contradiction exception
     */
    @Override
    public void propagate() throws ContradictionException {
        this.updateIndexFromValue();
    }

    @Override
    public String pretty() {
        return (this.v1.toString() + " = nth(" + this.v0.toString() + ", " + StringUtils.pretty(this.lval) + ")");
    }

    protected void updateValueFromIndex() throws ContradictionException {
        if (sortType == Sort.descending) {
            this.v1.updateInf(this.lval[v0.getUB() - cste], this, false);
            this.v1.updateSup(this.lval[v0.getLB() - cste], this, false);
        } else if (sortType == Sort.ascending) {
            this.v1.updateInf(this.lval[v0.getLB() - cste], this, false);
            this.v1.updateSup(this.lval[v0.getUB() - cste], this, false);

        } else {
            computeMinMaxValue();
        }
        // todo : <hcambaza> : why it does not perform AC on the value variable ?
    }

    private void computeMinMaxValue() throws ContradictionException {
        int minVal = Integer.MAX_VALUE;
        int maxVal = Integer.MIN_VALUE;
        DisposableIntIterator iter = this.v0.getDomain().getIterator();
        boolean isDsc = true;
        boolean isAsc = true;
        int prev = this.lval[v0.getLB() - cste];
        try {
            while (iter.hasNext()) {
                int index = iter.next();
                int val = this.lval[index - cste];
                if (minVal > val) {
                    minVal = val;
                }
                if (maxVal < val) {
                    maxVal = val;
                }
                if (sortType == Sort.detect) {
                    if (val > prev) {
                        isDsc = false;
                    }
                    if (val < prev) {
                        isAsc = false;
                    }
                }
                prev = val;
            }
            if (sortType == Sort.detect) {
                if (isDsc) {
                    sortType = Sort.descending;
                } else if (isAsc) {
                    sortType = Sort.ascending;
                } else {
                    sortType = Sort.none;
                }
            }
            this.v1.updateInf(minVal, this, false);
            this.v1.updateSup(maxVal, this, false);
        } finally {
            iter.dispose();
        }
    }

    protected void updateIndexFromValue() throws ContradictionException {
        int minFeasibleIndex = Math.max(cste, this.v0.getLB());
        int maxFeasibleIndex = Math.min(this.v0.getUB(), lval.length - 1 + cste);

        if (minFeasibleIndex > maxFeasibleIndex) {
            this.fail();
        }

        boolean forceAwake = !this.v1.hasEnumeratedDomain();

        while ((this.v0.canBeInstantiatedTo(minFeasibleIndex))
                && !(this.v1.canBeInstantiatedTo(lval[minFeasibleIndex - this.cste]))) {
            minFeasibleIndex++;
        }

        this.v0.updateInf(minFeasibleIndex, this, forceAwake);

        while ((this.v0.canBeInstantiatedTo(maxFeasibleIndex))
                && !(this.v1.canBeInstantiatedTo(lval[maxFeasibleIndex - this.cste]))) {
            maxFeasibleIndex--;
        }
        this.v0.updateSup(maxFeasibleIndex, this, forceAwake);

        if (this.v0.hasEnumeratedDomain()) {
            for (int i = minFeasibleIndex + 1; i <= maxFeasibleIndex - 1; i++) {
                if (this.v0.canBeInstantiatedTo(i) && !(this.v1.canBeInstantiatedTo(this.lval[i - this.cste]))) {
                    this.v0.removeVal(i, this, forceAwake);
                }
            }
        }
    }

    @Override
    public void awake() throws ContradictionException {
        this.updateIndexFromValue();
        this.updateValueFromIndex();
    }

    @Override
    public void awakeOnInst(int i) throws ContradictionException {
        if (i == 0) {
            this.v1.instantiate(this.lval[this.v0.getValue() - this.cste], this, false);
        }
    }

    @Override
    public void awakeOnRem(int i, int x) throws ContradictionException {
        if (i == 0) {
            this.updateValueFromIndex();
        } else {
            this.updateIndexFromValue();
        }
    }

    @Override
    public Boolean isEntailed() {
        if (this.v1.instantiated()) {
            boolean allVal = true;
            boolean oneVal = false;
            for (int val = v0.getLB(); val <= v0.getUB(); val = v0.getNextDomainValue(val)) {
                boolean b = (val - this.cste) >= 0
                        && (val - this.cste) < this.lval.length
                        && this.lval[val - this.cste] == this.v1.getValue();
                allVal &= b;
                oneVal |= b;
            }
            if (allVal) {
                return Boolean.TRUE;
            }
            if (oneVal) {
                return null;
            }
        } else {
            boolean b = false;
            for (int val = v0.getLB(); val <= v0.getUB() && !b; val = v0.getNextDomainValue(val)) {
                if ((val - this.cste) >= 0 &&
                        (val - this.cste) < this.lval.length) {
                    b = this.v1.canBeInstantiatedTo(this.lval[val - this.cste]);
                }
            }
            if (b) {
                return null;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        if (tuple[0] - this.cste >= lval.length ||
                tuple[0] - this.cste < 0) {
            return false;
        }
        return this.lval[tuple[0] - this.cste] == tuple[1];
    }
}
