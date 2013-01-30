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

package btrplace.solver.choco.durationEvaluator;

import btrplace.model.Attributes;
import btrplace.solver.choco.DurationEvaluator;

import java.util.UUID;

/**
 * A duration evaluator that try to get a duration from an attribute
 * if exists. Otherwise, it relies on a "parent" evaluator.
 * The retrieved attribute value must be an {@link Integer} object.
 *
 * @author Fabien Hermenier
 */
public class DurationFromAttribute implements DurationEvaluator {

    private Attributes attrs;

    private DurationEvaluator parent;

    private String key;

    /**
     * Make a new evaluator.
     *
     * @param a      the attributed to rely on
     * @param attrId the attribute identifier. The associated value must be an {@link Integer}.
     * @param dev    the evaluator to rely on if the attribute is not set or invalid
     */
    public DurationFromAttribute(Attributes a, String attrId, DurationEvaluator dev) {
        attrs = a;
        parent = dev;
        key = attrId;
    }

    @Override
    public int evaluate(UUID e) {
        if (attrs.isSet(e, key)) {
            try {
                return (Integer) attrs.get(e, key);
            } catch (Exception ex) {
                return parent.evaluate(e);
            }
        }
        return parent.evaluate(e);
    }

    /**
     * Get the parent evaluator to use when
     * the attribute is not set.
     *
     * @return an evaluator.
     */
    public DurationEvaluator getParent() {
        return parent;
    }

    /**
     * Set the parent evaluator.
     *
     * @param dev the evaluator to use
     */
    public void setParent(DurationEvaluator dev) {
        parent = dev;
    }

    /**
     * Get the attribute identifier.
     *
     * @return a String
     */
    public String getAttributeKey() {
        return key;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("d= attr[").append(key).append("]? attr[").append(key).append("]:").append(parent);
        return b.toString();
    }

}
