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

package btrplace.solver.choco;

import btrplace.model.Model;

import java.util.Set;


/**
 * An interface to specify an object that can estimated a supposed
 * set of misplaced VMs in a model.
 * <p/>
 * This information will be used by the {@link ChocoReconfigurationAlgorithm}
 * to restrict the amount of VMs to consider in the reconfiguration algorithm
 * to a minimum.
 * <p/>
 * The set of mis-placed VMs is not necessarily optimal but it must
 * be good enough to be able to compute a solution by only managing
 * these VMs.
 *
 * @author Fabien Hermenier
 */
public interface MisplacedVMsEstimator {

    /**
     * Get the VMs that are supposed to be mis-placed.
     *
     * @param m the model to use to inspect the VMs.
     * @return a set of VMs identifier that may be empty (when no VMs are misplaced)
     */
    Set<Integer> getMisPlacedVMs(Model m);
}
