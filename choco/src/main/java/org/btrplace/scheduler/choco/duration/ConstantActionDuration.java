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


/**
 * Evaluate an action duration to a constant.
 *
 * @author Fabien Hermenier
 */
public class ConstantActionDuration<E extends Element> implements ActionDurationEvaluator<E> {

    private int duration;

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
