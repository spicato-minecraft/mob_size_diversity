package mob_size_diversity.gametest;

import mob_size_diversity.apply.SizeAttachments;
import mob_size_diversity.apply.SizeAttributes;
import mob_size_diversity.apply.SizeEligibility;
import mob_size_diversity.apply.SizeRollService;
import mob_size_diversity.apply.SizeReach;
import mob_size_diversity.apply.SizeState;
import mob_size_diversity.config.LootEliteFlag;
import mob_size_diversity.config.SizeConfig;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

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
	public void sulfurCubeUsesSetABand(GameTestHelper context) {
		SizeConfig.setForTest(SizeConfig.defaults());
		buildFloor(context);
		Mob cube = context.spawn(EntityTypes.SULFUR_CUBE, 1, 1, 1, EntitySpawnReason.COMMAND);
		if (LootEliteFlag.isElite(cube)) {
			throw fail("command-spawned sulfur cube should not be an elite");
		}
		if (!SizeState.hasRoll(cube)) {
			throw fail("sulfur cube missing rolled scale");
		}
		double rolled = SizeState.requireRoll(cube);
		if (rolled < 0.95 || rolled > 1.25) {
			throw fail("Set A roll out of range: " + rolled);
		}
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void zombieRollsPersistentScale(GameTestHelper context) {
		SizeConfig.setForTest(SizeConfig.defaults());
		buildFloor(context);
		Mob zombie = context.spawn(EntityTypes.ZOMBIE, 1, 1, 1, EntitySpawnReason.COMMAND);
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

	@GameTest(maxTicks = 100)
	public void eliteTagUsesSetB(GameTestHelper context) {
		TestEliteAttachments.bindLootEliteFlag();
		SizeConfig.setForTest(SizeConfig.defaults());
		try {
			buildFloor(context);
			Mob zombie = spawnMarkedElite(context);
			if (!LootEliteFlag.isElite(zombie)) {
				throw fail("elite flag was not visible to the matcher");
			}
			Double scale = SizeAttributes.modifierScale(zombie);
			if (scale == null || scale < 1.35 || scale > 1.55) {
				throw fail("Set B roll out of range: " + scale);
			}
			context.succeed();
		} finally {
			LootEliteFlag.resetForTest();
			SizeConfig.setForTest(SizeConfig.defaults());
		}
	}

	@GameTest(maxTicks = 100)
	public void reachOffKeepsVanillaReach(GameTestHelper context) {
		SizeConfig config = SizeConfig.defaults();
		config.reach_scales_with_size = false;
		SizeConfig.setForTest(config);
		try {
			buildFloor(context);
			Mob zombie = context.spawn(EntityTypes.ZOMBIE, 1, 1, 1, EntitySpawnReason.COMMAND);
			Double scale = SizeAttributes.modifierScale(zombie);
			if (scale == null || scale < 0.95 || scale > 1.25) {
				throw fail("hostile scale out of range with reach off: " + scale);
			}
			if (Math.abs(SizeReach.extraMultiplier(zombie) - 1.0) > 1e-6) {
				throw fail("extra interaction-range multiplier should be 1, was " + SizeReach.extraMultiplier(zombie));
			}
			if (SizeAttributes.extraReachModifier(zombie) != null) {
				throw fail("interaction-range modifier should be absent when reach coupling is off");
			}
			context.succeed();
		} finally {
			SizeConfig.setForTest(SizeConfig.defaults());
		}
	}

	@GameTest(maxTicks = 100)
	public void reloadKeepsSameRoll(GameTestHelper context) {
		SizeConfig.setForTest(SizeConfig.defaults());
		buildFloor(context);
		Mob zombie = context.spawn(EntityTypes.ZOMBIE, 1, 1, 1, EntitySpawnReason.NATURAL);
		Double original = SizeAttributes.modifierScale(zombie);
		if (original == null) {
			throw fail("zombie missing scale modifier before save");
		}
		ServerLevel level = context.getLevel();
		HolderLookup.Provider lookup = level.registryAccess();
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, lookup);
		if (!zombie.save(output)) {
			throw fail("zombie save failed");
		}
		ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, lookup, output.buildResult());
		Optional<Entity> loaded = EntityType.create(input, level, new EntitySpawnRequest(EntitySpawnReason.LOAD, false));
		if (loaded.isEmpty() || !(loaded.get() instanceof Mob loadedMob)) {
			throw fail("loaded entity was not a mob");
		}
		Double loadedScale = SizeAttributes.modifierScale(loadedMob);
		if (loadedScale == null || Math.abs(loadedScale - original) > 1e-6) {
			throw fail("scale modifier changed across load: " + original + " -> " + loadedScale);
		}
		context.succeed();
	}

	@GameTest(maxTicks = 100)
	public void mannequinIsNotRolled(GameTestHelper context) {
		buildFloor(context);
		Entity mannequin = context.spawn(EntityTypes.MANNEQUIN, 1, 1, 1);
		if (SizeEligibility.shouldRoll(mannequin)) {
			throw fail("mannequin should not be eligible");
		}
		if (mannequin.getAttached(SizeAttachments.ROLLED_SCALE) != null) {
			throw fail("mannequin must not receive a rolled scale");
		}
		if (mannequin instanceof LivingEntity living) {
			AttributeInstance scale = living.getAttribute(Attributes.SCALE);
			if (scale != null && scale.getModifier(SizeAttributes.ROLLED_SCALE_MODIFIER_ID) != null) {
				throw fail("mannequin has a rolled scale modifier");
			}
		}
		context.succeed();
	}

	private static Mob spawnMarkedElite(GameTestHelper context) {
		ServerLevel level = context.getLevel();
		Mob mob = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.NATURAL);
		if (mob == null) {
			throw fail("failed to create zombie");
		}
		mob.setAttached(TestEliteAttachments.ELITE, Boolean.TRUE);
		Vec3 spawnPos = context.absoluteVec(Vec3.atBottomCenterOf(new BlockPos(1, 1, 1)));
		mob.snapTo(spawnPos.x, spawnPos.y, spawnPos.z, 0.0F, 0.0F);
		mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.NATURAL, null);
		level.addFreshEntityWithPassengers(mob);
		return mob;
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
