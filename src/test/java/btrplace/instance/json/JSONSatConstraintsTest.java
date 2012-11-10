package btrplace.instance.json;

import btrplace.instance.constraint.*;
import junit.framework.Assert;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Unit tests for {@link JSONSatConstraints}.
 *
 * @author Fabien Hermenier
 */
public class JSONSatConstraintsTest {

    private static Random rnd = new Random();

    private static Set<UUID> randomSet() {
        Set<UUID> s = new HashSet<UUID>();
        for (int i = 0; i < rnd.nextInt(5); i++) {
            s.add(UUID.randomUUID());
        }
        return s;
    }

    @Test
    public void testRunning() {
        Running s = new Running(randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.runningToJSON(s);
        Running s2 = x.runningFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testSleeping() {
        Sleeping s = new Sleeping(randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.sleepingToJSON(s);
        Sleeping s2 = x.sleepingFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testWaiting() {
        Waiting s = new Waiting(randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.waitingToJSON(s);
        Waiting s2 = x.waitingFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testDestroyed() {
        Destroyed s = new Destroyed(randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.destroyedToJSON(s);
        Destroyed s2 = x.destroyedFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testOnline() {
        Online s = new Online(randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.onlineToJSON(s);
        Online s2 = x.onlineFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testOffline() {
        Offline s = new Offline(randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.offlineToJSON(s);
        Offline s2 = x.offlineFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testBan() {
        Ban s = new Ban(randomSet(), randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.banToJSON(s);
        Ban s2 = x.banFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testFence() {
        Fence s = new Fence(randomSet(), randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.fenceToJSON(s);
        Fence s2 = x.fenceFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testRoot() {
        Root s = new Root(randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.rootToJSON(s);
        Root s2 = x.rootFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testPreserve() {
        Preserve s = new Preserve(randomSet(), "foo", 7);
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.preserveToJSON(s);
        Preserve s2 = x.preserveFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testSplitAmong() {
        Set<Set<UUID>> ss = new HashSet<Set<UUID>>();
        ss.add(randomSet());
        ss.add(randomSet());
        Set<Set<UUID>> ss2 = new HashSet<Set<UUID>>();
        ss2.add(randomSet());
        ss2.add(randomSet());
        ss2.add(randomSet());

        SplitAmong s = new SplitAmong(ss, ss2);
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.splitAmongToJSON(s);
        SplitAmong s2 = x.splitAmongFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testSplit() {
        Set<Set<UUID>> ss2 = new HashSet<Set<UUID>>();
        ss2.add(randomSet());
        ss2.add(randomSet());
        ss2.add(randomSet());
        Split s = new Split(ss2);
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.splitToJSON(s);
        Split s2 = x.splitFromJSON(o);
        Assert.assertEquals(s, s2);
    }

    @Test
    public void testSpread() {
        Spread s = new Spread(randomSet());
        JSONSatConstraints x = new JSONSatConstraints();
        JSONObject o = x.spreadToJSON(s);
        Spread s2 = x.spreadFromJSON(o);
        Assert.assertEquals(s, s2);
    }

}
