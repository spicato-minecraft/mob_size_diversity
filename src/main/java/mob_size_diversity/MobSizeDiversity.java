package mob_size_diversity;

import mob_size_diversity.apply.SizeAttachments;
import mob_size_diversity.apply.SizeRollService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MobSizeDiversity implements ModInitializer {
	public static final String MOD_ID = "mob_size_diversity";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SizeAttachments.ROLLED_SCALE.getClass();
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> SizeRollService.ensureApplied(entity));
		LOGGER.info("Mob Size Diversity initialized");
	}
}
