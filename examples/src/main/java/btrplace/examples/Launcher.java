package btrplace.examples;

/**
 * CLI app to launch tutorials.
 *
 * @author Fabien Hermenier
 */
public final class Launcher {

    /**
     * CLI. No instantiation.
     */
    private Launcher() {
    }

    /**
     * CLI. First value of the array should be the name of a class inheriting from {@link btrplace.examples.Example}.
     *
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            System.err.println("Expecting a class that inherit from btrplace.examples.Example as a parameter");
            System.exit(1);
        }
        try {
            Class c = Class.forName(args[0]);
            boolean validInterface = false;
            for (Class i : c.getInterfaces()) {
                if (i.equals(Example.class)) {
                    validInterface = true;
                    break;
                }
            }
            if (!validInterface) {
                System.err.println("The class must implement '" + Example.class.getName() + "'");
                System.exit(1);
            }
            Example ex = (Example) c.newInstance();
            boolean ret = ex.run();
            if (!ret) {
                System.err.println("The example '" + ex.getClass().getSimpleName() + "' failed");
                System.exit(1);
            }
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (InstantiationException e) {
            System.err.println("Unable to instantiate " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
