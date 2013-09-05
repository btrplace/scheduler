package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.model.constraint.MaxOnline;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * User: TU HUYNH DANG
 * Date: 5/24/13
 * Time: 9:22 AM
 */
public class MaxOnlinesConverterTest  {

    @Test
    public void testViables() throws JSONConverterException, IOException {
        Model model = new DefaultModel();
        Set<Node> s = new HashSet<Node>(Arrays.asList(model.newNode(), model.newNode(), model.newNode()));
        MaxOnline mo = new MaxOnline(s, 2);
        MaxOnlinesConverter moc = new MaxOnlinesConverter();
        moc.setModel(model);
        MaxOnline new_max = moc.fromJSON(moc.toJSONString(mo));
        Assert.assertEquals(mo, new_max);
    }
}
