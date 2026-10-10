package mob_size_diversity.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

/**
 * Loads MobMixin's target so a renamed class fails here instead of in a player session.
 */
public class ServerMixinApplySweepGameTest {

	@GameTest(maxTicks = 20)
	public void serverMixinApplySweep(GameTestHelper context) {
		load("net.minecraft.world.entity.Mob");
		context.succeed();
	}

	private static void load(String name) {
		try {
			Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new GameTestAssertException(Component.literal("Mixin target failed to load: " + name), 0);
		}
	}
}
