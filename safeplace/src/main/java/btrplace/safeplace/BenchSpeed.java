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

import btrplace.safeplace.test.Bench;
import btrplace.safeplace.test.Test;

/**
 * @author Fabien Hermenier
 */
public class BenchSpeed {

    public static void main(String[] args) {

        int nbVMs = 3;
        int nbNodes = 3;
        boolean reduce = true;
        int nbTests = 1000;
        int to = 30;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--vms":
                    Bench.nbVMs = Integer.parseInt(args[++i]);
                    break;
                case "--nodes":
                    nbNodes = Integer.parseInt(args[++i]);
                    break;
                case "--tests":
                    Bench.tests = Integer.parseInt(args[++i]);
                    break;
                case "--to":
                    Bench.to = Integer.parseInt(args[++i]);
                    break;
                case "--reduce":
                    Bench.reduce = true;
                    break;
                case "--no-reduce":
                    Bench.reduce = false;
                    break;
                default:
                    System.err.println("Unsupported operation: " + args[i]);
            }
        }
        Test.main(new String[]{});
    }
}
