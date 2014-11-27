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

package org.btrplace.safeplace.runner;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Fabien Hermenier
 */
public class TeePrintStream extends PrintStream {
    private final PrintStream second;

    public TeePrintStream(OutputStream main, PrintStream second) {
        super(main);
        this.second = second;
    }

    /**
     * Closes the main stream.
     * The second stream is just flushed but <b>not</b> closed.
     *
     * @see java.io.PrintStream#close()
     */
    @Override
    public void close() {
        // just for documentation
        super.close();
    }

    @Override
    public void flush() {
        super.flush();
        second.flush();
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        super.write(buf, off, len);
        second.write(buf, off, len);
    }

    @Override
    public void write(int b) {
        super.write(b);
        second.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        second.write(b);
    }
}