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

package org.btrplace.safeplace.spec.type;

import org.btrplace.safeplace.spec.term.Constant;

/**
 * @author Fabien Hermenier
 */
public class TimeType extends Atomic {

    private static TimeType instance = new TimeType();

    @Override
    public boolean match(String n) {
        if (n.charAt(0) == 't') {
            try {
                Integer.parseInt(n.substring(1));
                return true;
            } catch (Exception e) {
            }
        }
        return false;
    }

    @Override
    public Constant parse(String n) {
        if (match(n)) {
            try {
                return new Constant(Integer.parseInt(n.substring(1)), this);
            } catch (Exception e) {
            }
        }
        return null;
    }

    @Override
    public String label() {
        return "time";
    }

    @Override
    public String toString() {
        return label();
    }

    public static TimeType getInstance() {
        return instance;
    }

    @Override
    public boolean comparable(Type t) {
        return t.equals(NoneType.getInstance()) || equals(t);
    }

}
