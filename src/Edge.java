/** Одно общее ребро для обоих направлений движения. */
public final class Edge {
    private final Node from, to;
    private final double distance;
    private double pheromone = 1;
    public Edge(Node from, Node to) {
        this.from = from; this.to = to;
        distance = Math.hypot(from.getX() - to.getX(), from.getY() - to.getY());
        if (distance <= 0 || !Double.isFinite(distance))
            throw new IllegalArgumentException("Координаты городов должны различаться");
    }
    public Node getFrom() { return from; }
    public Node getTo() { return to; }
    public double getDistance() { return distance; }
    public double getPheromone() { return pheromone; }
    public void reset() { pheromone = 1; }
    public void evaporate(double rho) { pheromone = Math.max(1e-12, pheromone * (1 - rho)); }
    public void deposit(double amount) { pheromone += amount; }
}
