package mob_size_diversity.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolve curve for a mob. Tag rules first (file order), then entity, then namespace.
 */
public final class ScaleRuleMatcher {

	public interface MatchContext {
		String entityId();

		String namespace();

		boolean inTag(String tagId);

		boolean virtualElite();
	}

	private ScaleRuleMatcher() {
	}

	public static Optional<ScaleRule> firstMatch(List<ScaleRule> rules, MatchContext context) {
		Optional<ScaleRule> tag = firstKind(rules, ScaleRule.Kind.TAG, context);
		if (tag.isPresent()) {
			return tag;
		}
		Optional<ScaleRule> entity = firstKind(rules, ScaleRule.Kind.ENTITY, context);
		if (entity.isPresent()) {
			return entity;
		}
		return firstKind(rules, ScaleRule.Kind.NAMESPACE, context);
	}

	private static Optional<ScaleRule> firstKind(List<ScaleRule> rules, ScaleRule.Kind kind, MatchContext context) {
		for (ScaleRule rule : rules) {
			if (rule.kind() != kind) {
				continue;
			}
			if (matches(rule, context)) {
				return Optional.of(rule);
			}
		}
		return Optional.empty();
	}

	private static boolean matches(ScaleRule rule, MatchContext context) {
		return switch (rule.kind()) {
			case TAG -> isEliteTag(rule.match())
					? context.virtualElite()
					: context.inTag(rule.match());
			case ENTITY -> rule.match().equals(context.entityId());
			case NAMESPACE -> rule.match().equals(context.namespace());
		};
	}

	static boolean isEliteTag(String tagId) {
		return "mob_loot_rebalance:elite".equals(tagId);
	}
}
