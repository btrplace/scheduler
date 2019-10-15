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

package org.btrplace.model.view.network;

import org.btrplace.Copyable;

/**
 * Interface to specify a builder to create switches.
 * Each created switch is guarantee for being unique.
 *
 * @author Vincent Kherbache
 */
public interface SwitchBuilder extends Copyable<SwitchBuilder> {

    /**
     * Generate a new non-blocking Switch.
     *
     * @return {@code null} if no identifiers are available for the Switch.
     */
    Switch newSwitch();

    /**
     * Generate a new Switch.
     *
     * @param id the identifier to use for the Switch.
     * @param capacity the switch bandwidth
     * @return a Switch or {@code null} if the identifier is already used.
     */
    Switch newSwitch(int id, int capacity);

    /**
     * Generate a new Switch.
     *
     * @param capacity the maximal BW capacity of the switch
     * @return {@code null} if no identifiers are available for the Switch.
     */
    Switch newSwitch(int capacity);

    /**
     * Check if a given Switch has been defined fot this network view.
     *
     * @param sw the Switch to check.
     * @return {@code true} if the Switch already exists.
     */
    boolean contains(Switch sw);
}
