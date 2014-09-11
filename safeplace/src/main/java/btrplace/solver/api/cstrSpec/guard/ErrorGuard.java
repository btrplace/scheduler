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

package btrplace.solver.api.cstrSpec.guard;

import btrplace.solver.api.cstrSpec.CTestCaseResult;
import btrplace.solver.api.cstrSpec.verification.TestCase;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Fabien Hermenier
 */
public class ErrorGuard implements Guard {

    private AtomicInteger m;

    public ErrorGuard(int m) {
        this.m = new AtomicInteger(m);

    }

    @Override
    public boolean acceptDefiant(TestCase tc) {
        return m.decrementAndGet() > 0;
    }

    @Override
    public boolean acceptCompliant(TestCase tc) {
        return true;
    }

    @Override
    public boolean accept(CTestCaseResult r) {
        if (r.result() != CTestCaseResult.Result.success) {
            return m.decrementAndGet() > 0;
        }
        return true;
    }
}
