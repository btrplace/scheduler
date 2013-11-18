package btrplace.solver.api.cstrSpec.generator;

import choco.cp.solver.CPSolver;
import choco.kernel.solver.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Fabien Hermenier
 */
public class Foo {

    public static final Properties DEFAULT_CONFIGURATION;

    static {
        DEFAULT_CONFIGURATION = new Properties();
        try {
            DEFAULT_CONFIGURATION.load(new BufferedReader(new FileReader("choco.properties")));
        } catch (Exception e) {
            throw new RuntimeException("Unable to read the solver properties");
        }
    }

    public static void main(String[] args) throws IOException {

        int max = 100000;
        int sum = 0;
        long st = System.currentTimeMillis();
        for (int i = 0; i < max; i++) {
            CPSolver s = new CPSolver();
            sum++;
        }

        System.err.println(System.currentTimeMillis() - st + " " + sum);
        CPSolver s = new CPSolver();
        s.getConfiguration().storeDefault(new File("choco.properties"), "");

        sum = 0;
        st = System.currentTimeMillis();
        for (int i = 0; i < max; i++) {
            Properties props = new Properties(DEFAULT_CONFIGURATION);
            CPSolver x = new CPSolver(new Configuration(props));
            sum++;
        }
        System.err.println(System.currentTimeMillis() - st + " " + sum);
    }
}
