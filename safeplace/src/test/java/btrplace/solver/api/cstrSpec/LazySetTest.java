package btrplace.solver.api.cstrSpec;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fabien Hermenier
 */
public class LazySetTest {

    @Test
    public void testSimple() {
        Assert.fail();
        /*Variable i = new Variable("i", NodeType.getInstance());
        Proposition p = new Eq(new NodeState(Collections.<Term>singletonList(i)), new Constant(NodeStateType.Type.online, NodeStateType.getInstance()));
        LazySet cs = null;//new LazySet(i, p);
        Model mo = new DefaultModel();
        mo.getMapping().addOnlineNode(mo.newNode());
        mo.getMapping().addOnlineNode(mo.newNode());
        mo.getMapping().addOfflineNode(mo.newNode());
        Assert.assertEquals(cs.expand(mo).size(), 2);*/
    }
}
