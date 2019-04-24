import java.util.*;

final public class Graph {
    private int size = 0;
    private final Map<String, Integer> nameToId = new HashMap<>();
    private final String[] idToName;
    private final Set<Integer>[] links;

    Graph(Set<String> nodes) {
        nodes.forEach(n -> nameToId.put(n, size++));
        idToName = new String[size];
        links = new HashSet[size];
        nameToId.forEach((n, i) -> idToName[i] = n);
    }

    void addEdge(String u, String v) {
        if (!nameToId.containsKey(u) || !nameToId.containsKey(v)) {
            throw new NoSuchElementException();
        }
        final int uId = nameToId.get(u);
        final int vId = nameToId.get(v);
        links[uId].add(vId);
        links[vId].add(uId);
    }

    int getId(String u) {
        if (!nameToId.containsKey(u)) {
            throw new NoSuchElementException();
        }
        return nameToId.get(u);
    }

    List<Integer> BFS(int u, int v) {
        final int incoming[] = new int[size];
        Arrays.fill(incoming, -1);
        Queue<Integer> q = new LinkedList<>();
        q.add(u);
        incoming[u] = u;
        while (!q.isEmpty()) {
            final int front = q.remove();
            links[front].stream().filter(e -> incoming[e] != -1).forEach(e -> {
                incoming[e] = front;
                q.add(e);
            });
        }
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
}
