package mob_size_diversity.apply;

import mob_size_diversity.sampler.ScaleCurve;
import mob_size_diversity.sampler.ScaleSampler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/**
 * Roll once at first apply; persist; re-apply SCALE from the attachment.
 * Wave 1 uses shipped Set A for every eligible mob until JSON rules exist.
 */
public final class SizeRollService {

	private static final ScaleSampler SAMPLER = new ScaleSampler();
	private static ScaleCurve stubCurve = ScaleCurve.setA();

	private SizeRollService() {
	}

	public static void setStubCurveForTest(ScaleCurve curve) {
		stubCurve = curve != null ? curve : ScaleCurve.setA();
	}

	public static void ensureApplied(Entity entity) {
		if (entity.level().isClientSide() || !SizeEligibility.shouldRoll(entity)) {
			return;
		}
		Mob mob = (Mob) entity;
		if (!SizeState.hasRoll(mob)) {
			double rolled = SAMPLER.sample(stubCurve, mob.getRandom().nextDouble());
			SizeState.setRoll(mob, rolled);
		}
		SizeAttributes.apply(mob, SizeState.requireRoll(mob));
	}
}
