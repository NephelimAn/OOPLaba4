import java.util.*;

/** Ant System: обновление феромона происходит после завершения ВСЕХ маршрутов. */
public final class AntColony {
    private final Graph graph;
    private final Parameters parameters;
    private final Random random;
    private final List<Ant> ants = new ArrayList<>();
    private final List<Double> history = new ArrayList<>();
    private Tour best;
    private double iterationBest, average;
    private int matchingBest, iteration, step;
    public AntColony(Graph graph, Parameters parameters) {
        this.graph = graph; this.parameters = parameters; random = new Random(parameters.seed);
        graph.resetPheromone(); startAnts();
    }
    private void startAnts() {
        ants.clear();
        for (int i = 0; i < parameters.ants; i++)
            ants.add(new Ant(graph, parameters, random, i % graph.size()));
    }
    /** Один переход каждого муравья, включая возврат в стартовый город. */
    public void advance() {
        if (isFinished()) return;
        if (step == graph.size()) { startAnts(); step = 0; }
        for (Ant ant : ants) ant.move();
        step++;
        if (step == graph.size()) finishIteration();
    }
    public void runIteration() {
        if (isFinished()) return;
        int target = iteration + 1;
        while (iteration < target) advance();
    }
    private void finishIteration() {
        List<Tour> tours = new ArrayList<>();
        iterationBest = Double.POSITIVE_INFINITY; average = 0;
        for (Ant ant : ants) {
            Tour tour = ant.getTour(); tours.add(tour); average += tour.getLength();
            iterationBest = Math.min(iterationBest, tour.getLength());
            if (best == null || tour.getLength() < best.getLength()) best = tour;
        }
        average /= ants.size(); matchingBest = 0;
        for (Tour tour : tours) if (Math.abs(tour.getLength() - best.getLength()) < 1e-9) matchingBest++;
        for (Edge edge : graph.getEdges()) edge.evaporate(parameters.rho);
        for (Tour tour : tours) {
            int[] cities = tour.getCities();
            for (int i = 1; i < cities.length; i++)
                graph.edge(cities[i-1], cities[i]).deposit(parameters.q / tour.getLength());
        }
        iteration++; history.add(best.getLength());
    }
    public Graph getGraph() { return graph; }
    public List<Ant> getAnts() { return Collections.unmodifiableList(ants); }
    public List<Double> getHistory() { return Collections.unmodifiableList(history); }
    public Tour getBest() { return best; }
    public int getIteration() { return iteration; }
    public int getStep() { return step; }
    public double getIterationBest() { return iterationBest; }
    public double getAverage() { return average; }
    public int getMatchingBest() { return matchingBest; }
    public boolean isFinished() { return iteration >= parameters.iterations; }
}
