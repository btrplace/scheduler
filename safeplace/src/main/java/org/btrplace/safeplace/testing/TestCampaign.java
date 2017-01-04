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
