package btrplace.solver.api.cstrSpec;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class TestParse {

    @Test
    public void testParseSpread() throws Exception {
        ANTLRInputStream in = new ANTLRInputStream(new FileInputStream("src/test/resources/spread.cspec"));
        CstrSpecLexer lexer = new CstrSpecLexer(in);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CstrSpecParser parser = new CstrSpecParser(tokens);
        ParseTree tree = parser.spec();
        ParseTreeWalker walker = new ParseTreeWalker();
        Satisfy sat = new Satisfy();
        walker.walk(sat, tree);
        Proposition p = sat.getInvariant();
        System.err.println("P= " + p);
        System.err.println("P.expand()= " + p.expand());
        StateExtractor ex = new StateExtractor();

        /*Or states = new StateExtractor().extract(p);
        System.err.println(states.size() + " states");
        Assert.assertEquals(states.size(), 32);
        for (Proposition p : states) {
            System.err.println(p);
        }                             */
        Assert.fail();
    }
}
