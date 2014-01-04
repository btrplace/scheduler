package btrplace.solver.api.cstrSpec.invariant;

import btrplace.solver.api.cstrSpec.Constraint;
import btrplace.solver.api.cstrSpec.CstrSpecLexer;
import btrplace.solver.api.cstrSpec.CstrSpecParser;
import btrplace.solver.api.cstrSpec.MyCstrSpecVisitor;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class StatesExtractor2 {

    public Constraint extract(String buf) throws Exception {
        try (StringReader r = new StringReader(buf)) {
            return extract(r);
        }
    }

    public Constraint extract(File f) throws Exception {
        try (FileReader r = new FileReader(f)) {
            return extract(r);
        }
    }

    public List<Constraint> extractConstraints(File f) throws Exception {
        try (FileReader r = new FileReader(f)) {
            return extractConstraints(r);
        }
    }

    public List<Constraint> extractConstraints(Reader in) throws Exception {
        ANTLRInputStream is = new ANTLRInputStream(in);
        CstrSpecLexer lexer = new CstrSpecLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CstrSpecParser parser = new CstrSpecParser(tokens);
        ParseTree tree = parser.spec();
        MyCstrSpecVisitor v = new MyCstrSpecVisitor();
        return v.getConstraints(tree);
    }

    public Constraint extract(Reader in) throws Exception {
        ANTLRInputStream is = new ANTLRInputStream(in);
        CstrSpecLexer lexer = new CstrSpecLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CstrSpecParser parser = new CstrSpecParser(tokens);
        ParseTree tree = parser.constraint();
        MyCstrSpecVisitor v = new MyCstrSpecVisitor();
        return v.getConstraint(tree);
    }
}
