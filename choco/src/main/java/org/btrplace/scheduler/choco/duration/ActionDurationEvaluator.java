/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.duration;

import org.btrplace.model.Element;
import org.btrplace.model.Model;


/**
 * Interface to specify the duration evaluator for a possible action on an element.
 *
 * @author Fabien Hermenier
 */
@FunctionalInterface
public interface ActionDurationEvaluator<E extends Element> {

    /**
     * Evaluate the duration of the action on a given element.
     *
     * @param mo the model to consider
     * @param e  the VM
     * @return a positive integer
     */
    int evaluate(Model mo, E e);
}
