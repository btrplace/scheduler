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

package btrplace.safeplace.fuzzer;

import btrplace.safeplace.CTestCase;
import btrplace.safeplace.JSONUtils;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * @author Fabien Hermenier
 */
public class CTestsCaseInput implements Iterator<CTestCase>, Iterable<CTestCase> {

    private JsonReader reader;

    private Gson gson;

    public CTestsCaseInput(InputStream in) throws IOException {
        gson = JSONUtils.getInstance().getGson();
        reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        reader.beginArray();

    }

    @Override
    public Iterator<CTestCase> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        try {
            return reader.hasNext();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public CTestCase next() {
        return gson.fromJson(reader, CTestCase.class);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
