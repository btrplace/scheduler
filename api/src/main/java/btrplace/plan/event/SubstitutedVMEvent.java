package btrplace.plan.event;

import btrplace.model.Model;
import btrplace.plan.VMEvent;

import java.util.UUID;

/**
 * A event to inform a cloneable VM
 * has been cloned and is now available using a different UUID.
 *
 * @author Fabien Hermenier
 */
public class SubstitutedVMEvent implements VMEvent {

    private UUID oldUUID, newUUID;

    /**
     * Instantiate a new event.
     *
     * @param vm      the old VM UUID
     * @param newUUID the new VM UUID
     */
    public SubstitutedVMEvent(UUID vm, UUID newUUID) {
        oldUUID = vm;
        this.newUUID = newUUID;
    }

    /**
     * Get the old VM identifier.
     *
     * @return a UUID
     */
    @Override
    public UUID getVM() {
        return oldUUID;
    }

    /**
     * Get the new VM identifier.
     *
     * @return a UUID.
     */
    public UUID getNewUUID() {
        return newUUID;
    }

    @Override
    public boolean apply(Model m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object visit(ActionVisitor v) {
        return Boolean.TRUE;
    }

    @Override
    public String toString() {
        return new StringBuilder("substitutedVM(")
                .append("vm=").append(oldUUID)
                .append(", newUUID=").append(newUUID)
                .append(')').toString();
    }

}
