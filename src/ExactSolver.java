import java.util.Arrays;

/** Точный алгоритм Held-Karp для проверки на графах до 12 городов. */
public final class ExactSolver {
    private ExactSolver() { }
    public static Tour solve(Graph graph) {
        int n = graph.size();
        if (n > 12) throw new IllegalArgumentException("Точный расчёт: максимум 12 городов");
        int states = 1 << n;
        double[][] dp = new double[states][n];
        int[][] parent = new int[states][n];
        for (double[] row : dp) Arrays.fill(row, Double.POSITIVE_INFINITY);
        dp[1][0] = 0;
        for (int mask = 1; mask < states; mask += 2)
            for (int last = 0; last < n; last++) {
                if (!Double.isFinite(dp[mask][last])) continue;
                for (int next = 1; next < n; next++) {
                    if ((mask & (1 << next)) != 0) continue;
                    int newMask = mask | (1 << next);
                    double value = dp[mask][last] + graph.edge(last, next).getDistance();
                    if (value < dp[newMask][next]) {
                        dp[newMask][next] = value; parent[newMask][next] = last;
                    }
                }
            }
        int last = 1;
        for (int i = 2; i < n; i++)
            if (dp[states-1][i] + graph.edge(i, 0).getDistance()
                    < dp[states-1][last] + graph.edge(last, 0).getDistance()) last = i;
        int mask = states-1;
        int[] route = new int[n+1];
        for (int position = n-1; position > 0; position--) {
            route[position] = last;
            int previous = parent[mask][last]; mask ^= 1 << last; last = previous;
        }
        return new Tour(graph, route);
    }
}
