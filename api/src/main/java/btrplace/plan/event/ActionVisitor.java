package btrplace.plan.event;

/**
 * A visitor compatible will all actions supported by btrplace.
 *
 * @author Fabien Hermenier
 */
public interface ActionVisitor {

    /**
     * Visit an {@link Allocate} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(Allocate a);

    /**
     * Visit an {@link AllocateEvent} event.
     *
     * @param e the event to visit
     * @return a possible value
     */
    Object visit(AllocateEvent a);

    /**
     * Visit an {@link BootNode} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(BootNode a);

    /**
     * Visit an {@link BootVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(BootVM a);

    /**
     * Visit an {@link ForgeVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(ForgeVM a);

    /**
     * Visit an {@link KillVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(KillVM a);

    /**
     * Visit an {@link MigrateVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(MigrateVM a);

    /**
     * Visit an {@link ResumeVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(ResumeVM a);

    /**
     * Visit an {@link ShutdownNode} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(ShutdownNode a);

    /**
     * Visit an {@link ShutdownVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(ShutdownVM a);

    /**
     * Visit an {@link SuspendVM} action.
     *
     * @param a the action to visit
     * @return a possible value
     */
    Object visit(SuspendVM a);
}
