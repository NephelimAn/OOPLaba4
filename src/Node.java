/** Неизменяемый город. Координаты задают евклидовы расстояния. */
public final class Node {
    private final int id;
    private final double x, y;
    public Node(int id, double x, double y) {
        if (id < 0 || !Double.isFinite(x) || !Double.isFinite(y))
            throw new IllegalArgumentException("Некорректный город");
        this.id = id; this.x = x; this.y = y;
    }
    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
}
