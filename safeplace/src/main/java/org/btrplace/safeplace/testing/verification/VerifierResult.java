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

package org.btrplace.safeplace.testing.verification;

import org.btrplace.plan.event.Action;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

/**
 * @author Fabien Hermenier
 */
public class VerifierResult {

    private Boolean b;

    private String ex;

    private static final VerifierResult success = new VerifierResult(true, "");

    public VerifierResult(Boolean b, Action a) {
        this.b = b;
        this.ex = a.toString();
    }


    public VerifierResult(Boolean b, String ex) {
        this.b = b;
        this.ex = ex;
    }

    public static VerifierResult newOk() {
        return success;
    }

    public static VerifierResult newKo(String ex) {
        return new VerifierResult(false, ex);
    }

    public static VerifierResult newError(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return new VerifierResult(null, sw.toString());
    }

    public static VerifierResult newFailure(Action a) {
        return new VerifierResult(false, a);
    }

    public Boolean getStatus() {
        return b;
    }

    public String getException() {
        return ex;
    }

    @Override
    public String toString() {
        if (b == null) {
            return ex;
        }
        return b ? "true" : ex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VerifierResult that = (VerifierResult) o;

        if (!b.equals(that.b)) return false;
        return ex != null ? ex.equals(that.ex) : that.ex == null;

    }

    @Override
    public int hashCode() {
        return Objects.hash(b, ex);
    }


}
