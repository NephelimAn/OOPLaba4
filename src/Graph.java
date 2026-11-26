import java.util.*;

/** Полный неориентированный взвешенный граф. */
public final class Graph {
    private final List<Node> nodes;
    private final List<Edge> edges = new ArrayList<>();
    private final Edge[][] adjacency;
    public Graph(List<Node> nodes) {
        if (nodes == null || nodes.size() < 3) throw new IllegalArgumentException("Нужно минимум 3 города");
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        adjacency = new Edge[nodes.size()][nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getId() != i) throw new IllegalArgumentException("Номера городов: 0, 1, ...");
            for (int j = 0; j < i; j++) {
                Edge edge = new Edge(nodes.get(i), nodes.get(j));
                adjacency[i][j] = adjacency[j][i] = edge;
                edges.add(edge);
            }
        }
    }
    public static Graph random(int count, long seed) {
        if (count < 3 || count > 30) throw new IllegalArgumentException("Число городов: 3..30");
        Random random = new Random(seed);
        List<Node> nodes = new ArrayList<>();
        while (nodes.size() < count) {
            double x = .06 + .88 * random.nextDouble(), y = .06 + .88 * random.nextDouble();
            boolean free = true;
            for (Node node : nodes)
                if (Math.hypot(x - node.getX(), y - node.getY()) < .065) free = false;
            if (free) nodes.add(new Node(nodes.size(), x, y));
        }
        return new Graph(nodes);
    }
    public int size() { return nodes.size(); }
    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return Collections.unmodifiableList(edges); }
    public Edge edge(int a, int b) { return adjacency[a][b]; }
    public void resetPheromone() { for (Edge edge : edges) edge.reset(); }
}
