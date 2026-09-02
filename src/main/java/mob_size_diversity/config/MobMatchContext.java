package mob_size_diversity.config;

import mob_size_diversity.sampler.ScaleCurve;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public final class MobMatchContext implements ScaleRuleMatcher.MatchContext {

	private final Mob mob;
	private final Identifier typeId;

	public MobMatchContext(Mob mob) {
		this.mob = mob;
		this.typeId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
	}

	@Override
	public String entityId() {
		return typeId == null ? "" : typeId.toString();
	}

	@Override
	public String namespace() {
		return typeId == null ? "" : typeId.getNamespace();
	}

	@Override
	public boolean inTag(String tagId) {
		Identifier id;
		try {
			id = Identifier.parse(tagId);
		} catch (RuntimeException e) {
			return false;
		}
		TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, id);
		return mob.getType().builtInRegistryHolder().is(tag);
	}

	@Override
	public boolean virtualElite() {
		return LootEliteFlag.isElite(mob);
	}
}
