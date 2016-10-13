/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco;

import org.btrplace.model.*;
import org.btrplace.scheduler.SchedulerException;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class LifeCycleViolationException extends SchedulerException {

    public LifeCycleViolationException(Model mo, VM v, VMState cur, VMState dst) {
        super(mo, "No model available for VM transition " + cur + " -> " + dst);
    }

    public LifeCycleViolationException(Model mo, Node n, NodeState cur, Set<NodeState> dst) {
        super(mo, "No model available for Node transition " + n + " -> " + dst);
    }

}
