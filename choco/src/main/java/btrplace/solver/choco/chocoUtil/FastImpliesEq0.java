/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * A fast implementation for BVAR <=> VAR[0,x] = 0
 * <br/>
 *
 * @author Fabien Hermenier
 */
public class FastImpliesEq0 extends AbstractBinIntSConstraint {


    public FastImpliesEq0(IntDomainVar b, IntDomainVar var) {
        super(b, var);
        if ((!b.isInstantiated() && !b.hasBooleanDomain())
                || (b.isInstantiated() && !b.isInstantiatedTo(0) && !b.isInstantiatedTo(1))) {
            throw new SolverException(b.getName() + " is not a boolean variable");
        }
        if (var.getInf() < 0) {
            throw new SolverException(var.getName() + " should have a lb >= 0");
        }
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx == 0) {
            return IntVarEvent.INSTINT_MASK;
        } else {
            return IntVarEvent.INCINF_MASK + IntVarEvent.INSTINT_MASK;
        }
    }

    @Override
    public void awake() throws ContradictionException {
        if (v0.isInstantiatedTo(1)) {
            v1.instantiate(0, this, false);
            this.setEntailed();
        }
        if (v1.getInf() > 0) {
            v0.instantiate(0, this, false);
            this.setEntailed();
        }
    }

    @Override
    public void propagate() throws ContradictionException {
        //ChocoLogging.getBranchingLogger().info(v0.pretty() + " propagate()");
        if (v0.isInstantiatedTo(1)) {
            v1.instantiate(0, this, false);
            this.setEntailed();
        }
        if (v1.getInf() > 0) {
            v0.instantiate(0, this, false);
            this.setEntailed();
        }
    }

    @Override
    public void awakeOnInf(int varIdx) throws ContradictionException {
        //    ChocoLogging.getBranchingLogger().info(vars[varIdx].pretty());
        if (varIdx == 1) {//Cause the lb of v1 is supposed to be 0
            v0.instantiate(0, this, false);
            setEntailed();
        } else {
            throw new SolverException("awakeOnInf(" + varIdx + ")");
        }
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        if (idx == 0 && v0.getVal() == 1) {
            v1.instantiate(0, this, false);
        } else if (idx == 1 && v1.getVal() != 0) {
            v0.instantiate(0, this, false);
        }
    }

    @Override
    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        throw new SolverException("foo");
        //ChocoLogging.getBranchingLogger().info(vars[idx].pretty() + " " + deltaDomain.toString());
        /*if (idx == 1 && !v1.canBeInstantiatedTo(constante)) {
            v0.instantiate(0, this, false);
            this.setEntailed();
        } */
    }

    @Override
    public void awakeOnRem(int varIdx, int val) throws ContradictionException {
        throw new SolverException("foo");
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        return (tuple[0] == 1 && tuple[1] == 0) || tuple[0] == 0;
    }

    @Override
    public boolean isConsistent() {
        if (vars[0].isInstantiatedTo(1)) {
            return (vars[1].isInstantiatedTo(0));
        } else if (vars[0].isInstantiatedTo(1)) {
            return (!vars[1].canBeInstantiatedTo(0));
        }
        return true;
    }

    @Override
    public String toString() {
        return vars[0].pretty() + " -> " + vars[1].pretty() + " = 0";
    }

    @Override
    public String pretty() {
        return toString();
    }
}
