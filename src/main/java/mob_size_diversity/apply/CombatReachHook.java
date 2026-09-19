package mob_size_diversity.apply;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Multiplies Better Combat / MCR attack range by {@link SizeReach#extraMultiplier}.
 * No compile dependency on combat rebalance.
 */
public final class CombatReachHook {

	public static final String COMBAT_MOD_ID = "mob_combat_rebalance";
	private static final Logger LOGGER = LogManager.getLogger("mob_size_diversity.reach");

	private CombatReachHook() {
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void register() {
		if (!FabricLoader.getInstance().isModLoaded(COMBAT_MOD_ID)) {
			return;
		}
		try {
			Class<?> extensions = Class.forName("me.Thelnfamous1.bettermobcombat.api.MobAttackRangeExtensions");
			Class<?> modifierClass = Class.forName("net.bettercombat.api.client.AttackRangeExtensions$Modifier");
			Class<?> operationClass = Class.forName("net.bettercombat.api.client.AttackRangeExtensions$Operation");
			Object multiply = Enum.valueOf((Class<Enum>) operationClass.asSubclass(Enum.class), "MULTIPLY");
			Constructor<?> ctor = modifierClass.getConstructor(double.class, operationClass);
			Method register = extensions.getMethod("register", Function.class);
			register.invoke(null, (Function<Object, Object>) context -> {
				try {
					return ctor.newInstance(extraMultiplierFromContext(context), multiply);
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException("size reach modifier", e);
				}
			});
			LOGGER.info("Registered size attack-reach multiply with {}", COMBAT_MOD_ID);
		} catch (ReflectiveOperationException e) {
			LOGGER.warn("Could not register size attack-reach with {}", COMBAT_MOD_ID, e);
		}
	}

	static double extraMultiplierFromContext(Object context) {
		try {
			Method mobMethod = context.getClass().getMethod("mob");
			Object living = mobMethod.invoke(context);
			if (living instanceof LivingEntity entity) {
				return SizeReach.extraMultiplier(entity);
			}
		} catch (ReflectiveOperationException ignored) {
			return 1.0;
		}
		return 1.0;
	}
}
