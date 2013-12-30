package btrplace.solver.api.cstrSpec;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;

/**
 * @author Fabien Hermenier
 */
public class TestCstrSpecLexer {

    @Test
    public void split() throws Exception {
        String path = "src/test/resources/v2.cspec";
        ANTLRInputStream is = new ANTLRInputStream(new FileReader(new File(path)));
        CstrSpecLexer lexer = new CstrSpecLexer(is);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        System.out.println(tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(tokens.get(i).getText());
        }
        Assert.fail();
    }
}
