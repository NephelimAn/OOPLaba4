import java.util.Arrays;

/** Замкнутый маршрут: последняя вершина повторяет первую. */
public final class Tour {
    private final int[] cities;
    private final double length;
    public Tour(Graph graph, int[] cities) {
        if (cities.length != graph.size() + 1 || cities[0] != cities[cities.length - 1])
            throw new IllegalArgumentException("Маршрут должен быть замкнутым");
        boolean[] visited = new boolean[graph.size()];
        for (int i = 0; i < graph.size(); i++) {
            int city = cities[i];
            if (city < 0 || city >= graph.size() || visited[city])
                throw new IllegalArgumentException("Каждый город посещается один раз");
            visited[city] = true;
        }
        double sum = 0;
        for (int i = 1; i < cities.length; i++) sum += graph.edge(cities[i - 1], cities[i]).getDistance();
        this.cities = cities.clone(); length = sum;
    }
    public int[] getCities() { return cities.clone(); }
    public double getLength() { return length; }
    public boolean contains(Edge edge) {
        int a = edge.getFrom().getId(), b = edge.getTo().getId();
        for (int i = 1; i < cities.length; i++)
            if ((cities[i-1] == a && cities[i] == b) || (cities[i-1] == b && cities[i] == a)) return true;
        return false;
    }
    @Override public String toString() {
        return Arrays.toString(Arrays.stream(cities).map(i -> i + 1).toArray());
    }
}
