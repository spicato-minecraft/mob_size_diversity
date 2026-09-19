package mob_size_diversity.apply;

import mob_size_diversity.MobSizeDiversity;
import mob_size_diversity.config.SizeConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Writes the rolled factor onto vanilla {@link Attributes#SCALE} without replacing
 * baby/slime vanilla modifiers (multiplied total).
 */
public final class SizeAttributes {

	public static final Identifier ROLLED_SCALE_MODIFIER_ID =
			Identifier.fromNamespaceAndPath(MobSizeDiversity.MOD_ID, "rolled_scale");
	public static final Identifier ROLLED_REACH_MODIFIER_ID =
			Identifier.fromNamespaceAndPath(MobSizeDiversity.MOD_ID, "rolled_attack_reach");

	private SizeAttributes() {
	}

	public static void apply(Mob mob, double rolledScale) {
		applyScale(mob, rolledScale);
		applyExtraReach(mob, SizeReach.extraMultiplier(SizeConfig.get().reachScalesWithSize(), rolledScale));
	}

	private static void applyScale(Mob mob, double rolledScale) {
		AttributeInstance instance = mob.getAttribute(Attributes.SCALE);
		if (instance == null) {
			return;
		}
		instance.removeModifier(ROLLED_SCALE_MODIFIER_ID);
		instance.addPermanentModifier(new AttributeModifier(
				ROLLED_SCALE_MODIFIER_ID,
				rolledScale - 1.0,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
		));
	}

	private static void applyExtraReach(Mob mob, double extraMultiplier) {
		AttributeInstance instance = mob.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		if (instance == null) {
			return;
		}
		instance.removeModifier(ROLLED_REACH_MODIFIER_ID);
		if (extraMultiplier == 1.0) {
			return;
		}
		instance.addPermanentModifier(new AttributeModifier(
				ROLLED_REACH_MODIFIER_ID,
				extraMultiplier - 1.0,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
		));
	}

	public static Double extraReachModifier(Mob mob) {
		AttributeInstance instance = mob.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
		if (instance == null) {
			return null;
		}
		AttributeModifier modifier = instance.getModifier(ROLLED_REACH_MODIFIER_ID);
		if (modifier == null) {
			return null;
		}
		return modifier.amount() + 1.0;
	}

	public static Double modifierScale(Mob mob) {
		AttributeInstance instance = mob.getAttribute(Attributes.SCALE);
		if (instance == null) {
			return null;
		}
		AttributeModifier modifier = instance.getModifier(ROLLED_SCALE_MODIFIER_ID);
		if (modifier == null) {
			return null;
		}
		return modifier.amount() + 1.0;
	}
}
