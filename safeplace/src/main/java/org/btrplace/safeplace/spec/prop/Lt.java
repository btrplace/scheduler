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

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.verification.spec.SpecModel;

/**
 * @author Fabien Hermenier
 */
public class Lt extends AtomicProp {

    public Lt(Term a, Term b) {
        super(a, b, "<");
    }

    @Override
    public AtomicProp not() {
        return new Lt(b, a);
    }

    @Override
    public Boolean eval(SpecModel m) {
        Integer vA = (Integer) a.eval(m);
        Integer vB = (Integer) b.eval(m);
        if (vA == null || vB == null) {
            return null;
        }
        return vA < vB;
    }
}
