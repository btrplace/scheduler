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

package btrplace.solver.choco.durationEvaluator;

import btrplace.model.Model;
import btrplace.model.view.ShareableResource;


/**
 * Evaluate the duration of an action on an element linearly from a given resource.
 * <p/>
 * The duration {@code d} is expressed as {@code d = coefficient * rc.get(e) + offset}.
 * It is truncated to get an integer value
 *
 * @author Fabien Hermenier
 */
public class LinearToAResourceDuration implements DurationEvaluator {

    private String rc;

    private double a;

    private double b;

    private boolean onVM;

    /**
     * Make a new evaluator.
     * The offset value is set to 0
     *
     * @param rcId the resource identifier
     * @param a    the coefficient
     */
    public LinearToAResourceDuration(String rcId, boolean onVM, double a) {
        this(rcId, onVM, a, 0);
    }

    /**
     * Make a new evaluator.
     *
     * @param rcId the resource to consider
     * @param a    the coefficient
     * @param b    the offset
     */
    public LinearToAResourceDuration(String rcId, boolean onVM, double a, double b) {
        this.rc = rcId;
        this.a = a;
        this.b = b;
        this.onVM = onVM;
    }

    @Override
    public int evaluate(Model mo, int e) {
        ShareableResource r = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + rc);
        if (r == null) {
            return -1;
        }
        int x;
        if (onVM) {
            x = r.getVMConsumption(e);
        } else {
            x = r.getNodeCapacity(e);
        }
        return (int) (a * x + b);
    }

    /**
     * Get the associated resource identifier.
     *
     * @return a resource identifier
     */
    public String getResourceId() {
        return rc;
    }

    /**
     * Set the resource to use;
     *
     * @param rc a resource identifier
     */
    public void setResourceId(String rc) {
        this.rc = rc;
    }

    /**
     * Get the coefficient.
     *
     * @return a number
     */
    public double getCoefficient() {
        return a;
    }

    /**
     * Set the coefficient.
     *
     * @param a the coefficient to use
     */
    public void setCoefficient(double a) {
        this.a = a;
    }

    /**
     * Get the offset.
     *
     * @return a number
     */
    public double getOffset() {
        return b;
    }

    /**
     * Set the offset.
     *
     * @param b the offset to use
     */
    public void setOffset(double b) {
        this.b = b;
    }
}
