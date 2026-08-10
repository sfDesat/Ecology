package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.climate.PelagicColumnResolver;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies {@link PelagicColumnResolver} on deep-basin columns.
 * Open Ocean membership in {@code possibleBiomes()} is handled by {@link BiomeSourceMixin}.
 */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
	@Unique
	private PelagicColumnResolver.HolderCache ecology$holderCache;

	@Unique
	@SuppressWarnings("unchecked")
	private PelagicColumnResolver.HolderCache ecology$cache(Holder<Biome> sample) {
		if (this.ecology$holderCache == null) {
			Registry<Biome> registry = null;
			if (sample instanceof Holder.Reference<Biome> ref) {
				HolderOwner<?> owner = ((HolderReferenceAccessor) (Object) ref).ecology$getOwner();
				if (owner instanceof Registry<?> reg) {
					registry = (Registry<Biome>) reg;
				}
			}
			if (registry == null) {
				for (Holder<Biome> holder : ((BiomeSource) (Object) this).possibleBiomes()) {
					if (holder instanceof Holder.Reference<Biome> ref) {
						HolderOwner<?> owner = ((HolderReferenceAccessor) (Object) ref).ecology$getOwner();
						if (owner instanceof Registry<?> reg) {
							registry = (Registry<Biome>) reg;
							break;
						}
					}
				}
			}
			if (registry != null) {
				this.ecology$holderCache = PelagicColumnResolver.HolderCache.forSource(
					(BiomeSource) (Object) this,
					registry
				);
			}
		}
		return this.ecology$holderCache;
	}

	@Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;", at = @At("RETURN"), cancellable = true)
	private void ecology$applyPelagicLayers(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
		Holder<Biome> base = cir.getReturnValue();
		ResourceKey<Biome> horizontal = base.unwrapKey().orElse(null);
		if (horizontal == null) {
			return;
		}

		ResourceKey<Biome> pelagic = PelagicColumnResolver.resolve(horizontal, y);
		if (pelagic.equals(horizontal)) {
			return;
		}

		PelagicColumnResolver.HolderCache cache = ecology$cache(base);
		if (cache == null) {
			return;
		}
		Holder<Biome> resolved = cache.get(pelagic);
		if (resolved != null) {
			cir.setReturnValue(resolved);
		}
	}
}
