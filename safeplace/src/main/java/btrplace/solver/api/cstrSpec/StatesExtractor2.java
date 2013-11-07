package btrplace.solver.api.cstrSpec;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Fabien Hermenier
 */
public class StatesExtractor2 {

    private Functions funcs;

    public StatesExtractor2() {
        funcs = new Functions();
    }

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

    public Constraint extract(Reader in) throws Exception {
        ANTLRInputStream is = new ANTLRInputStream(in);
        CstrSpecLexer lexer = new CstrSpecLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CstrSpecParser parser = new CstrSpecParser(tokens);
        ParseTree tree = parser.constraint();
        MyCstrSpecVisitor v = new MyCstrSpecVisitor(this);
        return v.getConstraint(tree);
    }

    public Functions getFunctions() {
        return funcs;
    }
}
