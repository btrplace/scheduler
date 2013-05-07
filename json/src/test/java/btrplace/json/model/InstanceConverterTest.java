package btrplace.json.model;

import btrplace.json.JSONConverterException;
import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.test.PremadeElements;
import net.minidev.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link InstanceConverter}.
 *
 * @author Fabien Hermenier
 */
public class InstanceConverterTest implements PremadeElements {

    @Test
    public void testConversion() throws JSONConverterException {
        Mapping ma = new DefaultMapping();
        ma.addOnlineNode(n1);
        ma.addOfflineNode(n1);
        ma.addReadyVM(vm1);
        Model mo = new DefaultModel(ma);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Online(ma.getAllNodes()));
        cstrs.add(new Running(Collections.singleton(vm1)));
        Instance i = new Instance(mo, cstrs);

        InstanceConverter conv = new InstanceConverter();
        JSONObject o = conv.toJSON(i);
        Instance res = conv.fromJSON(o);
        Assert.assertEquals(i, res);
    }

}
