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

package org.btrplace.safeplace.testing;

import com.google.common.io.Files;
import com.google.protobuf.TextFormat;
import net.minidev.json.parser.ParseException;
import org.btrplace.json.JSONConverterException;
import org.btrplace.safeplace.spec.Constraint;
import org.btrplace.safeplace.testing.fuzzer.ReconfigurationPlanFuzzer;
import org.btrplace.safeplace.testing.fuzzer.decorators.FuzzerDecorator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class Replay implements TestCaseFuzzer {


    public BufferedReader in;

    private List<Constraint> constraints;

    public Replay(Path path) throws IOException {

        in = Files.newReader(path.toFile(), Charset.defaultCharset());
    }

    @Override
    public TestCaseFuzzer with(String var, int val) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public TestCaseFuzzer with(String var, int min, int max) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public TestCaseFuzzer with(String var, int[] vals) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public TestCaseFuzzer with(String var, String val) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public TestCaseFuzzer with(String var, String[] vals) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public TestCaseFuzzer with(String var, Domain d) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public TestCaseFuzzer validating(Constraint c, Tester t) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public TestCaseFuzzer restriction(Set<Restriction> domain) {
        throw new UnsupportedOperationException("Not available in replay mode");
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
    public TestCaseFuzzer constraint(Constraint cstr) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public Constraint constraint() {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public TestCase get() {
        try {
            in.readLine(); // "[" or "," or "]"
            String json = in.readLine();
            if (json == null) {
                return null;
            }
            TestCase tc = TestCase.fromJSON(constraints, json);
            return tc;
        } catch (IOException | ParseException | JSONConverterException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReconfigurationPlanFuzzer srcOffNodes(double ratio) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public ReconfigurationPlanFuzzer dstOffNodes(double ratio) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public ReconfigurationPlanFuzzer srcVMs(double ready, double running, double sleeping) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public ReconfigurationPlanFuzzer dstVMs(double ready, double running, double sleeping) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public ReconfigurationPlanFuzzer vms(int n) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public ReconfigurationPlanFuzzer nodes(int n) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public ReconfigurationPlanFuzzer durations(int min, int max) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public ReconfigurationPlanFuzzer with(FuzzerDecorator f) {
        throw new UnsupportedOperationException("Not available in replay mode");
    }

    @Override
    public Replay supportedConstraints(List<Constraint> cstrs) {
        this.constraints = cstrs;
        return this;
    }

}
