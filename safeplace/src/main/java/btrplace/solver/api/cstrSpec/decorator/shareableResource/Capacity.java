package btrplace.solver.api.cstrSpec.decorator.shareableResource;

import btrplace.model.Instance;
import btrplace.model.Node;
import btrplace.model.view.ShareableResource;
import btrplace.solver.api.cstrSpec.decorator.InstanceDecorator;
import btrplace.solver.api.cstrSpec.decorator.Registry;

import java.util.Collections;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Capacity implements InstanceDecorator {

    @Override
    public boolean decorate(Instance i, String[] params, Registry reg) {
        //capacity n1 cpu 3
        String fqvn = ShareableResource.VIEW_ID_BASE + params[2];
        Node n = reg.getNode(params[1]);
        int qty = Integer.parseInt(params[3]);
        ShareableResource rc = (ShareableResource) i.getModel().getView(fqvn);
        if (rc == null) {
            rc = new ShareableResource(params[2]);
            i.getModel().attach(rc);
        }
        rc.setCapacity(n, qty);
        return true;
    }

    @Override
    public String id() {
        return "allocate";
    }

    @Override
    public List<String> provide() {
        return Collections.emptyList();
    }

    @Override
    public List<String> require() {
        return Collections.emptyList();
    }
}
