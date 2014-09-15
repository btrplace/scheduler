/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.btrpsl;

import java.util.List;

/**
 * Interface used to specify a object that collect errors.
 *
 * @author Fabien Hermenier
 */
public interface ErrorReporter {

    /**
     * Report an error.
     *
     * @param lineNo the line index
     * @param colNo  the column index
     * @param msg    the error message
     */
    void append(int lineNo, int colNo, String msg);

    /**
     * Get the reported errors.
     *
     * @return a list of error messages
     */
    List<ErrorMessage> getErrors();

    /**
     * Propagate the script namespace.
     */
    void updateNamespace();
}
