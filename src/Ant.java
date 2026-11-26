import java.util.Arrays;
import java.util.Random;

/** Муравей хранит посещённые города и выбирает следующий переход методом рулетки. */
public final class Ant {
    private final Graph graph;
    private final Parameters parameters;
    private final Random random;
    private final boolean[] visited;
    private final int[] route;
    private int position;
    public Ant(Graph graph, Parameters parameters, Random random, int start) {
        this.graph = graph; this.parameters = parameters; this.random = random;
        visited = new boolean[graph.size()]; route = new int[graph.size()+1];
        route[0] = start; visited[start] = true;
    }
    public boolean isFinished() { return position == graph.size(); }
    public int getCurrentCity() { return route[position]; }
    public int[] getRoute() { return Arrays.copyOf(route, position+1); }
    public void move() {
        if (isFinished()) return;
        int next = position == graph.size()-1 ? route[0] : chooseNextCity();
        route[++position] = next; visited[next] = true;
    }
    public int chooseNextCity() {
        // Вычисление в логарифмах предотвращает переполнение tau^alpha * eta^beta.
        double[] weights = new double[graph.size()];
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < weights.length; i++) {
            if (visited[i]) continue;
            Edge edge = graph.edge(getCurrentCity(), i);
            weights[i] = parameters.alpha * Math.log(edge.getPheromone())
                    - parameters.beta * Math.log(edge.getDistance());
            max = Math.max(max, weights[i]);
        }
        if (!Double.isFinite(max)) throw new IllegalStateException("Нет непосещённых городов");
        double sum = 0; int last = -1;
        for (int i = 0; i < weights.length; i++) if (!visited[i]) {
            weights[i] = Math.exp(weights[i] - max); sum += weights[i]; last = i;
        }
        double choice = random.nextDouble() * sum;
        for (int i = 0; i < weights.length; i++) if (!visited[i]) {
            choice -= weights[i]; if (choice < 0) return i;
        }
        return last;
    }
    public Tour getTour() {
        if (!isFinished()) throw new IllegalStateException("Маршрут ещё не завершён");
        return new Tour(graph, route);
    }
}
