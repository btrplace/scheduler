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
        Satisfy sat = new Satisfy();
        Proposition p = sat.getInvariant("src/test/resources/spread.cspec");
        System.err.println("P= " + p);
        System.err.println("P.expand()= " + p.expand());
        Assert.fail();
    }

    @Test
    public void testParseBan() throws Exception {
        Satisfy sat = new Satisfy();
        Proposition p = sat.getInvariant("src/test/resources/ban.cspec");
        System.err.println("P= " + p);
        System.err.println("P.expand()= " + p.expand());
        Assert.fail();
    }

    @Test
    public void testParseMaxOnline() throws Exception {
        Satisfy sat = new Satisfy();
        Proposition p = sat.getInvariant("src/test/resources/maxOnline.cspec");
        System.err.println("P= " + p);
        System.err.println("P.expand()= " + p.expand());
        Assert.fail();
    }

    @Test
    public void testParseNodeState() throws Exception {
        Satisfy sat = new Satisfy();
        Proposition p = sat.getInvariant("src/test/resources/nodeState.cspec");
        System.err.println("P= " + p);
        System.err.println("P.expand()= " + p.expand());
        Assert.fail();
    }

    @Test
    public void testParseHost() throws Exception {
        Satisfy sat = new Satisfy();
        Proposition p = sat.getInvariant("src/test/resources/host.cspec");
        System.err.println("P= " + p);
        System.err.println("P.expand()= " + p.expand());
        Assert.fail();
    }

    @Test
    public void testParseNoVMOnOfflineNode() throws Exception {
        Satisfy sat = new Satisfy();
        Proposition p = sat.getInvariant("src/test/resources/noVMOnOfflineNode.cspec");
        System.err.println("P= " + p);
        System.err.println("P.expand()= " + p.expand());
        Assert.fail();
    }

}
