/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final String key;

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
            return attrs.get(e, key, -1);
        }
        return parent.evaluate(mo, e);
    }

    /**
     * Get the parent evaluator to use when
     * the attribute is not set.
     *
     * @return an evaluator.
     */
    public ActionDurationEvaluator<E> getParent() {
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
