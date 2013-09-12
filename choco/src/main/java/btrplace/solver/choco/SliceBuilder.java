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

package btrplace.solver.choco;

import btrplace.model.VM;
import btrplace.solver.SolverException;
import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;
import choco.kernel.solver.variables.integer.IntDomainVar;


/**
 * A tool to help at the instantiation of Slices.
 * <p/>
 * By default, the slice starts at {@link btrplace.solver.choco.ReconfigurationProblem#getStart()} and
 * ends at {@link btrplace.solver.choco.ReconfigurationProblem#getEnd()} and can be hosted on any node
 * declared in the origin model.
 *
 * @author Fabien Hermenier
 */
public class SliceBuilder {

    private ReconfigurationProblem rp;

    private IntDomainVar start = null, end = null, duration = null;

    private IntDomainVar hoster = null;

    private VM vm;

    private String lblPrefix;

    /**
     * Make a new Builder.
     *
     * @param p      the problem to customize
     * @param v      the VM associated to the slice
     * @param prefix the label prefix for the variables
     */
    public SliceBuilder(ReconfigurationProblem p, VM v, String prefix) {
        this.rp = p;
        this.vm = v;
        lblPrefix = prefix;
    }

    /**
     * Build the slice.
     *
     * @return the resulting slice
     * @throws SolverException if an error occurred
     */
    public Slice build() throws SolverException {
        if (hoster == null) {
            hoster = rp.makeHostVariable(lblPrefix, "_hoster");
        }
        if (start == null) {
            start = rp.getStart();
        }
        if (end == null) {
            end = rp.getEnd();
        }
        if (duration == null) {
            duration = makeDuration();
        }

        CPSolver s = rp.getSolver();

        //UB for the time variables
        ticksSooner(s, start, end);
        ticksSooner(s, end, end);
        ticksSooner(s, duration, end);

        if (!start.isInstantiatedTo(0)) {
            //TODO redundancy with makeDuration() ?
            s.post(s.eq(end, s.plus(start, duration)));
        }
        return new Slice(vm, start, end, duration, hoster);
    }

    /**
     * Ensure the time variable t1 ticks before or at moment t2
     *
     * @param s  the solver
     * @param t1
     * @param t2
     */
    private void ticksSooner(CPSolver s, IntDomainVar t1, IntDomainVar t2) {
        if (!t1.equals(t2) && t1.getSup() > t2.getInf()) {
            s.post(s.leq(t1, t2));
        }
    }

    /**
     * Make the duration variable depending on the others.
     */
    private IntDomainVar makeDuration() throws SolverException {
        if (start.isInstantiated() && end.isInstantiated()) {
            int d = end.getVal() - start.getVal();
            return rp.makeDuration(d, d, lblPrefix, "_duration");
        } else if (start.isInstantiated()) {
            if (start.isInstantiatedTo(0)) {
                return end;
            } else {
                return new IntDomainVarAddCste(rp.getSolver(), rp.makeVarLabel(lblPrefix, "_duration"), end, -start.getVal());
            }
        } else {
            int inf = end.getInf() - start.getSup();
            if (inf < 0) {
                inf = 0;
            }
            int sup = end.getSup() - start.getInf();
            IntDomainVar d = rp.makeDuration(sup, inf, lblPrefix, "_duration");
            rp.getSolver().post(rp.getSolver().eq(end, rp.getSolver().plus(start, d)));
            return d;
        }
    }

    /**
     * Set the moment the slice consume.
     *
     * @param st the variable to use
     * @return the current builder
     */
    public SliceBuilder setStart(IntDomainVar st) {
        start = st;
        return this;
    }

    /**
     * Set the moment the slice ends.
     *
     * @param e the variable to use
     * @return the current builder
     */
    public SliceBuilder setEnd(IntDomainVar e) {
        this.end = e;
        return this;
    }

    /**
     * Set the duration of the slice.
     *
     * @param d the variable to use
     * @return the current builder
     */
    public SliceBuilder setDuration(IntDomainVar d) {
        this.duration = d;
        return this;
    }

    /**
     * Set the hoster.
     *
     * @param h the variable to use
     * @return the current builder
     */
    public SliceBuilder setHoster(IntDomainVar h) {
        this.hoster = h;
        return this;
    }

    /**
     * Set the hoster to a specific value
     *
     * @param v the node index
     * @return the current builder
     */
    public SliceBuilder setHoster(int v) {
        this.hoster = rp.getSolver().createIntegerConstant(rp.makeVarLabel(lblPrefix, "_hoster(", vm, ")"), v);
        return this;
    }
}
