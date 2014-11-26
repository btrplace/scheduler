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

package org.btrplace.btrpsl.constraint;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.btrplace.btrpsl.PlainTextErrorReporter;
import org.btrplace.btrpsl.Script;
import org.btrplace.btrpsl.tree.BtrPlaceTree;

/**
 * A mock BtrPlaceTree to ease SatConstraintBuilder tests.
 *
 * @author Fabien Hermenier
 */
public class MockBtrPlaceTree extends BtrPlaceTree {

    public MockBtrPlaceTree() {
        super(new MockToken(), new PlainTextErrorReporter(new Script()));
    }

    static class MockToken implements Token {
        @Override
        public String getText() {
            return null;
        }

        @Override
        public void setText(String s) {

        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public void setType(int i) {

        }

        @Override
        public int getLine() {
            return 0;
        }

        @Override
        public void setLine(int i) {
        }

        @Override
        public int getCharPositionInLine() {
            return 0;
        }

        @Override
        public void setCharPositionInLine(int i) {

        }

        @Override
        public int getChannel() {
            return 0;
        }

        @Override
        public void setChannel(int i) {

        }

        @Override
        public int getTokenIndex() {
            return 0;
        }

        @Override
        public void setTokenIndex(int i) {

        }

        @Override
        public CharStream getInputStream() {
            return null;
        }

        @Override
        public void setInputStream(CharStream charStream) {

        }
    }
}
