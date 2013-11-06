package btrplace.solver.api.cstrSpec;

import btrplace.solver.api.cstrSpec.type.Primitives;
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

    private Primitives primitives;

    public StatesExtractor2() {
        funcs = new Functions();
        primitives = new Primitives();
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

    /*private void evaluate(String id, List<Value> params, List<Forall> binders, Proposition prop) {
        System.err.println("--" + prop + "--");
        System.err.print(id);
        System.err.print("(");
        for (Iterator<Value> ite = params.iterator(); ite.hasNext(); ) {
            Value v = ite.next();
            System.err.print(v);
            if (ite.hasNext()) {
                System.err.print(", ");
            }
        }
        System.err.println("):");
        //Set the value for the parameters

        //Foreach binder combination
        for (Forall f : binders) {
            Variable var = f.getVariable();
            for(Value v : f.domain()) {
                var.set(v);
                System.err.println("\t" + var + "=" + v + "; " + prop.expand());
                var.unset();
            }

        }
    }    */
    public Functions getFunctions() {
        return funcs;
    }

    public Primitives getPrimitives() {
        return primitives;
    }
}
