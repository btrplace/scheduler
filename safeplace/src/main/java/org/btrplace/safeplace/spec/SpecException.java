/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec;

import org.antlr.v4.runtime.Token;
import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.term.func.Function;
import org.btrplace.safeplace.spec.type.Type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class SpecException extends RuntimeException {

    private final String name;

    private final int column;

    public SpecException(String name, int c, String s) {
        super(s);
        this.name = name;
        this.column = c;
    }

    @Override
    public String getMessage() {
        return "[" + name + ": " + column + "]: " + super.getMessage();
    }

    public static SpecException typeMismatch(String name, int c, Type expected, Type got) {
        return new SpecException(name, c, "Type mismatch. Expected '" + expected + "', got '" + got + "'");
    }

    public static SpecException unknownSymbol(String name, Token sym) {
        return new SpecException(name, sym.getCharPositionInLine(), "Cannot resolve symbol '" + sym.getText() + "'");
    }

    public static SpecException unsupportedOperation(String name, Type t1, Token to, Type t2) {
        return new SpecException(name, to.getCharPositionInLine(), "Unsupported operation '" + t1 + " " + to.getText() + " " + t2 + "'");
    }

    public static SpecException badFunctionCall(String name, Token t, Function f, List<Term> args) {
        String got = args.stream().map(x -> x.type().toString()).collect(Collectors.joining(", ", f.id() + "(", ")"));
        return new SpecException(name, t.getCharPositionInLine(), "Invalid call. Expected '" + Function.toString(f) + "'. Got '" + got);
    }

}
