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

package btrplace.json;


import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.io.IOException;
import java.io.Reader;

/**
 * Tools to help at converting object to JSON.
 *
 * @author Fabien Hermenier
 */
public final class JSONUtils {

    private JSONUtils() {
    }

    /**
     * Extract one JSON object from a string
     *
     * @param str the string to parse
     * @return the resulting JSONObject
     * @throws ParseException if an error occurred while parsing
     */
    public static JSONObject readObject(String str) throws ParseException, JSONConverterException {
        JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
        Object o = p.parse(str);
        if (!(o instanceof JSONObject)) {
            throw new JSONConverterException("Unable to parse a JSON object");
        }
        return (JSONObject) o;
    }

    /**
     * Extract one JSON object from a stream.
     * The stream is closed afterward
     *
     * @param in the stream to read
     * @return the resulting JSONObject
     * @throws ParseException if an error occurred while parsing
     */
    public static JSONObject readObject(Reader in) throws ParseException, JSONConverterException, IOException {
        try {
            JSONParser p = new JSONParser(JSONParser.MODE_RFC4627);
            Object o = p.parse(in);
            if (!(o instanceof JSONObject)) {
                throw new JSONConverterException("Unable to parse a JSON object");
            }
            return (JSONObject) o;
        } finally {
            in.close();
        }
    }
}
