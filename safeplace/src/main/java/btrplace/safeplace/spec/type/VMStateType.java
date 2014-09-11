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

package btrplace.safeplace.spec.type;

import btrplace.safeplace.spec.term.Constant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class VMStateType extends Atomic {

    public static enum Type {ready, booting, running, migrating, suspending, sleeping, resuming, halting, terminated}

    private static VMStateType instance = new VMStateType();

    private VMStateType() {
        Set<Object> s = new HashSet<>();
        Collections.addAll(s, Type.values());
    }

    public static VMStateType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        try {
            Type.valueOf(n);
            return true;
        } catch (Exception e) {

        }
        return false;
    }

    @Override
    public String label() {
        return "vmState";
    }

    @Override
    public Constant newValue(String n) {
        return new Constant(Type.valueOf(n), this);
    }

    @Override
    public boolean comparable(btrplace.safeplace.spec.type.Type t) {
        return t.equals(NoneType.getInstance()) || equals(t);
    }

}
