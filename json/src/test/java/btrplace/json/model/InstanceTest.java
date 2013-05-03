package btrplace.json.model;

import btrplace.model.DefaultMapping;
import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Online;
import btrplace.model.constraint.Running;
import btrplace.model.constraint.SatConstraint;
import btrplace.test.PremadeElements;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link Instance}.
 *
 * @author Fabien Hermenier
 */
public class InstanceTest implements PremadeElements {

    @Test
    public void testInstantiation() {
        Model mo = Mockito.mock(Model.class);
        List<SatConstraint> l = new ArrayList<>();
        l.add(Mockito.mock(SatConstraint.class));
        Instance i = new Instance(mo, l);
        Assert.assertEquals(i.getModel(), mo);
        Assert.assertEquals(i.getConstraints(), l);
    }

    @Test
    public void testEqualsAndHashcode() {
        Mapping ma = new DefaultMapping();
        ma.addOnlineNode(n1);
        ma.addOfflineNode(n1);
        ma.addReadyVM(vm1);
        Model mo = new DefaultModel(ma);
        List<SatConstraint> cstrs = new ArrayList<>();
        cstrs.add(new Online(ma.getAllNodes()));
        cstrs.add(new Running(Collections.singleton(vm1)));
        Instance i = new Instance(mo, cstrs);
        Instance i2 = new Instance(mo.clone(), new ArrayList<>(cstrs));

        Assert.assertEquals(i, i2);
        Assert.assertEquals(i.hashCode(), i2.hashCode());

        i2.getModel().getMapping().addReadyVM(vm3);
        Assert.assertNotEquals(i, i2);

    }
}
