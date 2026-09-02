package mob_size_diversity.apply;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

/**
 * Living mobs only. Players, armor stands, and mannequins are out.
 */
public final class SizeEligibility {

	private SizeEligibility() {
	}

	public static boolean shouldRoll(Entity entity) {
		if (!(entity instanceof Mob)) {
			return false;
		}
		if (entity instanceof Player || entity instanceof ArmorStand) {
			return false;
		}
		return !isMannequin(entity);
	}

	private static boolean isMannequin(Entity entity) {
		Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
		return id != null && "mannequin".equals(id.getPath());
	}
}
