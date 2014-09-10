package btrplace.solver.api.cstrSpec.util;

/**
 * @author Fabien Hermenier
 */
public class Maths {

    public static long C(int n, int k) {
        return facto(n) / (facto(k) * facto(n - k));
    }

    public static long facto(int n) {
        long r = 1;
        while (n > 1) {
            r *= n--;
        }
        return r;
    }
}
