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

package org.btrplace.safeplace.testing.reporting;

import com.google.common.io.Files;
import org.btrplace.safeplace.testing.TestCaseResult;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * A report where the results that validate a predicate are stored for a latter analysis.
 *
 * @author Fabien Hermenier
 */
public class StoredReport extends Counting {

    private Path file;

    private Predicate<TestCaseResult> toSave;

    /**
     * New reportTo.
     * All the results will be stored
     *
     * @param file the output file
     */
    public StoredReport(Path file) {
        this(file, r -> true);
    }

    /**
     * New reportTo.
     *
     * @param file   the output file
     * @param toSave the predicate to indicate the results to save
     */
    public StoredReport(Path file, Predicate<TestCaseResult> toSave) {
        this.file = file;
        this.toSave = toSave;
    }

    private void save(TestCaseResult res) {
        try {
            java.nio.file.Files.deleteIfExists(file);
            Files.append("--------------------------------------------------------\n" + res.toString(), file.toFile(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void with(TestCaseResult r) {
        super.with(r);

        if (toSave.test(r)) {
            save(r);
        }
    }
}
