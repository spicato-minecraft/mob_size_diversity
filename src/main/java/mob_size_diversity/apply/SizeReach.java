package mob_size_diversity.apply;

import mob_size_diversity.config.SizeConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Extra attack-reach coupling. {@link net.minecraft.world.entity.ai.attributes.Attributes#SCALE}
 * always applies; this multiplier is dropped when {@code reach_scales_with_size} is false.
 */
public final class SizeReach {

	private SizeReach() {
	}

	public static double extraMultiplier(LivingEntity entity) {
		if (!(entity instanceof Mob mob) || !SizeState.hasRoll(mob)) {
			return 1.0;
		}
		return extraMultiplier(SizeConfig.get().reachScalesWithSize(), SizeState.requireRoll(mob));
	}

	public static double extraMultiplier(boolean reachScalesWithSize, double rolledScale) {
		return reachScalesWithSize ? rolledScale : 1.0;
	}
}
