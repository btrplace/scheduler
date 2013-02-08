/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace;

import net.minidev.json.JSONObject;

/**
 * Basic abstract solver-API/JSON objects converter.
 *
 * @author Fabien Hermenier
 */
public interface JSONConverter<E> {

    /**
     * JSON to Java object conversion
     *
     * @param in the json object
     * @return the conversion result
     * @throws JSONConverterException if an error occurred while converting the object
     */
    E fromJSON(JSONObject in) throws JSONConverterException;

    /**
     * Java to JSON conversion
     *
     * @param e the Java object to convert
     * @return the conversion result
     */
    JSONObject toJSON(E e);
}
