/** rho - доля испарения; Q - количество откладываемого феромона. */
public final class Parameters {
    public final int ants, iterations;
    public final double alpha, beta, rho, q;
    public final long seed;
    public Parameters(int ants, int iterations, double alpha, double beta, double rho, double q, long seed) {
        if (ants < 1 || ants > 500 || iterations < 1 || iterations > 10000
                || !Double.isFinite(alpha) || alpha < 0 || alpha > 10
                || !Double.isFinite(beta) || beta < 0 || beta > 10
                || !Double.isFinite(rho) || rho <= 0 || rho >= 1
                || !Double.isFinite(q) || q <= 0 || q > 10000)
            throw new IllegalArgumentException("Недопустимые параметры");
        this.ants = ants; this.iterations = iterations;
        this.alpha = alpha; this.beta = beta; this.rho = rho; this.q = q; this.seed = seed;
    }
}
