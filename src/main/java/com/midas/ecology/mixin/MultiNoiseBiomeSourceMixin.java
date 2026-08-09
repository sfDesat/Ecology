package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.OceanPelagicLayers;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
	@Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
	private void ecology$applyPelagicLayers(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
		Holder<Biome> base = cir.getReturnValue();
		ResourceKey<Biome> horizontal = base.unwrapKey().orElse(null);
		if (horizontal == null) {
			return;
		}

		ResourceKey<Biome> pelagic = OceanPelagicLayers.resolve(horizontal, y);
		if (pelagic.equals(horizontal)) {
			return;
		}

		BiomeSource source = (BiomeSource) (Object) this;
		for (Holder<Biome> candidate : source.possibleBiomes()) {
			if (candidate.is(pelagic)) {
				cir.setReturnValue(candidate);
				return;
			}
		}
	}
}
