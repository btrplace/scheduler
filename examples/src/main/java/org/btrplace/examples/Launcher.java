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

package org.btrplace.examples;

/**
 * CLI app to launch tutorials.
 *
 * @author Fabien Hermenier
 */
public final class Launcher {

    /**
     * CLI. No instantiation.
     */
    private Launcher() {
    }

    /**
     * CLI. First value of the array should be the name of a class inheriting from {@link org.btrplace.examples.Example}.
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Expecting a class that inherit from btrplace.examples.Example as a parameter");
            System.exit(1);
        }
        try {
            Class c = Class.forName(args[0]);
            boolean validInterface = false;
            for (Class i : c.getInterfaces()) {
                if (i.equals(Example.class)) {
                    validInterface = true;
                    break;
                }
            }
            if (!validInterface) {
                System.err.println("The class must implement '" + Example.class.getName() + "'");
                System.exit(1);
            }
            Example ex = (Example) c.newInstance();
            boolean ret = ex.run();
            if (!ret) {
                System.err.println("The example '" + ex.getClass().getSimpleName() + "' failed");
                System.exit(1);
            }
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (InstantiationException e) {
            System.err.println("Unable to instantiate " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
