package mob_size_diversity.apply;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SizeReachTest {

	@Test
	void extraMultiplierFollowsRolledScaleWhenEnabled() {
		assertEquals(1.3, SizeReach.extraMultiplier(true, 1.3));
		assertEquals(0.8, SizeReach.extraMultiplier(true, 0.8));
	}

	@Test
	void extraMultiplierIsIdentityWhenReachCouplingOff() {
		assertEquals(1.0, SizeReach.extraMultiplier(false, 1.3));
		assertEquals(1.0, SizeReach.extraMultiplier(false, 0.8));
	}
}
