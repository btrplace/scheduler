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

package org.btrplace.safeplace.spec.prop;

import org.btrplace.safeplace.testing.verification.spec.Context;

/**
 * A logical proposition.
 *
 * @author Fabien Hermenier
 */
public interface Proposition {

    Proposition not();

    Boolean eval(Context m);

    Proposition False = new Proposition() {
        @Override
        public Proposition not() {
            return True;
        }

        @Override
        public Boolean eval(Context m) {
            return Boolean.FALSE;
        }

        @Override
        public String toString() {
            return "false";
        }

        @Override
        public Proposition simplify(Context m) {
            return this;
        }
    };

    Proposition True = new Proposition() {
        @Override
        public Proposition not() {
            return False;
        }

        @Override
        public Boolean eval(Context m) {
            return Boolean.TRUE;
        }

        @Override
        public String toString() {
            return "true";
        }

        @Override
        public Proposition simplify(Context m) {
            return this;
        }
    };

    Proposition simplify(Context m);
}
