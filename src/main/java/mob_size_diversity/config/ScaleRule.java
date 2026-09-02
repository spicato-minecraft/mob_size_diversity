package mob_size_diversity.config;

import mob_size_diversity.sampler.ScaleCurve;

/**
 * One JSON rule. Tags override entity/namespace. First matching tag in file order wins.
 */
public record ScaleRule(Kind kind, String match, String presetId, ScaleCurve inline) {

	public enum Kind {
		TAG,
		ENTITY,
		NAMESPACE
	}

	public ScaleCurve resolveCurve(java.util.Map<String, ScaleCurve> presets) {
		if (inline != null) {
			return inline;
		}
		ScaleCurve preset = presets.get(presetId);
		if (preset == null) {
			throw new IllegalStateException("unknown preset '" + presetId + "'");
		}
		return preset;
	}
}
