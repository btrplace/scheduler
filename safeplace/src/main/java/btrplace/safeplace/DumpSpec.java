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

package btrplace.safeplace;

import btrplace.safeplace.spec.SpecExtractor;

/**
 * @author Fabien Hermenier
 */
public class DumpSpec {

    public static void main(String[] args) throws Exception {
        SpecExtractor r = new SpecExtractor();
        Specification spec = r.extract();

        System.out.println("/* Core constraints */");
        spec.getConstraints().stream().filter(c -> c.isCore()).forEach(c -> System.out.println(c.pretty()));

        System.out.println("\n/* Side constraints */");
        spec.getConstraints().stream().filter(c -> !c.isCore()).forEach(c -> System.out.println(c.pretty()));

    }
}
