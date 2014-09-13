/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl;

/**
 * An exception that occurs when there was an issue during the
 * registration of an identifier.
 *
 * @author Fabien Hermenier
 */
public class NamingServiceException extends Exception {

    private String name;

    /**
     * Make a new exception.
     *
     * @param n   the identifier that causes the issue
     * @param msg the error message
     */
    public NamingServiceException(String n, String msg) {
        super(msg);
        this.name = n;
    }

    /**
     * Get the element name.
     *
     * @return a string
     */
    public String getName() {
        return name;
    }
}
