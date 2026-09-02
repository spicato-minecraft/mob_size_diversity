package mob_size_diversity.sampler;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaleSamplerTest {

	private static final int SAMPLES = 40_000;
	private static final int BINS = 20;

	@Test
	void setASamplesStayInsideSupport() {
		assertSupport(ScaleCurve.setA());
	}

	@Test
	void setBSamplesStayInsideSupport() {
		assertSupport(ScaleCurve.setB());
	}

	@Test
	void setCSamplesStayInsideSupport() {
		assertSupport(ScaleCurve.setC());
	}

	@Test
	void boundQZeroDoesNotPileUpOnWalls() {
		assertNoWallSpike(ScaleCurve.setA());
		assertNoWallSpike(ScaleCurve.setB());
		assertNoWallSpike(ScaleCurve.setC());
	}

	@Test
	void negativeSkewPutsMeanLeftOfPeak() {
		ScaleSampler sampler = new ScaleSampler();
		ScaleCurve curve = ScaleCurve.setA();
		Random random = new Random(1L);
		double sum = 0.0;
		for (int i = 0; i < SAMPLES; i++) {
			sum += sampler.sample(curve, random);
		}
		assertTrue(sum / SAMPLES < curve.peak(), "left skew should pull the mean below the peak");
	}

	@Test
	void densityIsZeroOutsideSupport() {
		ScaleSampler sampler = new ScaleSampler();
		ScaleCurve curve = ScaleCurve.setA();
		assertEquals(0.0, sampler.unnormalizedDensity(curve, curve.min() - 1e-6));
		assertEquals(0.0, sampler.unnormalizedDensity(curve, curve.max() + 1e-6));
		assertTrue(sampler.unnormalizedDensity(curve, curve.peak()) > 0.0);
	}

	private static void assertSupport(ScaleCurve curve) {
		ScaleSampler sampler = new ScaleSampler();
		Random random = new Random(3L);
		for (int i = 0; i < SAMPLES; i++) {
			double x = sampler.sample(curve, random);
			assertTrue(x >= curve.min() && x <= curve.max(), () -> "sample " + x + " outside " + curve);
		}
	}

	private static void assertNoWallSpike(ScaleCurve curve) {
		ScaleSampler sampler = new ScaleSampler();
		Random random = new Random(4L);
		int[] counts = new int[BINS];
		double width = (curve.max() - curve.min()) / BINS;
		for (int i = 0; i < SAMPLES; i++) {
			double x = sampler.sample(curve, random);
			int bin = Math.min(BINS - 1, (int) ((x - curve.min()) / width));
			counts[bin]++;
		}
		assertTrue(counts[0] <= counts[1] * 3, "min wall spike: " + counts[0] + " vs neighbor " + counts[1]);
		assertTrue(
				counts[BINS - 1] <= counts[BINS - 2] * 3,
				"max wall spike: " + counts[BINS - 1] + " vs neighbor " + counts[BINS - 2]
		);
	}
}
