/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
     * @param b    the base. Must be &gt; 0
     */
    public LinearToAResourceActionDuration(String rcId, double a, double b) {
        this.rc = rcId;
        this.coefficient = a;
        this.offset = b;
        if (b < 0) {
            throw new IllegalArgumentException("the base cannot be negative");
        }
    }

    @Override
    public int evaluate(Model mo, E e) {
        ShareableResource r = ShareableResource.get(mo, rc);
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
        return (int) Math.round(coefficient * x + offset);
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

    @Override
    public String toString() {
        return coefficient + " x " + rc + " + " + offset;
    }
}
