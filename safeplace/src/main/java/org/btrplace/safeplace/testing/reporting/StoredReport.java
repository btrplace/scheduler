/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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

  private final Path file;

  private final Predicate<TestCaseResult> toSave;

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
