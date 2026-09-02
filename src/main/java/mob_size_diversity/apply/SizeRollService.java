package mob_size_diversity.apply;

import mob_size_diversity.config.MobMatchContext;
import mob_size_diversity.config.SizeConfig;
import mob_size_diversity.sampler.ScaleCurve;
import mob_size_diversity.sampler.ScaleSampler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.Optional;

/**
 * Roll once at first apply; persist; re-apply SCALE from the attachment.
 */
public final class SizeRollService {

	private static final ScaleSampler SAMPLER = new ScaleSampler();

	private SizeRollService() {
	}

	public static void ensureApplied(Entity entity) {
		if (entity.level().isClientSide() || !SizeEligibility.shouldRoll(entity)) {
			return;
		}
		Mob mob = (Mob) entity;
		if (!SizeState.hasRoll(mob)) {
			Optional<ScaleCurve> curve = SizeConfig.get().resolve(new MobMatchContext(mob));
			if (curve.isEmpty()) {
				return;
			}
			double rolled = SAMPLER.sample(curve.get(), mob.getRandom().nextDouble());
			SizeState.setRoll(mob, rolled);
		}
		SizeAttributes.apply(mob, SizeState.requireRoll(mob));
	}
}
