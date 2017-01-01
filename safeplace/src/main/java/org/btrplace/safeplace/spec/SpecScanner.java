/*
 * Copyright (c) 2017 University Nice Sophia Antipolis
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

package org.btrplace.safeplace.spec;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.btrplace.model.constraint.SatConstraint;
import org.btrplace.model.constraint.SideConstraint;
import org.btrplace.safeplace.spec.antlr.CstrSpecLexer;
import org.btrplace.safeplace.spec.antlr.CstrSpecParser;
import org.btrplace.safeplace.spec.prop.Proposition;
import org.btrplace.safeplace.spec.term.UserVar;
import org.btrplace.safeplace.spec.term.func.Function;
import org.btrplace.scheduler.CoreConstraint;
import org.btrplace.scheduler.CoreConstraints;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Fabien Hermenier
 */
public class SpecScanner {

    private FastClasspathScanner scanner;

    private List<Side> sides;
    private List<Function> functions;

    public SpecScanner() {

        functions = Collections.synchronizedList(new ArrayList<>());
        sides = Collections.synchronizedList(new ArrayList<>());

        scanner = new FastClasspathScanner();
    }

    /**
     * @throws SpecException
     */
    public List<org.btrplace.safeplace.spec.Constraint> scan() throws IllegalAccessException, InstantiationException, IOException {
        List<CoreConstraint> coreAnnots = Collections.synchronizedList(new ArrayList<>());
        List<Class<? extends Function>> funcs = Collections.synchronizedList(new ArrayList<>());
        scanner.matchClassesImplementing(Function.class, funcs::add);

        scanner.matchClassesWithAnnotation(CoreConstraint.class,
                c -> coreAnnots.add(c.getAnnotation(CoreConstraint.class)));
        scanner.matchClassesWithAnnotation(CoreConstraints.class,
                c -> {
                    CoreConstraint[] x = c.getAnnotationsByType(CoreConstraint.class);
                    coreAnnots.addAll(Arrays.asList(x));
                });

        scanner.matchClassesWithAnnotation(SideConstraint.class,
                c -> sides.add(new Side(c.getAnnotation(SideConstraint.class), (Class<? extends SatConstraint>) c)));

        scanner.scan(Runtime.getRuntime().availableProcessors() - 1);

        for (Class<? extends Function> f : funcs) {
            if (!f.equals(Constraint.class)) {
                functions.add(f.newInstance());
            }
        }

        scanner.matchClassesImplementing(Function.class, c -> {
            try {
                functions.add(c.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        });

        List<org.btrplace.safeplace.spec.Constraint> cstrs = new ArrayList<>();

        for (CoreConstraint c : coreAnnots) {
            cstrs.add(parseCore2(c));
        }

        List<org.btrplace.safeplace.spec.Constraint> l = new ArrayList<>();

        for (Side s : resolveDependencies(sides)) {
            org.btrplace.safeplace.spec.Constraint c = parseSide(s, l);
            l.add(c);
        }

        cstrs.addAll(l);
        return cstrs;
    }

    private List<Side> resolveDependencies(List<Side> sides) {
        List<String> ids = sides.stream().map(s -> s.impl.getSimpleName()).collect(Collectors.toList());
        List<Node> roots = new ArrayList<>();
        for (Side s : sides) {
            Node n = new Node(s);
            n.pre.addAll(ids.stream().filter(id -> s.s.inv().contains(id)).collect(Collectors.toList()));
            roots.add(n);
        }

        List<Side> order = new ArrayList<>();
        while (!roots.isEmpty()) {
            for (Iterator<Node> ite = roots.iterator(); ite.hasNext(); ) {
                Node n = ite.next();
                if (n.pre.isEmpty()) {
                    order.add(n.s);
                    ite.remove();
                } else {
                    for (Side s : order) {
                        n.pre.remove(s.impl.getSimpleName());
                    }
                }
            }
        }
        return order;
    }

    /**
     * @throws SpecException
     */
    private Constraint parseCore2(CoreConstraint core) throws IOException {
        CommonTokenStream tokens = getTokens(core.inv());
        CstrSpecParser parser = new CstrSpecParser(tokens);
        ParseTree tree = parser.formula();
        MyCstrSpecVisitor v = new MyCstrSpecVisitor().library(functions);
        Proposition p = v.getProposition(core.name(), tree);
        return new Constraint(core.name(), p);
    }

    /**
     * @throws SpecException
     */
    private List<UserVar> makeArgs(String cl, String[] strings) throws IOException {
        List<UserVar> args = new ArrayList<>();
        for (String arg : strings) {
            CstrSpecParser parser = new CstrSpecParser(getTokens(arg));
            ParseTree tree = parser.typedef();
            MyCstrSpecVisitor v = new MyCstrSpecVisitor().library(functions);
            args.add(v.getUserVar(cl, tree));
        }
        return args;
    }

    /**
     * @throws SpecException
     */
    private org.btrplace.safeplace.spec.Constraint parseSide(Side s, List<Constraint> known) throws IOException {
        List<UserVar> args = makeArgs(s.impl.getSimpleName(), s.s.args());
        CstrSpecParser parser = new CstrSpecParser(getTokens(s.s.inv()));
        ParseTree tree = parser.formula();
        MyCstrSpecVisitor v = new MyCstrSpecVisitor().library(functions).args(args).constraints(known);
        Proposition p = v.getProposition(s.impl.getSimpleName(), tree);
        return new org.btrplace.safeplace.spec.Constraint(s.impl.getSimpleName(), p).args(args).impl(s.impl);
    }

    private CommonTokenStream getTokens(String source) throws IOException {
        ANTLRInputStream is = new ANTLRInputStream(new StringReader(source));
        CstrSpecLexer lexer = new CstrSpecLexer(is);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new DiagnosticErrorListener());
        return new CommonTokenStream(lexer);
    }

    private static class Side {
        protected SideConstraint s;
        protected Class<? extends SatConstraint> impl;

        public Side(SideConstraint c, Class<? extends SatConstraint> impl) {
            s = c;
            this.impl = impl;
        }
    }

    private static class Node {
        protected Side s;
        protected List<String> pre;

        public Node(Side s) {
            this.s = s;
            pre = new ArrayList<>();
        }
    }
}
