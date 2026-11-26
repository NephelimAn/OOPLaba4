import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;

/** Визуализация читает модель и не влияет на выбор маршрутов. */
public final class GraphPanel extends JPanel {
    private AntColony colony;
    private boolean showBest = true;
    private int[] from;
    private double progress = 1;
    public GraphPanel() { setBackground(new Color(247, 249, 253)); }
    public void setColony(AntColony colony) { this.colony = colony; clearAnimation(); }
    public void setShowBest(boolean show) { showBest = show; repaint(); }
    public void animateFrom(int[] from) { this.from = from.clone(); }
    public void setProgress(double progress) { this.progress = progress; repaint(); }
    public void clearAnimation() { from = null; progress = 1; repaint(); }
    private int x(Node node) { return 35 + (int)(node.getX() * (getWidth()-70)); }
    private int y(Node node) { return 55 + (int)(node.getY() * (getHeight()-250)); }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (colony == null) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
        g.setColor(new Color(30, 44, 70)); g.drawString("Муравьиная колония", 25, 30);
        Graph graph = colony.getGraph();
        double max = graph.getEdges().stream().mapToDouble(Edge::getPheromone).max().orElse(1);
        for (Edge edge : graph.getEdges()) {
            double strength = edge.getPheromone() / max;
            g.setColor(new Color(55, 110, 210, 25 + (int)(150 * strength)));
            g.setStroke(new BasicStroke((float)(.5 + 4 * strength)));
            g.draw(new Line2D.Double(x(edge.getFrom()), y(edge.getFrom()), x(edge.getTo()), y(edge.getTo())));
        }
        if (showBest && colony.getBest() != null) {
            g.setColor(new Color(25, 160, 95)); g.setStroke(new BasicStroke(3));
            int[] route = colony.getBest().getCities();
            for (int i = 1; i < route.length; i++) {
                Node a = graph.getNodes().get(route[i-1]), b = graph.getNodes().get(route[i]);
                g.drawLine(x(a), y(a), x(b), y(b));
            }
        }
        if (graph.size() <= 10) {
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            for (Edge edge : graph.getEdges()) {
                int mx = (x(edge.getFrom()) + x(edge.getTo())) / 2;
                int my = (y(edge.getFrom()) + y(edge.getTo())) / 2;
                String label = String.format(java.util.Locale.US, "%.2f", edge.getDistance());
                g.setColor(new Color(247, 249, 253, 220)); g.fillRect(mx-13, my-9, 28, 12);
                g.setColor(new Color(80, 95, 120)); g.drawString(label, mx-12, my);
            }
        }
        for (Node node : graph.getNodes()) {
            g.setColor(new Color(30, 44, 70)); g.fillOval(x(node)-13, y(node)-13, 26, 26);
            g.setColor(Color.WHITE); g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            String label = String.valueOf(node.getId()+1);
            g.drawString(label, x(node)-g.getFontMetrics().stringWidth(label)/2, y(node)+4);
        }
        for (int i = 0; i < colony.getAnts().size(); i++) {
            Node target = graph.getNodes().get(colony.getAnts().get(i).getCurrentCity());
            Node source = from == null ? target : graph.getNodes().get(from[i]);
            double angle = 2 * Math.PI * i / colony.getAnts().size();
            int ax = (int)(x(source) + progress * (x(target)-x(source)) + 20*Math.cos(angle));
            int ay = (int)(y(source) + progress * (y(target)-y(source)) + 20*Math.sin(angle));
            g.setColor(Color.getHSBColor(i / (float)colony.getAnts().size(), .8f, .85f));
            g.fillOval(ax-4, ay-4, 8, 8); g.setColor(Color.WHITE); g.setStroke(new BasicStroke(1));
            g.drawOval(ax-4, ay-4, 8, 8);
        }
        drawHistory(g); g.dispose();
    }
    private void drawHistory(Graphics2D g) {
        int left = 55, top = getHeight()-145, width = getWidth()-90, height = 95;
        g.setColor(new Color(80, 95, 120)); g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.drawString("Сходимость: лучший маршрут по циклам", left, top-12);
        g.setStroke(new BasicStroke(1)); g.drawRect(left, top, width, height);
        List<Double> history = colony.getHistory();
        if (history.isEmpty()) { g.drawString("Ожидание первого завершённого цикла", left+12, top+35); return; }
        double min = history.stream().mapToDouble(Double::doubleValue).min().getAsDouble();
        double max = history.get(0), range = Math.max(.01, max-min);
        g.drawString(String.format(java.util.Locale.US, "%.3f", max), 5, top+12);
        g.drawString(String.format(java.util.Locale.US, "%.3f", min), 5, top+height);
        g.setColor(new Color(25, 160, 95)); g.setStroke(new BasicStroke(2));
        for (int i = 0; i < history.size(); i++) {
            int px = left + i*width/Math.max(1, history.size()-1);
            int py = top+8 + (int)((max-history.get(i))/range * (height-16));
            if (i == 0) g.fillOval(px-3, py-3, 6, 6);
            else {
                int previousY = top+8+(int)((max-history.get(i-1))/range*(height-16));
                g.drawLine(left+(i-1)*width/Math.max(1, history.size()-1), previousY, px, py);
            }
        }
        g.setColor(new Color(80, 95, 120)); g.drawString("1", left, top+height+18);
        g.drawString(String.valueOf(history.size()), left+width-20, top+height+18);
    }
}
