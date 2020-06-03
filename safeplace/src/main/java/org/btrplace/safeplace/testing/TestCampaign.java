/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.testing;

import org.btrplace.safeplace.testing.fuzzer.ConfigurableFuzzer;
import org.btrplace.safeplace.testing.limit.Limits;
import org.btrplace.safeplace.testing.reporting.Report;
import org.btrplace.safeplace.testing.verification.Verifier;
import org.btrplace.scheduler.choco.Parameters;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author Fabien Hermenier
 */
public interface TestCampaign {

    TestCampaign schedulerParams(Parameters ps);

    Parameters schedulerParams();

    TestCampaign verifyWith(Verifier v);

    Limits limits();

    TestCampaign reportTo(Report r);

    TestCampaign replay(Path p);

    ConfigurableFuzzer check(String constraint);

    TestCampaign printProgress(boolean b);

    TestCampaign onDefect(Consumer<TestCaseResult> res);

    Report go();
}
