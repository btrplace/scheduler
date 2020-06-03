/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final List<Constraint> constraints;

  private final Set<Restriction> restriction = EnumSet.allOf(Restriction.class);

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
                if (restriction.contains(Restriction.CONTINUOUS) && !tc.impl().setContinuous(true)) {
                    throw new IllegalArgumentException("Cannot be CONTINUOUS");
                } else if (!tc.impl().setContinuous(false)) {
                    throw new IllegalArgumentException("Cannot be DISCRETE");
                }
            }
            return tc;
        } catch (IOException | ParseException | JSONConverterException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
