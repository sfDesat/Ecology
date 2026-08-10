package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.spawn.EcologySpawnRules;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets turtles pass spawn placement checks in Ecology grazing biomes when the
 * candidate position is underwater (vanilla is sand / on-ground only).
 */
@Mixin(SpawnPlacements.class)
public abstract class SpawnPlacementsMixin {
	@Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
	private static void ecology$turtleGrazingWater(
		EntityType<?> entityType,
		ServerLevelAccessor level,
		EntitySpawnReason spawnReason,
		BlockPos pos,
		RandomSource random,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (EcologySpawnRules.checkGrazingTurtleWaterSpawn(entityType, level, pos)) {
			cir.setReturnValue(true);
		}
	}
}
