import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class GraphTest {
    private static Graph g;

    @BeforeClass
    public static void init() {
        DataProcessing process = new DataProcessing();
        try {
            process.acquireData();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
        final List<List<String>> casts = process.getAllCasts();
        final Set<String> set = new HashSet<>();
        casts.forEach(set::addAll);

        g = new Graph(set);
        casts.forEach(c -> {
            final int l = c.size();
            for (int i = 0; i < l; ++i) {
                for (int j = i + 1; j < l; ++j) {
                    g.addEdge(c.get(i), c.get(j));
                }
            }
        });
    }

    @Test
    public void testGraphDiameter() {
        Util.Pair<Integer, Integer> diameter = g.getDiameter();
        System.out.println(g.getName(diameter.first));
        System.out.println(g.getName(diameter.second));
    }
}