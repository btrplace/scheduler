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

package btrplace.solver.choco;

import btrplace.solver.SolverException;
import choco.cp.solver.variables.integer.IntDomainVarAddCste;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.UUID;

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

    private IntDomainVar isExclusive = null;

    private UUID e;

    /**
     * Make a new Builder.
     *
     * @param rp the problem to customize
     * @param e  the element associated to the slice
     */
    public SliceBuilder(ReconfigurationProblem rp, UUID e) {
        this.rp = rp;
        this.e = e;
    }

    /**
     * Build the slice.
     *
     * @return the resulting slice
     * @throws SolverException if an error occurred
     */
    public Slice build() throws SolverException {
        if (hoster == null) {
            hoster = rp.makeHostVariable(rp.makeVarLabel("hoster_slice(" + e + ")"));
        }
        if (start == null) {
            start = rp.getStart();
        }
        if (end == null) {
            end = rp.getEnd();
        }
        if (duration == null) {
            if (start.isInstantiated() && end.isInstantiated()) {
                duration = rp.getSolver().createIntegerConstant(rp.makeVarLabel("duration_slice(" + e + ")"), end.getVal() - start.getVal());
            } else if (start.isInstantiated()) {
                duration = new IntDomainVarAddCste(rp.getSolver(), rp.makeVarLabel("duration_slice(" + e + ")"), end, -start.getVal());
            } else {
                int inf = end.getInf() - start.getSup();
                if (inf < 0) {
                    inf = 0;
                }
                int sup = end.getSup() - start.getInf();
                duration = rp.getSolver().createBoundIntVar(rp.makeVarLabel("duration_slice(" + e + ")"), inf, sup);
                rp.getSolver().post(rp.getSolver().eq(end, rp.getSolver().plus(start, duration)));
            }
        }

        if (isExclusive == null) {
            isExclusive = rp.getSolver().createBooleanVar(rp.makeVarLabel("exclusive_slice(" + e + ")"));
        }
        if (start.getSup() > rp.getEnd().getInf()) {
            rp.getSolver().post(rp.getSolver().leq(start, rp.getEnd()));
        }
        if (end.getSup() > rp.getEnd().getInf()) {
            rp.getSolver().post(rp.getSolver().leq(end, rp.getEnd()));
        }
        if (duration.getSup() > rp.getEnd().getInf()) {
            rp.getSolver().post(rp.getSolver().leq(duration, rp.getEnd()));
        }
        return new Slice(e, start, end, duration, hoster, isExclusive);
    }

    /**
     * Set the moment the slice start.
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
        this.hoster = rp.getSolver().createIntegerConstant(rp.makeVarLabel("hoster_slice(" + e + ")"), v);
        return this;
    }

    /**
     * Set the exclusivity status of the slice.
     *
     * @param exclusive the variable indicating the exlusive status of the slice
     * @return the current builder
     */
    public SliceBuilder setExclusive(IntDomainVar exclusive) {
        isExclusive = exclusive;
        return this;
    }

    /**
     * Indicates if the slice is exclusive or not.
     *
     * @param b {@code true} to indicate the slice is exclusive
     * @return the current builder
     */
    public SliceBuilder setExclusive(boolean b) {
        isExclusive = rp.getSolver().createIntegerConstant(rp.makeVarLabel("exclusive_slice(" + e + ")"), b ? 1 : 0);
        return this;
    }
}
