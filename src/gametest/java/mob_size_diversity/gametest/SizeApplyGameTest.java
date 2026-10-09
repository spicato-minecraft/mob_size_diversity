package mob_size_diversity.gametest;

import mob_size_diversity.apply.SizeAttachments;
import mob_size_diversity.apply.SizeAttributes;
import mob_size_diversity.apply.SizeEligibility;
import mob_size_diversity.apply.SizeRollService;
import mob_size_diversity.apply.SizeReach;
import mob_size_diversity.apply.SizeState;
import mob_size_diversity.config.SizeConfig;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;

public class SizeApplyGameTest {

	@GameTest(maxTicks = 100)
	public void cowUsesSetCBand(GameTestHelper context) {
		SizeConfig.setForTest(SizeConfig.defaults());
		buildFloor(context);
		Mob cow = context.spawn(EntityTypes.COW, 1, 1, 1, EntitySpawnReason.NATURAL);
		if (!SizeState.hasRoll(cow)) {
			throw fail("cow missing rolled scale");
		}
		double rolled = SizeState.requireRoll(cow);
		if (rolled < 0.95 || rolled > 1.15) {
			throw fail("Set C roll out of range: " + rolled);
		}
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void zombieRollsPersistentScale(GameTestHelper context) {
		SizeConfig.setForTest(SizeConfig.defaults());
		buildFloor(context);
		Mob zombie = context.spawn(EntityTypes.ZOMBIE, 1, 1, 1, EntitySpawnReason.NATURAL);
		if (!SizeState.hasRoll(zombie)) {
			throw fail("zombie missing rolled scale attachment");
		}
		double rolled = SizeState.requireRoll(zombie);
		if (rolled < 0.95 || rolled > 1.25) {
			throw fail("Set A roll out of range: " + rolled);
		}
		Double modifier = SizeAttributes.modifierScale(zombie);
		if (modifier == null || Math.abs(modifier - rolled) > 1e-6) {
			throw fail("SCALE modifier " + modifier + " != attachment " + rolled);
		}
		double first = rolled;
		if (Math.abs(SizeReach.extraMultiplier(zombie) - rolled) > 1e-6) {
			throw fail("default extra reach should follow rolled scale: " + SizeReach.extraMultiplier(zombie));
		}
		zombie.tickCount++;
		SizeRollService.ensureApplied(zombie);
		if (SizeState.requireRoll(zombie) != first) {
			throw fail("ensureApplied rerolled");
		}
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void armorStandIsNotRolled(GameTestHelper context) {
		buildFloor(context);
		ArmorStand stand = context.spawn(EntityTypes.ARMOR_STAND, 1, 1, 1);
		if (SizeEligibility.shouldRoll(stand)) {
			throw fail("armor stand should not be eligible");
		}
		if (stand.getAttached(SizeAttachments.ROLLED_SCALE) != null) {
			throw fail("armor stand must not receive a rolled scale");
		}
		context.succeed();
	}

	private static void buildFloor(GameTestHelper context) {
		for (int x = 0; x <= 2; x++) {
			for (int z = 0; z <= 2; z++) {
				context.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
	}

	private static GameTestAssertException fail(String message) {
		return new GameTestAssertException(Component.literal(message), 0);
	}
}
