/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
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

package btrplace.solver.choco.actionModel;


import solver.variables.IntVar;

/**
 * An interface to specify an VM action model related
 * to a VM that is already running and that will keep running.
 *
 * @author Fabien Hermenier
 */
public interface KeepRunningVMModel extends VMActionModel {


    /**
     * Indicates if the VMs is staying on its current hosting node.
     *
     * @return a variable instantiated to {@code 1} iff the VM is staying on its current node.
     * Instantiated to {@code 0} otherwise
     */
    IntVar isStaying();
}
