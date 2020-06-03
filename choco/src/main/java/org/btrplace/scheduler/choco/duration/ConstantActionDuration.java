/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.scheduler.choco.duration;

import org.btrplace.model.Element;
import org.btrplace.model.Model;


/**
 * Evaluate an action duration to a constant.
 *
 * @author Fabien Hermenier
 */
public class ConstantActionDuration<E extends Element> implements ActionDurationEvaluator<E> {

  private final int duration;

    /**
     * Make a new evaluator.
     *
     * @param d the estimated duration to accomplish the action. Must be strictly positive
     */
    public ConstantActionDuration(int d) {
        this.duration = d;
    }

    @Override
    public int evaluate(Model mo, E e) {
        return duration;
    }


    @Override
    public String toString() {
        return "d=" + duration;
    }
}
