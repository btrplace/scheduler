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

package btrplace.btrpsl.constraint;

/**
 * @author Fabien Hermenier
 */
public abstract class DefaultConstraintParam<E> implements ConstraintParam<E> {

    private String name;

    private String paramType;

    /**
     * Make a new number parameter.
     *
     * @param n the parameter value
     */
    public DefaultConstraintParam(String n, String t) {
        this.name = n;
        this.paramType = t;
    }

    @Override
    public String prettySignature() {
        return paramType;
    }

    @Override
    public String fullSignature() {
        return name + ": " + paramType;
    }


    @Override
    public String getName() {
        return name;
    }
}
