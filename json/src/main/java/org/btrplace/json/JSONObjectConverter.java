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

import net.minidev.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

/**
 * Specify a converter between a JSON formatted message and a object.
 *
 * @author Fabien Hermenier
 */
public interface JSONObjectConverter<E> {

    /**
     * Un-serialize an object.
     *
     * @param in the json object
     * @return the conversion result
     * @throws JSONConverterException if an error occurred while converting the object
     */
    E fromJSON(JSONObject in) throws JSONConverterException;

    /**
     * Serialize an object.
     *
     * @param e the object to serialize
     * @return the conversion result
     * @throws JSONConverterException if an error occurred while converting the object
     */
    JSONObject toJSON(E e) throws JSONConverterException;

    /**
     * Un-serialize an object from a file.
     *
     * @param path the file path
     * @return the resulting object
     * @throws IOException            if an error occurred while reading the stream
     * @throws JSONConverterException if the stream cannot be parsed
     */
    E fromJSON(File path) throws IOException, JSONConverterException;

    /**
     * Un-serialize an object from a string.
     *
     * @param buf the string to parse
     * @return the resulting object
     * @throws JSONConverterException if the stream cannot be parsed
     */
    E fromJSON(String buf) throws JSONConverterException;

    /**
     * Un-serialize an object from a stream.
     * The stream must be close afterward
     *
     * @param r the stream to read
     * @return the resulting object
     * @throws JSONConverterException if the stream cannot be parsed
     */
    E fromJSON(Reader r) throws JSONConverterException;

    /**
     * Serialize an object to a string.
     *
     * @param o the object to serialize
     * @return the JSON message
     * @throws JSONConverterException if an error occurred while converting the object
     */
    String toJSONString(E o) throws JSONConverterException;

    /**
     * Serialize an object to an appendable stream.
     *
     * @param e the object to serialize
     * @param w the stream to append to
     * @throws JSONConverterException if an error occurred while converting the object
     * @throws IOException            if an error occurred while writing the object
     */
    void toJSON(E e, Appendable w) throws JSONConverterException, IOException;

    /**
     * Serialize an object to a file.
     *
     * @param e    the object
     * @param path the path name
     * @throws JSONConverterException if an error occurred while converting the object
     * @throws IOException            if an error occurred while writing the object
     */
    void toJSON(E e, File path) throws JSONConverterException, IOException;
}
