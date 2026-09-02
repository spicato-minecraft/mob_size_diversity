package mob_size_diversity.sampler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

/**
 * Compose an unnormalized density, renormalize, sample. Does not clamp — support
 * is {@code [min, max]} with density 0 outside (bound Q = 0 brick-wall).
 */
public final class ScaleSampler {

	static final int BINS = 2048;

	private final ConcurrentHashMap<ScaleCurve, double[]> cdfCache = new ConcurrentHashMap<>();

	public double sample(ScaleCurve curve, RandomGenerator random) {
		return sample(curve, random.nextDouble());
	}

	public double sample(ScaleCurve curve, double uniform01) {
		double u = uniform01;
		if (u < 0.0 || u >= 1.0) {
			u = Math.clamp(u, 0.0, 0.999999999999);
		}
		double[] cdf = cdfCache.computeIfAbsent(curve, this::buildCdf);
		int lo = 0;
		int hi = BINS - 1;
		while (lo < hi) {
			int mid = (lo + hi) >>> 1;
			if (cdf[mid] < u) {
				lo = mid + 1;
			} else {
				hi = mid;
			}
		}
		return binCenter(curve, lo);
	}

	double unnormalizedDensity(ScaleCurve curve, double x) {
		if (x < curve.min() || x > curve.max()) {
			return 0.0;
		}
		double s = curve.centerQ();
		double z = (x - curve.peak()) / s;
		double peakTerm = Math.exp(-0.5 * z * z) * standardNormalCdf(curve.skew() * z);
		return peakTerm * minEnvelope(curve, x) * maxEnvelope(curve, x);
	}

	private double[] buildCdf(ScaleCurve curve) {
		double[] density = new double[BINS];
		double sum = 0.0;
		for (int i = 0; i < BINS; i++) {
			double x = binCenter(curve, i);
			double d = unnormalizedDensity(curve, x);
			density[i] = d;
			sum += d;
		}
		if (!(sum > 0.0)) {
			throw new IllegalStateException("density integrated to zero for " + curve);
		}
		double[] cdf = new double[BINS];
		double acc = 0.0;
		for (int i = 0; i < BINS; i++) {
			acc += density[i] / sum;
			cdf[i] = acc;
		}
		cdf[BINS - 1] = 1.0;
		return cdf;
	}

	private static double binCenter(ScaleCurve curve, int i) {
		double width = (curve.max() - curve.min()) / BINS;
		return curve.min() + (i + 0.5) * width;
	}

	private static double minEnvelope(ScaleCurve curve, double x) {
		if (curve.minBoundQ() == 0.0 || x >= curve.peak()) {
			return 1.0;
		}
		double span = curve.peak() - curve.min();
		if (span <= 0.0) {
			return 1.0;
		}
		double t = (x - curve.min()) / span;
		return Math.pow(Math.max(0.0, t), curve.minBoundQ());
	}

	private static double maxEnvelope(ScaleCurve curve, double x) {
		if (curve.maxBoundQ() == 0.0 || x <= curve.peak()) {
			return 1.0;
		}
		double span = curve.max() - curve.peak();
		if (span <= 0.0) {
			return 1.0;
		}
		double t = (curve.max() - x) / span;
		return Math.pow(Math.max(0.0, t), curve.maxBoundQ());
	}

	private static double standardNormalCdf(double x) {
		return 0.5 * (1.0 + erf(x / Math.sqrt(2.0)));
	}

	/**
	 * Abramowitz and Stegun 7.1.26.
	 */
	private static double erf(double z) {
		double sign = Math.signum(z);
		double x = Math.abs(z);
		double t = 1.0 / (1.0 + 0.3275911 * x);
		double y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t - 0.284496736) * t + 0.254829592)
				* t
				* Math.exp(-x * x);
		return sign * y;
	}
}
