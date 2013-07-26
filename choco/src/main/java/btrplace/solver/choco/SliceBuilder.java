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

    private VM e;

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
        this.e = v;
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
            if (start.isInstantiated() && end.isInstantiated()) {
                int d = end.getVal() - start.getVal();
                duration = rp.makeDuration(d, d, lblPrefix, "_duration");
            } else if (start.isInstantiated()) {
                if (start.isInstantiatedTo(0)) {
                    duration = end;
                } else {
                    duration = new IntDomainVarAddCste(rp.getSolver(), rp.makeVarLabel(lblPrefix, "_duration"), end, -start.getVal());
                }
            } else {
                int inf = end.getInf() - start.getSup();
                if (inf < 0) {
                    inf = 0;
                }
                int sup = end.getSup() - start.getInf();
                duration = rp.makeDuration(sup, inf, lblPrefix, "_duration");
                rp.getSolver().post(rp.getSolver().eq(end, rp.getSolver().plus(start, duration)));
            }
        }

        if (start != rp.getEnd() && start.getSup() > rp.getEnd().getInf()) {
            rp.getSolver().post(rp.getSolver().leq(start, rp.getEnd()));
        }
        if (end != rp.getEnd() && end.getSup() > rp.getEnd().getInf()) {
            rp.getSolver().post(rp.getSolver().leq(end, rp.getEnd()));
        }
        if (duration != rp.getEnd() && duration.getSup() > rp.getEnd().getInf()) {
            rp.getSolver().post(rp.getSolver().leq(duration, rp.getEnd()));
        }
        CPSolver s = rp.getSolver();
        if (start.isInstantiatedTo(0)) {
            s.post(s.eq(duration, end));
        } else {
            s.post(s.eq(end, s.plus(start, duration)));
        }
        return new Slice(e, start, end, duration, hoster);
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
     * @param end the variable to use
     * @return the current builder
     */
    public SliceBuilder setEnd(IntDomainVar end) {
        this.end = end;
        return this;
    }

    /**
     * Set the duration of the slice.
     *
     * @param duration the variable to use
     * @return the current builder
     */
    public SliceBuilder setDuration(IntDomainVar duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Set the hoster.
     *
     * @param hoster the variable to use
     * @return the current builder
     */
    public SliceBuilder setHoster(IntDomainVar hoster) {
        this.hoster = hoster;
        return this;
    }

    /**
     * Set the hoster to a specific value
     *
     * @param v the node index
     * @return the current builder
     */
    public SliceBuilder setHoster(int v) {
        this.hoster = rp.getSolver().createIntegerConstant(rp.makeVarLabel(lblPrefix, "_hoster(", e, ")"), v);
        return this;
    }
}
