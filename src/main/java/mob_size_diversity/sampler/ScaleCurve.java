package mob_size_diversity.sampler;

/**
 * Density parameters for one rule. Not a hostility classifier — callers attach
 * these numbers to entity/tag rules.
 *
 * @param peak mode of the peak term (product “mean”)
 * @param skew in {@code [-1, +1]}; negative is left-skewed
 * @param min inclusive support lower bound
 * @param max inclusive support upper bound
 * @param centerQ width of the peak term; higher = fatter bell and longer tails
 * @param minBoundQ {@code 0} = no extra min envelope (brick-wall via support only)
 * @param maxBoundQ {@code 0} = no extra max envelope
 */
public record ScaleCurve(
		double peak,
		double skew,
		double min,
		double max,
		double centerQ,
		double minBoundQ,
		double maxBoundQ
) {
	public ScaleCurve {
		if (!(min < max)) {
			throw new IllegalArgumentException("min must be < max");
		}
		if (peak < min || peak > max) {
			throw new IllegalArgumentException("peak must lie in [min, max]");
		}
		if (skew < -1.0 || skew > 1.0) {
			throw new IllegalArgumentException("skew must lie in [-1, 1]");
		}
		if (!(centerQ > 0.0)) {
			throw new IllegalArgumentException("centerQ must be > 0");
		}
		if (minBoundQ < 0.0 || maxBoundQ < 0.0) {
			throw new IllegalArgumentException("bound Qs must be >= 0");
		}
	}

	/** Shipped Set A numbers (hostiles / piglins in default JSON). */
	public static ScaleCurve setA() {
		return new ScaleCurve(1.1, -0.5, 0.95, 1.25, 0.08, 0.0, 0.0);
	}

	/** Shipped Set B numbers (elite tag rule in default JSON). */
	public static ScaleCurve setB() {
		return new ScaleCurve(1.4, -0.2, 1.35, 1.55, 0.08, 0.0, 0.0);
	}

	/** Shipped Set C numbers (passive / other living mobs in default JSON). */
	public static ScaleCurve setC() {
		return new ScaleCurve(1.1, 0.0, 0.95, 1.15, 0.08, 0.0, 0.0);
	}
}
