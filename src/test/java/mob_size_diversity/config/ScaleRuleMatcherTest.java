package mob_size_diversity.config;

import mob_size_diversity.sampler.ScaleCurve;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaleRuleMatcherTest {

	@Test
	void tagOverridesEntityAndNamespace() {
		List<ScaleRule> rules = List.of(
				new ScaleRule(ScaleRule.Kind.ENTITY, "minecraft:zombie", "set_c", null),
				new ScaleRule(ScaleRule.Kind.TAG, "minecraft:undead", "set_a", null),
				new ScaleRule(ScaleRule.Kind.NAMESPACE, "minecraft", "set_c", null)
		);
		FakeContext zombie = new FakeContext("minecraft:zombie", "minecraft", Set.of("minecraft:undead"), false);
		assertEquals("set_a", ScaleRuleMatcher.firstMatch(rules, zombie).orElseThrow().presetId());
	}

	@Test
	void firstMatchingTagWins() {
		List<ScaleRule> rules = List.of(
				new ScaleRule(ScaleRule.Kind.TAG, "mob_loot_rebalance:elite", "set_b", null),
				new ScaleRule(ScaleRule.Kind.TAG, "minecraft:undead", "set_a", null)
		);
		FakeContext elite = new FakeContext("minecraft:zombie", "minecraft", Set.of("minecraft:undead"), true);
		assertEquals("set_b", ScaleRuleMatcher.firstMatch(rules, elite).orElseThrow().presetId());
	}

	@Test
	void namespaceIsFallbackForVanilla() {
		List<ScaleRule> rules = List.of(
				new ScaleRule(ScaleRule.Kind.TAG, "minecraft:undead", "set_a", null),
				new ScaleRule(ScaleRule.Kind.NAMESPACE, "minecraft", "set_c", null)
		);
		FakeContext cow = new FakeContext("minecraft:cow", "minecraft", Set.of(), false);
		assertEquals("set_c", ScaleRuleMatcher.firstMatch(rules, cow).orElseThrow().presetId());
	}

	@Test
	void otherNamespaceWithoutRuleDoesNotMatch() {
		List<ScaleRule> rules = List.of(
				new ScaleRule(ScaleRule.Kind.NAMESPACE, "minecraft", "set_c", null)
		);
		FakeContext other = new FakeContext("alexsmobs:grizzly_bear", "alexsmobs", Set.of(), false);
		assertTrue(ScaleRuleMatcher.firstMatch(rules, other).isEmpty());
	}

	@Test
	void defaultsResolveEliteThenHostileThenPassive() {
		SizeConfig config = SizeConfig.defaults();
		Optional<ScaleCurve> elite = config.resolve(new FakeContext("minecraft:zombie", "minecraft", Set.of("minecraft:undead"), true));
		Optional<ScaleCurve> zombie = config.resolve(new FakeContext("minecraft:zombie", "minecraft", Set.of("minecraft:undead"), false));
		Optional<ScaleCurve> cow = config.resolve(new FakeContext("minecraft:cow", "minecraft", Set.of(), false));
		assertEquals(ScaleCurve.setB(), elite.orElseThrow());
		assertEquals(ScaleCurve.setA(), zombie.orElseThrow());
		assertEquals(ScaleCurve.setC(), cow.orElseThrow());
	}

	private record FakeContext(String entityId, String namespace, Set<String> tags, boolean virtualElite)
			implements ScaleRuleMatcher.MatchContext {
		@Override
		public boolean inTag(String tagId) {
			return tags.contains(tagId);
		}
	}
}
