package mob_size_diversity.config;

import mob_size_diversity.sampler.ScaleCurve;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SizeConfigReloadTest {

	@AfterEach
	void resetInstance() {
		SizeConfig.resetForTest();
	}

	@Test
	void getKeepsCachedCurvesUntilReload(@TempDir Path tempDir) throws IOException {
		SizeConfig cached = SizeConfig.defaults();
		SizeConfig.setForTest(cached);
		assertEquals(ScaleCurve.setA().peak(), SizeConfig.get().presets.get("set_a").peak, 0.0001);

		SizeConfig onDisk = SizeConfig.defaults();
		onDisk.presets.get("set_a").peak = 1.2;
		onDisk.saveTo(tempDir.resolve("mob_size_diversity.json"));

		assertEquals(ScaleCurve.setA().peak(), SizeConfig.get().presets.get("set_a").peak, 0.0001,
				"get() must not pick up disk until reload()");
	}

	@Test
	void reloadFromRecompilesPresetsFromDisk(@TempDir Path tempDir) throws IOException {
		SizeConfig.setForTest(SizeConfig.defaults());

		SizeConfig onDisk = SizeConfig.defaults();
		onDisk.presets.get("set_a").peak = 1.2;
		onDisk.presets.get("set_a").min = 1.05;
		onDisk.presets.get("set_a").max = 1.25;
		onDisk.saveTo(tempDir.resolve("mob_size_diversity.json"));

		SizeConfig.reloadFrom(tempDir);

		assertEquals(1.2, SizeConfig.get().presets.get("set_a").peak, 0.0001);
		FakeContext zombie = new FakeContext("minecraft:zombie", "minecraft", Set.of("minecraft:undead"), false);
		ScaleCurve curve = SizeConfig.get().resolve(zombie).orElseThrow();
		assertEquals(1.2, curve.peak(), 0.0001);
		assertEquals(1.05, curve.min(), 0.0001);
		assertEquals(1.25, curve.max(), 0.0001);
		assertNotEquals(ScaleCurve.setA(), curve);
	}

	private record FakeContext(String entityId, String namespace, Set<String> tags, boolean virtualElite)
			implements ScaleRuleMatcher.MatchContext {
		@Override
		public boolean inTag(String tagId) {
			return tags.contains(tagId);
		}
	}
}
