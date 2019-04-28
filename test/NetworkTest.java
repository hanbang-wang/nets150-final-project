import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Test
    public void testGraphDiameter() {
        Util.Pair<Integer, Integer> diameter = g.getDiameter();
        System.out.println(g.getName(diameter.first));
        System.out.println(g.getName(diameter.second));
    }
}