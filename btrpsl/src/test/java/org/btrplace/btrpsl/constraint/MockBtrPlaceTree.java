/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
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
