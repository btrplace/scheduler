/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

import btrplace.model.DefaultMapping;
import btrplace.model.Mapping;

import java.util.UUID;

/**
 * Unsafe but quick tool to create mappings.
 *
 * @author Fabien Hermenier
 */
public class MappingBuilder {

    private Mapping map;

    public MappingBuilder() {
        map = new DefaultMapping();
    }

    public MappingBuilder run(UUID n, UUID... vms) {
        for (UUID vm : vms) {
            if (!map.addRunningVM(vm, n)) {
                System.err.println("Unable to set '" + vm + "' running. Is '" + n + "' online ?");
            }
        }
        return this;
    }

    public MappingBuilder sleep(UUID n, UUID... vms) {
        for (UUID vm : vms) {
            if (!map.addSleepingVM(vm, n)) {
                System.err.println("Unable to set '" + vm + "' running. Is '" + n + "' online ?");
            }
        }
        return this;
    }

    public MappingBuilder ready(UUID... vms) {
        for (UUID vm : vms) {
            map.addReadyVM(vm);
        }
        return this;
    }

    public MappingBuilder on(UUID... nodes) {
        for (UUID n : nodes) {
            map.addOnlineNode(n);
        }
        return this;
    }

    public MappingBuilder off(UUID... nodes) {
        for (UUID n : nodes) {
            if (!map.addOfflineNode(n)) {
                System.err.println("Unable to set '" + n + "' offline. Is it hosting VMs ?");
            }
        }
        return this;
    }

    public Mapping get() {
        return map;
    }
}
