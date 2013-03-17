package btrplace.examples;

/**
 * An interface to define a runnable example.
 *
 * @author Fabien Hermenier
 */
public interface Example {

    /**
     * Run the example.
     *
     * @return {@code true} iff the example was executed successfully.
     * @throws Exception if an error occurred
     */
    boolean run() throws Exception;
}
