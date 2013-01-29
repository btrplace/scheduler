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
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.BitSet;

/**
 * Enforces two sets of variables values to be disjoint
 * created sofdem - 08/09/11
 * <p/>
 * TODO: Seems buggy but hard to reproduce
 *
 * @author Sophie Demassey
 */
public class Disjoint extends AbstractLargeIntSConstraint {


    /**
     * number of variables in the first set (group 0)
     */
    private final int nbX;

    /**
     * the variable domains must be included in [0, nbValues-1]
     */
    private final int nbValues;

    /**
     * candidates[g][v] = number of variables in group 'g' which can be assigned to the value 'v',
     * with g = 0 || 1 and 0 <= v < nbValues
     */
    private IStateInt[][] candidates;
    /**
     * required[g].get(v) iff at least one variable in the group 'g' is assigned to the value 'v',
     * with g = 0 || 1 and 0 <= v < nbValues
     */
    private IStateBitSet[] required;


    /**
     * @param environment solver environment
     * @param x           first set of variables (group 0)
     * @param y           second set of variables (group 1)
     * @param nbValues    max variable value + 1
     */
    public Disjoint(IEnvironment environment, IntDomainVar[] x, IntDomainVar[] y, int nbValues) {
        super(ArrayUtils.append(x, y));
        this.nbX = x.length;
        this.nbValues = nbValues;
        candidates = new IStateInt[2][nbValues];
        required = new IStateBitSet[2];
        required[0] = environment.makeBitSet(nbValues);
        required[1] = environment.makeBitSet(nbValues);
        for (int v = 0; v < nbValues; v++) {
            candidates[0][v] = environment.makeInt(0);
            candidates[1][v] = environment.makeInt(0);
        }
    }

    @Override
    public int getFilteredEventMask(int idx) {
        return IntVarEvent.REMVAL_MASK + IntVarEvent.INSTINT_MASK;
    }

    /**
     * Initialise required and candidate for a given variable
     * that belong to a given group.
     *
     * @param var   the variable
     * @param group the group of the variable
     */
    private void initVar(IntDomainVar var, int group) {
        if (var.isInstantiated()) {
            required[group].set(var.getVal());
        } else {
            DisposableIntIterator it = var.getDomain().getIterator();
            try {
                while (it.hasNext()) {
                    int val = it.next();
                    candidates[group][val].add(1);
                }
            } finally {
                it.dispose();
            }
        }
    }

    @Override
    public void awake() throws ContradictionException {
        int i = 0;
        for (; i < nbX; i++) {
            initVar(vars[i], 0);
        }
        for (; i < vars.length; i++) {
            initVar(vars[i], 1);
        }
        propagate();
    }

    /**
     * update the internal data and filter when a variable is newly instantiated
     * 1) fail if a variable in the other group is already instantiated to this value
     * 2) remove the value of the domains of all the variables of the other group
     *
     * @param val   the new assigned value
     * @param group the group of the new instantiated variable
     * @param other the other group (other = 1-group)
     * @throws choco.kernel.solver.ContradictionException
     *          when some variables in both groups are instantiated to the same value
     */
    public void setRequired(int val, int group, int other) throws ContradictionException {

        if (required[other].get(val)) {
            //The value is used in the other group. It's a contradiction
            fail();
        }
        if (candidates[other][val].get() > 0) {
            //The value was possible for the other group, so we remove it from its variable

            int n = 0; //The number of variables that were updated
            int i = (other == 0) ? 0 : nbX;
            int end = (other == 0) ? nbX : vars.length;
            for (; i < end; i++) {
                if (vars[i].removeVal(val, this, false)) {
                    n++;
                    /*if (vars[i].isInstantiated()) {
                        setRequired(vars[i].getVal(), other, group);
                    } */
                }
            }
            assert n == candidates[other][val].get() : n + " variables in group '" + other + "' were updated but candidate=" + candidates[other][val].get();
            candidates[other][val].set(0);
        }
        required[group].set(val);
    }

    @Override
    public void awakeOnInst(int idx) throws ContradictionException {
        int group = (idx < nbX) ? 0 : 1;
        //ChocoLogging.getBranchingLogger().finest("awakeOnInst grp= " + group + " val=" + vars[idx].getVal());
        setRequired(vars[idx].getVal(), group, 1 - group);
        constAwake(false);
    }

    @Override
    public void awakeOnRemovals(int idx, DisposableIntIterator deltaDomain) throws ContradictionException {
        //ChocoLogging.getBranchingLogger().finest("awakeOnRemovals(" + idx + ")");
        int group = (idx < nbX) ? 0 : 1;
        while (deltaDomain.hasNext()) {
            int n = deltaDomain.next();
            //ChocoLogging.getBranchingLogger().finest("Decrease candidates for value " + n +  " in group " + group);
            candidates[group][n].add(-1);
        }
        constAwake(false);
    }

    @Override
    public void propagate() throws ContradictionException {
        for (int v = 0; v < nbValues; v++) {
            //Check if the value 'v' is required by a group
            if (required[0].get(v)) {
                //Required by group 0
                setRequired(v, 0, 1);
            }
            if (required[1].get(v)) {
                setRequired(v, 1, 0);
            }
        }
        //prettyCandidates(0);
        //prettyCandidates(1);
    }

    @Override
    public boolean isSatisfied(int[] tuple) {
        BitSet valuesOne = new BitSet(nbValues);
        int i = 0;
        for (; i < nbX; i++) {
            valuesOne.set(tuple[i]);
        }
        for (; i < tuple.length; i++) {
            if (valuesOne.get(tuple[i])) {
                return false;
            }
        }
        return true;
    }

    private void prettyCandidates(int g) {
        StringBuilder b = new StringBuilder();
        int x = 0;
        for (IStateInt v : candidates[g]) {
            b.append(" value(").append(x++).append("):").append(v.get());
        }
        ChocoLogging.getBranchingLogger().finest("Candidates for group " + g + ": " + b.toString());
    }
}
