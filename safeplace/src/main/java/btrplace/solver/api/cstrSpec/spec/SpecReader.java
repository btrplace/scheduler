package btrplace.solver.api.cstrSpec.spec;

import btrplace.solver.api.cstrSpec.CstrSpecLexer;
import btrplace.solver.api.cstrSpec.CstrSpecParser;
import btrplace.solver.api.cstrSpec.Specification;
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
public class SpecReader {

    public Specification getSpecification(String buffer) throws Exception {
        try (StringReader r = new StringReader(buffer)) {
            return getSpecification(r, "");
        }
    }

    public Specification getSpecification(File f) throws Exception {
        try (FileReader r = new FileReader(f)) {
            return getSpecification(r, f.getName());
        }
    }

    public Specification getSpecification(Reader in, String name) throws Exception {
        ANTLRInputStream is = new ANTLRInputStream(in);
        CstrSpecLexer lexer = new CstrSpecLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CstrSpecParser parser = new CstrSpecParser(tokens);
        ParseTree tree = parser.spec();
        MyCstrSpecVisitor v = new MyCstrSpecVisitor(name);
        return v.getSpecification(tree);
    }
}
