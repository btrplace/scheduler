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

package org.btrplace.safeplace.testing.fuzzer;

import com.google.common.io.Files;
import net.minidev.json.parser.ParseException;
import org.btrplace.json.JSONConverterException;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.TestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * An object to read previously saved {@link TestCase}.
 * All the durations are set to 0.
 *
 * @author Fabien Hermenier
 */
public class Replay implements Fuzzer {

    private final BufferedReader in;

    private List<Constraint> constraints;

    private Set<Restriction> restriction = EnumSet.allOf(Restriction.class);

    /**
     * Replay from a given path.
     *
     * @param cstrs the constraint catalog
     * @param path  the file containing the jsons.
     * @throws IOException if an error occurred while reading the file
     */
    public Replay(List<Constraint> cstrs, Path path) throws IOException {
        in = Files.newReader(path.toFile(), Charset.defaultCharset());
        constraints = cstrs;
    }

    @Override
    public long lastFuzzingDuration() {
        return 0;
    }

    @Override
    public long lastValidationDuration() {
        return 0;
    }

    @Override
    public int lastFuzzingIterations() {
        return 1;
    }

    @Override
    public TestCase get() {
        try {
            String json = in.readLine();
            if (json == null) {
                return null;
            }
            TestCase tc = TestCase.fromJSON(constraints, json);
            if (restriction.size() == 1) {
                if (restriction.contains(Restriction.continuous)) {
                    if (!tc.impl().setContinuous(true)) {
                        throw new IllegalArgumentException("Cannot be continuous");
                    }
                } else {
                    if (!tc.impl().setContinuous(false)) {
                        throw new IllegalArgumentException("Cannot be discrete");
                    }
                }
            }
            return tc;
        } catch (IOException | ParseException | JSONConverterException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
