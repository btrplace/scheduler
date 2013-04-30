package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Overbook;
import btrplace.model.view.ShareableResource;

import java.util.UUID;

/**
 * @author Fabien Hermenier
 */
public class OverbookChecker extends AllowAllConstraintChecker {

    private String id;

    private double ratio;

    public OverbookChecker(Overbook o) {
        super(o);
        id = o.getResource();
        ratio = o.getRatio();
    }

    @Override
    public boolean endsWith(Model i) {
        Mapping cfg = i.getMapping();
        ShareableResource rc = (ShareableResource) i.getView(ShareableResource.VIEW_ID_BASE + id);
        if (rc == null) {
            return false;
        }
        for (UUID nId : nodes) {
            if (cfg.getOnlineNodes().contains(nId)) {
                //Server capacity with the ratio
                double capa = rc.get(nId) * ratio;
                //Minus the VMs usage
                for (UUID vmId : cfg.getRunningVMs(nId)) {
                    capa -= rc.get(vmId);
                    if (capa < 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
