/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.btrpsl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A structure to report all the errors detected when parsing a script.
 * Output is a plain text.
 *
 * @author Fabien Hermenier
 */
public class PlainTextErrorReporter implements ErrorReporter {

  private static final Comparator<ErrorMessage> cmp = (e1, e2) -> e1.lineNo() - e2.lineNo();
  /**
   * The error messages.
   */
  private final List<ErrorMessage> errors;

    private final Script script;

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
    @Override
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

        errors.stream().filter(msg -> msg.getNamespace() == null).forEach(msg -> msg.setNamespace(script.id()));
    }
}
