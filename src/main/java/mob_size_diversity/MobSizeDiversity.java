package mob_size_diversity;

import mob_size_diversity.apply.CombatReachHook;
import mob_size_diversity.apply.SizeAttachments;
import mob_size_diversity.apply.SizeRollService;
import mob_size_diversity.config.SizeConfig;
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
		SizeConfig.get();
		CombatReachHook.register();
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> SizeRollService.ensureApplied(entity));
		net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("mob_loot_rebalance").ifPresent(container ->
				LOGGER.info("MLR loaded from {}", container.getOrigin())
		);
		LOGGER.info("Mob Size Diversity initialized");
	}
}
