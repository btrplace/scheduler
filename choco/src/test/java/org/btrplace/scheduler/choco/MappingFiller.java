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

package org.btrplace.scheduler.choco;

import org.btrplace.model.DefaultMapping;
import org.btrplace.model.Mapping;
import org.btrplace.model.Node;
import org.btrplace.model.VM;


/**
 * Unsafe but quick tool to fill mappings.
 *
 * @author Fabien Hermenier
 */
public class MappingFiller {

    private Mapping map;

    public MappingFiller() {
        this(new DefaultMapping());
    }

    public MappingFiller(Mapping m) {
        map = m;
    }

    public MappingFiller run(Node n, VM... vms) {
        for (VM vm : vms) {
            if (!map.addRunningVM(vm, n)) {
                System.err.println("Unable to set '" + vm + "' running. Is '" + n + "' online ?");
            }
        }
        return this;
    }

    public MappingFiller sleep(Node n, VM... vms) {
        for (VM vm : vms) {
            if (!map.addSleepingVM(vm, n)) {
                System.err.println("Unable to set '" + vm + "' running. Is '" + n + "' online ?");
            }
        }
        return this;
    }

    public MappingFiller ready(VM... vms) {
        for (VM vm : vms) {
            map.addReadyVM(vm);
        }
        return this;
    }

    public MappingFiller on(Node... nodes) {
        for (Node n : nodes) {
            map.addOnlineNode(n);
        }
        return this;
    }

    public MappingFiller off(Node... nodes) {
        for (Node n : nodes) {
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
