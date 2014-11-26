/*
 * Copyright (c) 2014 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.duration;

import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.ShareableResource;


/**
 * Evaluate the duration of an action on an element linearly from a given resource.
 * <p>
 * The duration {@code d} is expressed as {@code d = coefficient * rc.get(e) + newBound}.
 * It is truncated to get an integer value
 *
 * @author Fabien Hermenier
 */
public class LinearToAResourceActionDuration<E extends Element> implements ActionDurationEvaluator<E> {

    private String rc;

    private double coefficient;

    private double offset;

    /**
     * Make a new evaluator.
     * The newBound value is set to 0
     *
     * @param rcId the resource identifier
     * @param a    the coefficient
     */
    public LinearToAResourceActionDuration(String rcId, double a) {
        this(rcId, a, 0);
    }

    /**
     * Make a new evaluator.
     *
     * @param rcId the resource to consider
     * @param a    the coefficient
     * @param b    the newBound
     */
    public LinearToAResourceActionDuration(String rcId, double a, double b) {
        this.rc = rcId;
        this.coefficient = a;
        this.offset = b;
    }

    @Override
    public int evaluate(Model mo, E e) {
        ShareableResource r = (ShareableResource) mo.getView(ShareableResource.VIEW_ID_BASE + rc);
        if (r == null) {
            return -1;
        }
        int x;
        if (e instanceof VM) {
            x = r.getConsumption((VM) e);
        } else if (e instanceof Node) {
            x = r.getCapacity((Node) e);
        } else {
            return -1;
        }
        return (int) (coefficient * x + offset);
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
     * @param r a resource identifier
     */
    public void setResourceId(String r) {
        this.rc = r;
    }

    /**
     * Get the coefficient.
     *
     * @return a number
     */
    public double getCoefficient() {
        return coefficient;
    }

    /**
     * Set the coefficient.
     *
     * @param a the coefficient to use
     */
    public void setCoefficient(double a) {
        this.coefficient = a;
    }

    /**
     * Get the newBound.
     *
     * @return a number
     */
    public double getOffset() {
        return offset;
    }

    /**
     * Set the newBound.
     *
     * @param b the newBound to use
     */
    public void setOffset(double b) {
        this.offset = b;
    }
}
