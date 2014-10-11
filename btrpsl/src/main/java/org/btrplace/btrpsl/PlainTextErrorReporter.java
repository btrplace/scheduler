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

package org.btrplace.btrpsl;

import java.util.*;

/**
 * A structure to report all the errors detected when parsing a script.
 * Output is a plain text.
 *
 * @author Fabien Hermenier
 */
public class PlainTextErrorReporter implements ErrorReporter {

    private static Comparator<ErrorMessage> cmp = new Comparator<ErrorMessage>() {
        @Override
        public int compare(ErrorMessage e1, ErrorMessage e2) {
            return e1.lineNo() - e2.lineNo();
        }
    };
    /**
     * The error messages.
     */
    private List<ErrorMessage> errors;

    private Script script;

    /**
     * Make a new instance.
     *
     * @param v the script that is built
     */
    public PlainTextErrorReporter(Script v) {
        errors = new LinkedList<>();
        this.script = v;
    }

    /**
     * Get the number of errors
     *
     * @return an integer
     */
    public int size() {
        return errors.size();
    }

    /**
     * Print all the errors, one per line.
     *
     * @return all the reported errors
     */
    public String toString() {
        StringBuilder b = new StringBuilder();
        Collections.sort(errors, cmp);
        for (Iterator<ErrorMessage> ite = errors.iterator(); ite.hasNext(); ) {
            b.append(ite.next().toString());
            if (ite.hasNext()) {
                b.append('\n');
            }
        }
        return b.toString();
    }

    @Override
    public List<ErrorMessage> getErrors() {
        return this.errors;
    }

    @Override
    public void append(int lineNo, int colNo, String msg) {
        errors.add(new ErrorMessage(script.id(), lineNo, colNo, msg));
    }

    @Override
    public void updateNamespace() {

        for (ErrorMessage msg : errors) {
            if (msg.getNamespace() == null) {
                msg.setNamespace(script.id());
            }
        }
    }
}
