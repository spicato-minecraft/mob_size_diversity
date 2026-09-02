package mob_size_diversity.apply;

import net.minecraft.world.entity.Mob;

public final class SizeState {

	private SizeState() {
	}

	public static boolean hasRoll(Mob mob) {
		return mob.getAttached(SizeAttachments.ROLLED_SCALE) != null;
	}

	public static double requireRoll(Mob mob) {
		Double rolled = mob.getAttached(SizeAttachments.ROLLED_SCALE);
		if (rolled == null) {
			throw new IllegalStateException("missing rolled scale on " + mob.getType());
		}
		return rolled;
	}

	public static void setRoll(Mob mob, double scale) {
		mob.setAttached(SizeAttachments.ROLLED_SCALE, scale);
	}
}
