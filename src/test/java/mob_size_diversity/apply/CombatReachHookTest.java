package mob_size_diversity.apply;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatReachHookTest {

	@Test
	void missingMobMethodYieldsIdentity() {
		assertEquals(1.0, CombatReachHook.extraMultiplierFromContext(new Object()));
	}
}
