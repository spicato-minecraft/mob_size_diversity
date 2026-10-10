package mob_size_diversity.gametest;

import mob_size_diversity.config.LootEliteFlag;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;

/**
 * Stand-in for {@code mob_loot_rebalance:elite}. The matcher treats that tag id as
 * {@link LootEliteFlag}, not as an entity-type tag, and the loot mod is not required.
 */
public final class TestEliteAttachments implements ModInitializer {

	public static final AttachmentType<Boolean> ELITE = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath("mob_size_diversity_test", "elite")
	);

	@Override
	public void onInitialize() {
		ELITE.getClass();
	}

	public static void bindLootEliteFlag() {
		try {
			Field type = LootEliteFlag.class.getDeclaredField("type");
			Field resolved = LootEliteFlag.class.getDeclaredField("resolved");
			type.setAccessible(true);
			resolved.setAccessible(true);
			type.set(null, ELITE);
			resolved.setBoolean(null, true);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not bind elite flag for test", e);
		}
	}
}
