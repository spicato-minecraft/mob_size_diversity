package mob_size_diversity.mixin;

import mob_size_diversity.apply.SizeRollService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin {

	@Inject(method = "finalizeSpawn", at = @At("RETURN"), order = 2000)
	private void mob_size_diversity$rollScaleAtSpawn(
			ServerLevelAccessor level,
			DifficultyInstance difficulty,
			EntitySpawnReason reason,
			@Nullable SpawnGroupData groupData,
			CallbackInfoReturnable<SpawnGroupData> cir
	) {
		if (!(level instanceof ServerLevel)) {
			return;
		}
		SizeRollService.ensureApplied((Mob) (Object) this);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void mob_size_diversity$reapplyScaleAfterLoad(ValueInput valueInput, CallbackInfo ci) {
		SizeRollService.ensureApplied((Mob) (Object) this);
	}
}
