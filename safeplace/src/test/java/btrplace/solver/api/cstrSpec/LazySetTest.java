package btrplace.solver.api.cstrSpec;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.*;
import btrplace.solver.api.cstrSpec.invariant.func.NodeState;
import btrplace.solver.api.cstrSpec.invariant.type.NodeStateType;
import btrplace.solver.api.cstrSpec.invariant.type.NodeType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * @author Fabien Hermenier
 */
public class LazySetTest {

    @Test
    public void testSimple() {
        Variable i = new Variable("i", NodeType.getInstance());
        Proposition p = new Eq(new NodeState(Collections.<Term>singletonList(i)), new Constant(NodeStateType.Type.online, NodeStateType.getInstance()));
        LazySet cs = null;//new LazySet(i, p);
        Model mo = new DefaultModel();
        mo.getMapping().addOnlineNode(mo.newNode());
        mo.getMapping().addOnlineNode(mo.newNode());
        mo.getMapping().addOfflineNode(mo.newNode());
        Assert.assertEquals(cs.expand(mo).size(), 2);
    }
}
