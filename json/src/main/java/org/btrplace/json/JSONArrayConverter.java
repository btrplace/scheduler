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

package org.btrplace.json;

import net.minidev.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
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

    /**
     * Un-serialize a collection from a file.
     *
     * @param path the file path
     * @return the resulting collection
     * @throws IOException            if an error occurred while reading the stream
     * @throws JSONConverterException if the stream cannot be parsed
     */
    List<E> listFromJSON(File path) throws IOException, JSONConverterException;

    /**
     * Un-serialize a collection from a string.
     *
     * @param buf the string to parse
     * @return the resulting collection
     * @throws JSONConverterException if the stream cannot be parsed
     */
    List<E> listFromJSON(String buf) throws JSONConverterException;

    /**
     * Un-serialize a collection from a stream.
     * The stream must be close afterward
     *
     * @param r the stream to read
     * @return the resulting collection
     * @throws JSONConverterException if the stream cannot be parsed
     */
    List<E> listFromJSON(Reader r) throws JSONConverterException;

    /**
     * Serialize a collection to a string.
     *
     * @param o the collection to serialize
     * @return the JSON message
     * @throws JSONConverterException if an error occurred while converting the collection
     */
    String toJSONString(Collection<E> o) throws JSONConverterException;

    /**
     * Serialize a collection to an appendable stream.
     *
     * @param e the collection to serialize
     * @param w the stream to append to
     * @throws JSONConverterException if an error occurred while converting the collection
     * @throws IOException            if an error occurred while writing the collection
     */
    void toJSON(Collection<E> e, Appendable w) throws JSONConverterException, IOException;

    /**
     * Serialize a collection to a file.
     *
     * @param e    the collection
     * @param path the path name
     * @throws JSONConverterException if an error occurred while converting the collection
     * @throws IOException            if an error occurred while writing the collection
     */
    void toJSON(Collection<E> e, File path) throws JSONConverterException, IOException;
}
