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

package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.spec.SpecReader;

import java.io.File;

/**
 * @author Fabien Hermenier
 */
public class SpecStatistics {

    public static void main(String[] args) throws Exception {
        SpecReader r = new SpecReader();
        Specification spec = r.getSpecification(new File(args[0]));
        System.err.println(spec.getConstraints().size() + " constraint(s)");
        System.out.println("id length");
        for (Constraint c : spec.getConstraints()) {

            //int l = c.getProposition().toString().length();
            int l = c.pretty().length();
            //System.err.println(c.pretty());
            System.out.println(c.id() + " " + l);
        }
    }
}
