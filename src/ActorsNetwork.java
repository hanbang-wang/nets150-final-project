import java.util.*;

final public class ActorsNetwork {
    private int size = 0;
    private final Map<String, Integer> nameToId = new HashMap<>();
    private final String[] idToName;
    private final List<Integer>[] links;

    ActorsNetwork(Set<String> nodes) {
        nodes.forEach(n -> nameToId.put(n, size++));
        idToName = new String[size];
        links = new List[size];
        for (int i = 0; i < size; ++i) {
            links[i] = new LinkedList<>();
        }
        nameToId.forEach((n, i) -> idToName[i] = n);
    }

    boolean exists(String k) {
        return nameToId.containsKey(k);
    }

    private void ensureValid(int u) {
        if (u < 0 || u >= size) {
            throw new IllegalArgumentException();
        }
    }

    private void ensureValid(int... u) {
        Arrays.stream(u).forEach(this::ensureValid);
    }

    void addEdge(String u, String v) {
        if (!exists(u) || !exists(v)) {
            throw new NoSuchElementException();
        }
        final int uId = nameToId.get(u);
        final int vId = nameToId.get(v);
        links[uId].add(vId);
        links[vId].add(uId);
    }

    int getId(String u) {
        if (!exists(u)) {
            throw new NoSuchElementException();
        }
        return nameToId.get(u);
    }

    String getName(int u) {
        ensureValid(u);
        return idToName[u];
    }

    int getDegree(int u) {
        return links[u].size();
    }

    int getSize() {
        return size;
    }

    private int[] breadthFirstSearchWithLength(int u) {
        final int[] distance = new int[size];
        Arrays.fill(distance, -1);
        Queue<Integer> q = new LinkedList<>();
        q.add(u);
        distance[u] = 0;
        while (!q.isEmpty()) {
            final int front = q.remove();
            links[front].stream().filter(e -> distance[e] == -1).forEach(e -> {
                distance[e] = distance[front] + 1;
                q.add(e);
            });
        }
        return distance;
    }

    private int[] breadthFirstSearchWithIncoming(int u, int v) {
        final int[] incoming = new int[size];
        Arrays.fill(incoming, -1);
        Queue<Integer> q = new LinkedList<>();
        q.add(u);
        incoming[u] = u;
        while (!q.isEmpty() && incoming[v] == -1) {
            final int front = q.remove();
            Collections.shuffle(links[front]);
            links[front].stream().filter(e -> incoming[e] == -1).forEach(e -> {
                incoming[e] = front;
                q.add(e);
            });
        }
        return incoming;
    }

    List<Integer> shortestPath(int u, int v) {
        ensureValid(u, v);
        final int[] incoming = breadthFirstSearchWithIncoming(u, v);
        if (incoming[v] == -1) {
            return Collections.emptyList();
        }
        LinkedList<Integer> ret = new LinkedList<>();
        ret.addFirst(v);
        int cur = v;
        while (cur != incoming[cur]) {
            cur = incoming[cur];
            ret.addFirst(cur);
        }
        return ret;
    }

    private int getRandomMaximum(int[] l) {
        int max = 0;
        List<Integer> ret = new LinkedList<>();
        for (int i = 0; i < size; ++i) {
            if (l[i] >= max) {
                if (l[i] != max) {
                    ret.clear();
                    max = l[i];
                }
                ret.add(i);
            }
        }
        return ret.get((int) (Math.random() * ret.size()));
    }

    Util.Pair<Integer, Integer> getDiameter() {
        final int start = (int) (Math.random() * size);
        int[] distances = breadthFirstSearchWithLength(start);
        final int oneEnd = getRandomMaximum(distances);
        distances = breadthFirstSearchWithLength(oneEnd);
        return new Util.Pair<>(oneEnd, getRandomMaximum(distances));
    }

    List<Integer> getNeighbors(int u) {
        return links[u];
    }
}