package mob_size_diversity.apply;

import com.mojang.serialization.Codec;
import mob_size_diversity.MobSizeDiversity;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/**
 * Persisted rolled scale. Source of truth — not recomputed from UUID.
 */
public final class SizeAttachments {

	public static final AttachmentType<Double> ROLLED_SCALE = AttachmentRegistry.create(
			Identifier.fromNamespaceAndPath(MobSizeDiversity.MOD_ID, "rolled_scale"),
			builder -> builder.persistent(Codec.DOUBLE)
	);

	private SizeAttachments() {
	}
}
