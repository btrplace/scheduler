package btrplace.bench;

import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by vkherbac on 16/09/14.
 */
public class LauncherTest {

    @Test
    public void test() throws IOException {

        Launcher.main(new String[] {
                "--repair",
                "--timeout", "500",
                "-i", "src/test/resources/wkld-tdsc/nr/r3/p5000/c66/2.gz",
                "-o", "src/test/resources/nr-r3-p5000-c66-2.csv"
        });
        System.err.flush();
        System.out.flush();
    }
}
