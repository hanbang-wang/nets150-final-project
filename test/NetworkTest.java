import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class NetworkTest {
    private static ActorsNetwork g;

    @BeforeClass
    public static void init() {
        DataProcessing process = new DataProcessing();
        try {
            process.acquireData();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        final List<String[]> casts = process.getAllCasts();
        final Set<String> set = new HashSet<>();
        casts.forEach(l -> set.addAll(Arrays.asList(l)));

        g = new ActorsNetwork(set);
        casts.forEach(c -> {
            for (int i = 0; i < c.length; ++i) {
                for (int j = i + 1; j < c.length; ++j) {
                    g.addEdge(c[i], c[j]);
                }
            }
        });
        System.out.println(g.getSize());
    }

//    @Test
//    public void testGraphDiameter() {
//        Util.Pair<Integer, Integer> diameter = g.getDiameter();
//        System.out.println(g.getName(diameter.first));
//        System.out.println(g.getName(diameter.second));
//    }

    @Test
    public void statAnalysis() {
        System.out.println("Average degree: " + g.averageDegree());
        System.out.println("Degree standard deviation: " + g.graphStDev());
        Set<String> toLookup = new HashSet<>();
        toLookup.add("Ben Affleck");
        toLookup.add("Matt Damon");
        toLookup.add("Rainn Wilson");
        toLookup.add("Terry Crews");
        toLookup.add("Ermina Zaenah");
        for (String actor : toLookup) {
            System.out.println(actor + " is " + g.getActorStDev(g.getId(actor)) +
                    " deviations from mean");
        }
    }

    @Test
    public void printSortedStDevs() {
        System.out.println("Average degree: " + g.averageDegree());
        System.out.println("Degree standard deviation: " + g.graphStDev());

        Iterator<Map.Entry<Double, Integer>> iter =
                g.actorStDevs().entrySet().iterator();
        double prev = -1000;

        while (iter.hasNext()) {
            Map.Entry<Double, Integer> actor = iter.next();
            System.out.println(g.getName(actor.getValue()) + " is " +
                    actor.getKey() + " deviations from mean with degree " +
                    g.getDegree(actor.getValue()));
            if (actor.getKey() < prev) {
                throw new RuntimeException("Current actor has stdev " +
                        actor.getKey() + " but previous had " + prev);
            }
            prev = actor.getKey();
        }
    }
}