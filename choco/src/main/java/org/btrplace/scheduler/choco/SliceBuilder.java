/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco;

import org.btrplace.model.VM;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.extensions.TaskMonitor;
import org.chocosolver.solver.variables.IntVar;

/**
 * A tool to help at the instantiation of Slices.
 * <p>
 * By default, the slice starts at {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getStart()} and
 * ends at {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getEnd()} and can be hosted on any node
 * declared in the origin model.
 *
 * @author Fabien Hermenier
 */
public class SliceBuilder {

  private final ReconfigurationProblem rp;

    private IntVar start = null;
    private IntVar end = null;
    private IntVar duration = null;

    private IntVar hoster = null;

  private final VM vm;

  private Object[] lblPrefix;

    /**
     * Make a new Builder.
     *
     * @param p      the problem to customize
     * @param v      the VM associated to the slice
     * @param prefix the label prefix for the variables
     */
    public SliceBuilder(ReconfigurationProblem p, VM v, Object... prefix) {
        this.rp = p;
        this.vm = v;
        lblPrefix = prefix;
    }

    /**
     * Make a new Builder.
     *
     * @param p      the problem to customize
     * @param v      the VM associated to the slice
     */
    public SliceBuilder(ReconfigurationProblem p, VM v) {
        this.rp = p;
        this.vm = v;
        lblPrefix = null;
    }

    /**
     * Build the slice.
     *
     * @return the resulting slice
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred
     */
    public Slice build() throws SchedulerException {
        if (hoster == null) {
            if (rp.labelVariables() && lblPrefix != null) {
                hoster = rp.makeHostVariable(lblPrefix, "_hoster");
            } else {
                hoster = rp.makeHostVariable();
            }
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

        //UB for the time variables
        if (!start.isInstantiatedTo(0)) {
            //enforces start <= end, duration <= end, start + duration == end
            TaskMonitor.build(start, duration, end);
        }
        //start == 0 --> start <= end. duration = end enforced by TaskScheduler
        return new Slice(vm, start, end, duration, hoster);
    }

    /**
     * Make the duration variable depending on the others.
     */
    private IntVar makeDuration() throws SchedulerException {
        if (start.isInstantiated() && end.isInstantiated()) {
            int d = end.getValue() - start.getValue();
            if (rp.labelVariables() && lblPrefix != null) {
                return rp.makeDuration(d, d, lblPrefix, "_duration");
            } else {
                return rp.getModel().intVar(d);
            }
        } else if (start.isInstantiated()) {
            if (start.isInstantiatedTo(0)) {
                return end;
            }
            return rp.getModel().intOffsetView(end, -start.getValue());
        }
        int inf = end.getLB() - start.getUB();
        if (inf < 0) {
            inf = 0;
        }
        int sup = end.getUB() - start.getLB();
        if (rp.labelVariables() && lblPrefix != null) {
            return rp.makeDuration(sup, inf, lblPrefix, "_duration");
        }
        return rp.makeDuration(sup, inf);
    }

    /**
     * Set the moment the slice consume.
     *
     * @param st the variable to use
     * @return the current builder
     */
    public SliceBuilder setStart(IntVar st) {
        start = st;
        return this;
    }

    /**
     * Set the moment the slice ends.
     *
     * @param e the variable to use
     * @return the current builder
     */
    public SliceBuilder setEnd(IntVar e) {
        this.end = e;
        return this;
    }

    /**
     * Set the duration of the slice.
     *
     * @param d the variable to use
     * @return the current builder
     */
    public SliceBuilder setDuration(IntVar d) {
        this.duration = d;
        return this;
    }

    /**
     * Set the hoster.
     *
     * @param h the variable to use
     * @return the current builder
     */
    public SliceBuilder setHoster(IntVar h) {
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
        if (rp.labelVariables() && lblPrefix != null) {
            this.hoster = rp.fixed(v, lblPrefix, "_hoster(", vm, ")");
        } else {
            this.hoster = rp.getModel().intVar(v);
        }
        return this;
    }
}
