import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Контроллер окна. Модель не зависит от Swing. */
public final class ColonyFrame extends JFrame {
    private final JSpinner cities = spinner(8, 3, 30, 1);
    private final JSpinner ants = spinner(32, 1, 500, 1);
    private final JSpinner iterations = spinner(200, 1, 10000, 10);
    private final JSpinner alpha = spinner(1.5, 0, 10, .1);
    private final JSpinner beta = spinner(2, 0, 10, .1);
    private final JSpinner rho = spinner(.35, .01, .99, .05);
    private final JSpinner q = spinner(1, .01, 10000, .1);
    private final JSpinner seed = spinner(42, 0, 1000000, 1);
    private final JSlider speed = new JSlider(1, 100, 65);
    private final JButton start = new JButton("Старт");
    private final JButton step = new JButton("Один переход");
    private final JButton cycle = new JButton("Один цикл");
    private final JLabel status = new JLabel(" ");
    private final JTextArea statistics = new JTextArea();
    private final GraphPanel canvas = new GraphPanel();
    private final Timer timer;
    private AntColony colony;
    private Tour optimum;
    private double progress = 1;

    public ColonyFrame() {
        super("Лабораторная работа: муравьиный алгоритм | Java ООП");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 720));
        JPanel controls = new JPanel(new GridLayout(0, 2, 8, 8));
        controls.setBorder(new EmptyBorder(16, 16, 12, 16));
        addControl(controls, "Города", cities); addControl(controls, "Муравьи", ants);
        addControl(controls, "Число циклов M", iterations);
        addControl(controls, "α: влияние феромона", alpha); addControl(controls, "β: влияние расстояния", beta);
        addControl(controls, "ρ: доля испарения", rho); addControl(controls, "Q: отложение феромона", q);
        addControl(controls, "Seed эксперимента", seed); addControl(controls, "Скорость", speed);
        JButton reset = new JButton("Сброс / применить");
        JButton newGraph = new JButton("Новый граф");
        controls.add(start); controls.add(step); controls.add(cycle); controls.add(reset);
        controls.add(newGraph);
        JCheckBox showBest = new JCheckBox("Лучший путь", true);
        showBest.addActionListener(e -> { canvas.setShowBest(showBest.isSelected()); });
        controls.add(showBest);
        statistics.setEditable(false); statistics.setLineWrap(true); statistics.setWrapStyleWord(true);
        statistics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        statistics.setBorder(new EmptyBorder(12, 16, 12, 16));
        JPanel sidebar = new JPanel(new BorderLayout()); sidebar.setPreferredSize(new Dimension(350, 650));
        sidebar.add(controls, BorderLayout.NORTH); sidebar.add(new JScrollPane(statistics), BorderLayout.CENTER);
        add(sidebar, BorderLayout.WEST); add(canvas, BorderLayout.CENTER);
        status.setBorder(new EmptyBorder(8, 16, 8, 16)); add(status, BorderLayout.SOUTH);
        timer = new Timer(25, e -> tick());
        start.addActionListener(e -> {
            if (timer.isRunning()) pause();
            else if (!colony.isFinished()) { timer.start(); start.setText("Пауза"); updateButtons(); }
        });
        step.addActionListener(e -> { advance(); progress = 1; canvas.setProgress(1); refresh(); });
        cycle.addActionListener(e -> { colony.runIteration(); canvas.clearAnimation(); refresh(); });
        reset.addActionListener(e -> reset());
        newGraph.addActionListener(e -> {
            seed.setValue((((Number) seed.getValue()).intValue() + 1) % 1000001); reset();
        });
        reset(); setSize(1180, 800); setLocationRelativeTo(null);
    }
    private static JSpinner spinner(double value, double min, double max, double increment) {
        return new JSpinner(new SpinnerNumberModel(value, min, max, increment));
    }
    private void addControl(JPanel panel, String label, JComponent input) {
        panel.add(new JLabel(label)); panel.add(input);
    }
    private double value(JSpinner spinner) { return ((Number) spinner.getValue()).doubleValue(); }
    private void reset() {
        pause();
        try {
            for (JSpinner input : new JSpinner[]{cities, ants, iterations, alpha, beta, rho, q, seed}) input.commitEdit();
            Parameters parameters = new Parameters((int)value(ants), (int)value(iterations), value(alpha), value(beta),
                    value(rho), value(q), (long)value(seed));
            Graph graph = Graph.random((int)value(cities), parameters.seed);
            colony = new AntColony(graph, parameters);
            optimum = graph.size() <= 12 ? ExactSolver.solve(graph) : null;
            canvas.setColony(colony); progress = 1; refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Проверьте параметры: " + ex.getMessage(), "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void pause() { timer.stop(); start.setText("Старт"); if (colony != null) updateButtons(); }
    private void advance() {
        int[] from = new int[colony.getAnts().size()];
        // После завершения цикла муравьи вновь распределяются по стартовым городам.
        for (int i = 0; i < from.length; i++) from[i] = colony.getAnts().get(i).getCurrentCity();
        colony.advance(); canvas.animateFrom(from); progress = 0;
    }
    private void tick() {
        if (progress >= 1) {
            if (colony.isFinished()) { pause(); refresh(); return; }
            advance();
        }
        progress = Math.min(1, progress + .015 + speed.getValue() * .004);
        canvas.setProgress(progress); refresh();
    }
    private void updateButtons() {
        start.setEnabled(!colony.isFinished());
        step.setEnabled(!timer.isRunning() && !colony.isFinished());
        cycle.setEnabled(!timer.isRunning() && !colony.isFinished());
    }
    private void refresh() {
        updateButtons();
        Tour best = colony.getBest();
        String text = "Цикл: " + colony.getIteration() + "\nПереход: " + colony.getStep() + " / " + colony.getGraph().size();
        if (best != null) {
            text += String.format(java.util.Locale.US,
                    "\n\nЛучший за всё время: %.5f\nЛучший в цикле: %.5f\nСредний в цикле: %.5f\nСовпали с рекордом: %d / %d\n\nМаршрут:\n%s",
                    best.getLength(), colony.getIterationBest(), colony.getAverage(), colony.getMatchingBest(), colony.getAnts().size(), best);
        }
        if (optimum != null) {
            text += String.format(java.util.Locale.US, "\n\nТочный оптимум: %.5f", optimum.getLength());
            if (best != null) text += String.format(java.util.Locale.US, "\nОтклонение: %.2f %%",
                    Math.max(0, 100 * (best.getLength() / optimum.getLength() - 1)));
        } else text += "\n\nТочный расчёт доступен до 12 городов.";
        text += "\n\nСиний: феромон (толще = больше).\nЗелёный: лучший найденный тур.\nЦветные точки: муравьи.\nЧисла на рёбрах: расстояния (n ≤ 10).\n\nИзменения параметров применяются кнопкой «Сброс / применить».";
        statistics.setText(text);
        status.setText(colony.isFinished() ? "Завершено M циклов. Для нового запуска нажмите «Сброс / применить»."
                : "Задача коммивояжёра: каждый город один раз и возврат в начало. Длины в условных единицах.");
        canvas.repaint();
    }
}
