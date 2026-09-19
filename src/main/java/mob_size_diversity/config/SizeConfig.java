package mob_size_diversity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mob_size_diversity.MobSizeDiversity;
import mob_size_diversity.sampler.ScaleCurve;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Primary config at {@code config/mob_size_diversity.json}. Datapack entity tags
 * only add membership to tags this file already references.
 */
public final class SizeConfig {

	private static final Logger LOGGER = LogManager.getLogger(MobSizeDiversity.MOD_ID + ".config");
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final String FILE_NAME = MobSizeDiversity.MOD_ID + ".json";

	private static volatile SizeConfig instance;

	public boolean reach_scales_with_size = true;
	public Map<String, CurveJson> presets = new LinkedHashMap<>();
	public List<RuleJson> rules = new ArrayList<>();

	private transient Map<String, ScaleCurve> resolvedPresets = Map.of();
	private transient List<ScaleRule> resolvedRules = List.of();

	public static SizeConfig get() {
		SizeConfig current = instance;
		if (current == null) {
			current = load();
			instance = current;
		}
		return current;
	}

	public static void setForTest(SizeConfig config) {
		config.compile();
		instance = config;
	}

	public static void resetForTest() {
		instance = null;
		LootEliteFlag.resetForTest();
	}

	/**
	 * Force a re-read from {@code config/mob_size_diversity.json} and recompile
	 * presets. Existing mob rolls are not changed.
	 */
	public static SizeConfig reload() {
		instance = load();
		return instance;
	}

	/**
	 * Force a re-read from {@code configDir/mob_size_diversity.json} into the
	 * process singleton and recompile presets.
	 */
	public static SizeConfig reloadFrom(Path configDir) {
		instance = loadFrom(configDir.resolve(FILE_NAME));
		return instance;
	}

	public static SizeConfig load() {
		return loadFrom(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME));
	}

	static SizeConfig loadFrom(Path path) {
		SizeConfig config = defaults();
		if (Files.isRegularFile(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
				SizeConfig fromFile = GSON.fromJson(root, SizeConfig.class);
				if (fromFile != null) {
					config = fromFile;
				}
			} catch (IOException | RuntimeException e) {
				LOGGER.error("Failed to read config {}, using defaults", path, e);
			}
		} else {
			try {
				Files.createDirectories(path.getParent());
				config.saveTo(path);
			} catch (IOException e) {
				LOGGER.warn("Could not write default config to {}", path, e);
			}
		}
		config.compile();
		return config;
	}

	public static SizeConfig defaults() {
		SizeConfig config = new SizeConfig();
		config.reach_scales_with_size = true;
		config.presets.put("set_a", CurveJson.from(ScaleCurve.setA()));
		config.presets.put("set_b", CurveJson.from(ScaleCurve.setB()));
		config.presets.put("set_c", CurveJson.from(ScaleCurve.setC()));
		config.rules.add(RuleJson.tag("mob_loot_rebalance:elite", "set_b"));
		config.rules.add(RuleJson.tag("minecraft:undead", "set_a"));
		config.rules.add(RuleJson.tag("minecraft:raiders", "set_a"));
		config.rules.add(RuleJson.tag("minecraft:illager", "set_a"));
		config.rules.add(RuleJson.tag("mob_size_diversity:set_a", "set_a"));
		config.rules.add(RuleJson.namespace("minecraft", "set_c"));
		config.compile();
		return config;
	}

	public Optional<ScaleCurve> resolve(ScaleRuleMatcher.MatchContext context) {
		return ScaleRuleMatcher.firstMatch(resolvedRules, context).map(rule -> rule.resolveCurve(resolvedPresets));
	}

	public boolean reachScalesWithSize() {
		return reach_scales_with_size;
	}

	void compile() {
		if (presets == null) {
			presets = new LinkedHashMap<>();
		}
		if (rules == null) {
			rules = new ArrayList<>();
		}
		LinkedHashMap<String, ScaleCurve> compiledPresets = new LinkedHashMap<>();
		for (Map.Entry<String, CurveJson> entry : presets.entrySet()) {
			compiledPresets.put(entry.getKey(), entry.getValue().toCurve());
		}
		List<ScaleRule> compiledRules = new ArrayList<>();
		for (RuleJson rule : rules) {
			compiledRules.add(rule.toRule());
		}
		resolvedPresets = Map.copyOf(compiledPresets);
		resolvedRules = List.copyOf(compiledRules);
	}

	public void save() {
		try {
			saveTo(FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME));
		} catch (IOException e) {
			LOGGER.error("Failed to save config {}", FILE_NAME, e);
		}
	}

	public void saveTo(Path path) throws IOException {
		Files.createDirectories(path.getParent());
		try (Writer writer = Files.newBufferedWriter(path)) {
			GSON.toJson(toJson(), writer);
		}
	}

	private JsonObject toJson() {
		JsonObject root = new JsonObject();
		root.addProperty("reach_scales_with_size", reach_scales_with_size);
		JsonObject presetsJson = new JsonObject();
		for (Map.Entry<String, CurveJson> entry : presets.entrySet()) {
			presetsJson.add(entry.getKey(), GSON.toJsonTree(entry.getValue()));
		}
		root.add("presets", presetsJson);
		JsonArray rulesJson = new JsonArray();
		for (RuleJson rule : rules) {
			rulesJson.add(GSON.toJsonTree(rule));
		}
		root.add("rules", rulesJson);
		return root;
	}

	public static final class CurveJson {
		public double peak;
		public double skew;
		public double min;
		public double max;
		public double center_q = 0.12;
		public double min_bound_q;
		public double max_bound_q;

		public static CurveJson from(ScaleCurve curve) {
			CurveJson json = new CurveJson();
			json.peak = curve.peak();
			json.skew = curve.skew();
			json.min = curve.min();
			json.max = curve.max();
			json.center_q = curve.centerQ();
			json.min_bound_q = curve.minBoundQ();
			json.max_bound_q = curve.maxBoundQ();
			return json;
		}

		ScaleCurve toCurve() {
			return new ScaleCurve(peak, skew, min, max, center_q, min_bound_q, max_bound_q);
		}
	}

	public static final class RuleJson {
		public String tag;
		public String entity;
		public String namespace;
		public String preset;
		public CurveJson curve;

		public static RuleJson tag(String tag, String preset) {
			RuleJson json = new RuleJson();
			json.tag = tag;
			json.preset = preset;
			return json;
		}

		public static RuleJson namespace(String namespace, String preset) {
			RuleJson json = new RuleJson();
			json.namespace = namespace;
			json.preset = preset;
			return json;
		}

		ScaleRule toRule() {
			int keys = 0;
			ScaleRule.Kind kind = null;
			String match = null;
			if (tag != null && !tag.isBlank()) {
				keys++;
				kind = ScaleRule.Kind.TAG;
				match = tag;
			}
			if (entity != null && !entity.isBlank()) {
				keys++;
				kind = ScaleRule.Kind.ENTITY;
				match = entity;
			}
			if (namespace != null && !namespace.isBlank()) {
				keys++;
				kind = ScaleRule.Kind.NAMESPACE;
				match = namespace;
			}
			if (keys != 1 || kind == null) {
				throw new IllegalArgumentException("rule must set exactly one of tag, entity, namespace");
			}
			ScaleCurve inline = curve == null ? null : curve.toCurve();
			if (inline == null && (preset == null || preset.isBlank())) {
				throw new IllegalArgumentException("rule needs preset or inline curve");
			}
			return new ScaleRule(kind, match, preset, inline);
		}
	}
}
