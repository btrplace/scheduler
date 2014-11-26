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

import org.btrplace.model.Attributes;
import org.btrplace.model.Element;
import org.btrplace.model.Model;

/**
 * A duration evaluator that try to get a duration from an attribute
 * if exists. Otherwise, it relies on a "parent" evaluator.
 * The retrieved attribute value must be an {@link Integer} object.
 *
 * @author Fabien Hermenier
 */
public class ActionDurationFromOptionalAttribute<E extends Element> implements ActionDurationEvaluator<E> {

    private ActionDurationEvaluator<E> parent;

    private String key;

    /**
     * Make a new evaluator.
     *
     * @param attrId the attribute identifier. The associated value must be an {@link Integer}.
     * @param dev    the evaluator to rely on if the attribute is not set or invalid
     */
    public ActionDurationFromOptionalAttribute(String attrId, ActionDurationEvaluator<E> dev) {
        parent = dev;
        key = attrId;
    }

    @Override
    public int evaluate(Model mo, E e) {
        Attributes attrs = mo.getAttributes();
        if (attrs.isSet(e, key)) {
            try {
                return attrs.getInteger(e, key);
            } catch (Exception ex) {
                return parent.evaluate(mo, e);
            }
        }
        return parent.evaluate(mo, e);
    }

    /**
     * Get the parent evaluator to use when
     * the attribute is not set.
     *
     * @return an evaluator.
     */
    public ActionDurationEvaluator getParent() {
        return parent;
    }

    /**
     * Set the parent evaluator.
     *
     * @param dev the evaluator to use
     */
    public void setParent(ActionDurationEvaluator<E> dev) {
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
        return "d= attr[" + key + "]? attr[" + key + "]:" + parent;
    }

}
