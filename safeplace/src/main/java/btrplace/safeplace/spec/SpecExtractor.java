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

package btrplace.safeplace.spec;

import btrplace.CoreConstraint;
import btrplace.CoreConstraints;
import btrplace.SideConstraint;
import btrplace.safeplace.Constraint;
import btrplace.safeplace.CstrSpecLexer;
import btrplace.safeplace.CstrSpecParser;
import btrplace.safeplace.Specification;
import btrplace.safeplace.spec.term.UserVar;
import eu.infomas.annotation.AnnotationDetector;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class SpecExtractor implements AnnotationDetector.TypeReporter {

    private List<CoreConstraint> cores;

    private List<Side> sides;

    public SpecExtractor() {
        cores = new ArrayList<>();
        sides = new ArrayList<>();
    }

    @Override
    public Class<? extends Annotation>[] annotations() {
        return new Class[]{CoreConstraints.class, CoreConstraint.class, SideConstraint.class};
    }

    @Override
    public void reportTypeAnnotation(Class<? extends Annotation> an, String className) {
        try {
            Class cl = Class.forName(className);
            CoreConstraints cores = (CoreConstraints) cl.getAnnotation(CoreConstraints.class);
            if (cores != null) {
                for (CoreConstraint core : cores.value()) {
                    this.cores.add(core);

                }
            }
            SideConstraint side = (SideConstraint) cl.getAnnotation(SideConstraint.class);
            if (side != null) {
                String name = cl.getSimpleName();
                this.sides.add(new Side(side, name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CommonTokenStream getTokens(String source) throws IOException {
        ANTLRInputStream is = new ANTLRInputStream(new StringReader(source));
        CstrSpecLexer lexer = new CstrSpecLexer(is);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new DiagnosticErrorListener());
        //lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);
        return new CommonTokenStream(lexer);
    }

    public Specification extract() throws IOException, SpecException {
        AnnotationDetector detector = new AnnotationDetector(this);
        detector.detect();

        List<Constraint> l = new ArrayList<>();
        for (CoreConstraint core : cores) {
            l.add(parseCore(core));
        }
        sides = resolveDepedencies(sides);
        for (Side s : sides) {
            Constraint c = parseSide(s, l);
            l.add(c);
        }
        return new Specification(l);
    }

    private List<UserVar> makeArgs(String cl, String[] strings) throws IOException, SpecException {
        List<UserVar> args = new ArrayList<>();
        for (String arg : strings) {
            CstrSpecParser parser = new CstrSpecParser(getTokens(arg));
            ParseTree tree = parser.typedef();
            MyCstrSpecVisitor v = new MyCstrSpecVisitor(cl);
            args.add(v.getUserVar(cl, tree));
        }
        return args;
    }

    private Constraint parseSide(Side s, List<Constraint> known) throws IOException, SpecException {
        List<UserVar> args = makeArgs(s.cl, s.s.args());
        new ArrayList<>();
        CstrSpecParser parser = new CstrSpecParser(getTokens(s.s.inv()));
        ParseTree tree = parser.formula();
        MyCstrSpecVisitor v = new MyCstrSpecVisitor(s.cl);

        return v.getSideConstraint(s.cl, args, known, tree);
    }


    private Constraint parseCore(CoreConstraint core) throws IOException, SpecException {
        CommonTokenStream tokens = getTokens(core.inv());
        CstrSpecParser parser = new CstrSpecParser(tokens);
        ParseTree tree = parser.formula();
        MyCstrSpecVisitor v = new MyCstrSpecVisitor(core.name());
        return v.getCoreConstraint(core.name(), tree);
    }

    public static void main(String[] args) {
        SpecExtractor s = new SpecExtractor();
        try {
            Specification spec = s.extract();
            for (Constraint c : spec.getConstraints()) {
                System.out.println(c.pretty());
            }
            System.out.println(spec.getConstraints().size() + " constraint(s)");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }

    private static class Side {
        SideConstraint s;
        String cl;

        public Side(SideConstraint c, String n) {
            cl = n;
            s = c;
        }
    }

    private List<Side> resolveDepedencies(List<Side> sides) {
        List<String> ids = new ArrayList<>(sides.size());
        List<Node> roots = new ArrayList<>();
        for (Side s : sides) {
            ids.add(s.cl);
        }
        for (Side s : sides) {
            Node n = new Node(s);
            for (String id : ids) {
                if (s.s.inv().contains(id)) {
                    n.pre.add(id);
                }
            }
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
                        n.pre.remove(s.cl);
                    }
                }
            }
        }
        return order;
    }

    private static class Node {
        Side s;
        List<String> pre;

        public Node(Side s) {
            this.s = s;
            pre = new ArrayList<>();
        }
    }
}
