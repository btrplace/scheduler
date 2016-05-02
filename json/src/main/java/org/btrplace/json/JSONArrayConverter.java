/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
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

package org.btrplace.json;

import net.minidev.json.JSONArray;

import java.util.Collection;
import java.util.List;

/**
 * Specify a converter between a JSON formatted message and a collection of objects.
 *
 * @author Fabien Hermenier
 */
public interface JSONArrayConverter<E> {

    /**
     * Un-serialize to a collection.
     *
     * @param in the json array
     * @return the conversion result
     * @throws JSONConverterException if an error occurred while converting the collection
     */
    List<E> listFromJSON(JSONArray in) throws JSONConverterException;

    /**
     * Serialize a collection.
     *
     * @param e the collection to serialize
     * @return the conversion result
     * @throws JSONConverterException if an error occurred while converting the collection
     */
    JSONArray toJSON(Collection<E> e) throws JSONConverterException;
}
