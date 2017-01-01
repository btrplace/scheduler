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

package org.btrplace.safeplace.spec.type;

import org.btrplace.safeplace.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public class NodeStateType implements Litteral, Atomic {

    public enum Type {ONLINE, BOOTING, HALTING, OFFLINE}

    private static NodeStateType instance = new NodeStateType();

    private NodeStateType() {
    }

    public static NodeStateType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "nodeState";
    }

    @Override
    public Constant parse(String n) {
        try {
            return new Constant(Type.valueOf(n.toUpperCase()), this);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
