package btrplace.plan;

import btrplace.model.Model;
import btrplace.plan.event.*;

/**
 * Validate or not each of the actions performed on a
 * {@link ReconfigurationPlan}.
 *
 * @author Fabien Hermenier
 */
public interface ReconfigurationPlanValidator {

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(Allocate a);

    /**
     * Accept or not the given event.
     *
     * @param a the event to check
     * @return {@code true} to accept the event.
     */
    boolean accept(AllocateEvent a);

    /**
     * Accept or not the given event.
     *
     * @param a the event to check
     * @return {@code true} to accept the event.
     */
    boolean accept(SubstitutedVMEvent a);

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(BootNode a);

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(BootVM a);

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(ForgeVM a);

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(KillVM a);

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(MigrateVM a);

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(ResumeVM a);

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(ShutdownNode a);

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(ShutdownVM a);

    /**
     * Accept or not the given action.
     *
     * @param a the action to check
     * @return {@code true} to accept the action.
     */
    boolean accept(SuspendVM a);

    /**
     * Accept or not the given model at the end
     * of the reconfiguration
     *
     * @param mo the model to test
     * @return {@code true} to accept the action.
     */
    boolean accept(Model mo);
}
