/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.scheduler.choco.extensions.pack;

import org.chocosolver.solver.exception.ContradictionException;

/**
 * Created by fabien.hermenier on 11/05/2017.
 */
public interface KSDecorator {

  void postAssignItem(int item, int bin) throws ContradictionException;

  void postRemoveItem(int item, int bin) throws ContradictionException;

  void postInitialize() throws ContradictionException;
}
