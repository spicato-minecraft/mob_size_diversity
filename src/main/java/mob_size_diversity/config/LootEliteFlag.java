package mob_size_diversity.config;

import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Mob;

/**
 * Reads {@code mob_loot_rebalance:elite} without a compile dependency.
 */
public final class LootEliteFlag {

	public static final String LOOT_MOD_ID = "mob_loot_rebalance";
	private static final String ATTACHMENT_CLASS = "mob_loot_rebalance.elite.EliteAttachments";
	private static final String ATTACHMENT_FIELD = "IS_ELITE";

	private static AttachmentType<Boolean> type;
	private static boolean resolved;

	private LootEliteFlag() {
	}

	public static boolean isElite(Mob mob) {
		AttachmentType<Boolean> resolvedType = type();
		if (resolvedType == null) {
			return false;
		}
		return Boolean.TRUE.equals(mob.getAttachedOrElse(resolvedType, Boolean.FALSE));
	}

	@SuppressWarnings("unchecked")
	private static AttachmentType<Boolean> type() {
		if (resolved) {
			return type;
		}
		resolved = true;
		if (!FabricLoader.getInstance().isModLoaded(LOOT_MOD_ID)) {
			return null;
		}
		try {
			Class<?> attachments = Class.forName(ATTACHMENT_CLASS);
			type = (AttachmentType<Boolean>) attachments.getField(ATTACHMENT_FIELD).get(null);
			return type;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	public static void resetForTest() {
		resolved = false;
		type = null;
	}
}
