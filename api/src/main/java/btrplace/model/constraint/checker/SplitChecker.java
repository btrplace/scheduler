package btrplace.model.constraint.checker;

import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.constraint.Split;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class SplitChecker extends AllowAllConstraintChecker {

    private Collection<Set<UUID>> sets;

    public SplitChecker(Split s) {
        super(s);
        sets = s.getSets();
    }

    @Override
    public boolean endsWith(Model mo) {
        Mapping m = mo.getMapping();
        List<Set<UUID>> used = new ArrayList<>(sets.size()); //The pgroups that are used
        for (Set<UUID> vgrp : sets) {
            Set<UUID> myGroup = new HashSet<>();

            //Get the servers used by this group of VMs
            for (UUID vmId : vgrp) {
                if (m.getRunningVMs().contains(vmId)) {
                    UUID nId = m.getVMLocation(vmId);
                    //Is this server inside another group ?
                    for (Set<UUID> pGroup : used) {
                        if (pGroup.contains(nId)) {
                            return false;
                        }
                    }
                    myGroup.add(nId);
                }
            }
            used.add(myGroup);
        }
        return true;
    }
}
