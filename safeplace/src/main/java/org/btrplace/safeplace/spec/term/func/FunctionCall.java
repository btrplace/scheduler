/*
 * Copyright  2020 The BtrPlace Authors. All rights reserved.
 * Use of this source code is governed by a LGPL-style
 * license that can be found in the LICENSE.txt file.
 */

package org.btrplace.safeplace.spec.term.func;

import org.btrplace.safeplace.spec.term.Term;
import org.btrplace.safeplace.spec.type.Type;
import org.btrplace.safeplace.testing.verification.spec.Context;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class FunctionCall<T> implements Term<T> {

    protected Function<T> c;

  private final List<Term> args;

    public enum Moment {
        BEGIN {
            @Override
            public String toString() {
                return "^";
            }
        },
        END {
            @Override
            public String toString() {
                return "$";
            }
        },
        ANY {
            @Override
            public String toString() {
                return "";
            }
        }
    }

  private final Moment moment;

    public FunctionCall(Function<T> c, List<Term> args, Moment m) {
        this.c = c;
        this.args = args;
        this.moment = m;
    }

    @Override
    public Type type() {
        return c.type(args);
    }

    @Override
    public T eval(Context m, Object... objs) {
        Object[] values = new Object[args.size()];
        int i = 0;
        for (Term t : args) {
            values[i++] = t.eval(m);
        }

        if (moment.equals(Moment.BEGIN)) {
            return c.eval(m.getRootContext(), values);
        }
        return c.eval(m, values);
    }

    @Override
    public String toString() {
        return args.stream().map(Object::toString)
                .collect(Collectors.joining(", ", moment + c.id() + "(", ")"));
    }

    public List<Term> args() {
        return args;
    }

    public static String toString(String id, List<Term> args) {
        return args.stream().map(t -> t.type().toString())
                .collect(Collectors.joining(", ", id + "(", ")"));
    }


}
