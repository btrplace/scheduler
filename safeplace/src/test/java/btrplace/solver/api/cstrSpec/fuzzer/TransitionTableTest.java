package btrplace.solver.api.cstrSpec.fuzzer;

import org.testng.annotations.Test;

import java.io.FileReader;

/**
 * @author Fabien Hermenier
 */
public class TransitionTableTest {

    @Test
    public void test() throws Exception {
        String file = "btrplace.solver.api.cstrSpec.fuzzer/vm_transitions";
        TransitionTable trans = new TransitionTable(new FileReader(file));
        System.out.println(trans);
    }
}
