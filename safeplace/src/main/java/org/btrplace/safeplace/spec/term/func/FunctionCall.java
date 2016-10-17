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

    private Function<T> c;

    private List<Term> args;

    public enum Moment {
        begin {
            @Override
            public String toString() {
                return "^";
            }
        },
        end {
            @Override
            public String toString() {
                return "$";
            }
        },
        any {
            @Override
            public String toString() {
                return "";
            }
        }
    }

    private Moment moment;

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
