package com.midas.ecology.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.midas.ecology.worldgen.spawn.EcologySpawnRules;
import net.minecraft.world.entity.animal.fish.AbstractSchoolingFish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Vanilla {@code getMaxSpawnClusterSize()} for schooling fish is 8, so biome
 * {@code maxCount} values above that are silently truncated. Raise the cap so
 * open-ocean megaschools can actually place.
 */
@Mixin(AbstractSchoolingFish.class)
public abstract class AbstractSchoolingFishMixin {
	@ModifyReturnValue(method = "getMaxSpawnClusterSize", at = @At("RETURN"))
	private int ecology$raiseSchoolClusterCap(int original) {
		return Math.max(original, EcologySpawnRules.SCHOOLING_FISH_MAX_CLUSTER);
	}
}
