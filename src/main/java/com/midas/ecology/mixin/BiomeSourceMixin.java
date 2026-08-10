package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.EcologyBiomes;
import com.midas.ecology.worldgen.climate.PelagicColumnResolver;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@link BiomeSource#possibleBiomes()} is defined here (not on
 * {@link MultiNoiseBiomeSource}). For Ecology overworld multi-noise sources that
 * already contain {@link EcologyBiomes#DEEP_BASIN}, also expose
 * {@link EcologyBiomes#OPEN_OCEAN} so 3D pelagic remapping can return a real Holder.
 */
@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin {
	@Inject(method = "possibleBiomes", at = @At("RETURN"), cancellable = true)
	@SuppressWarnings("unchecked")
	private void ecology$includePelagicMembership(CallbackInfoReturnable<Set<Holder<Biome>>> cir) {
		if (!((Object) this instanceof MultiNoiseBiomeSource)) {
			return;
		}

		Set<Holder<Biome>> biomes = cir.getReturnValue();
		boolean hasDeepBasin = false;
		boolean hasOpenOcean = false;
		Holder.Reference<Biome> sampleRef = null;

		for (Holder<Biome> holder : biomes) {
			if (holder.is(EcologyBiomes.DEEP_BASIN)) {
				hasDeepBasin = true;
			}
			if (holder.is(EcologyBiomes.OPEN_OCEAN)) {
				hasOpenOcean = true;
			}
			if (sampleRef == null && holder instanceof Holder.Reference<Biome> ref) {
				sampleRef = ref;
			}
		}

		if (!hasDeepBasin || hasOpenOcean || sampleRef == null) {
			return;
		}

		HolderOwner<?> owner = ((HolderReferenceAccessor) (Object) sampleRef).ecology$getOwner();
		if (!(owner instanceof Registry<?> registryRaw)) {
			return;
		}

		Registry<Biome> registry = (Registry<Biome>) registryRaw;
		Set<Holder<Biome>> expanded = new HashSet<>(biomes);
		boolean changed = false;

		for (ResourceKey<Biome> key : PelagicColumnResolver.membershipBiomes()) {
			Holder<Biome> holder = registry.get(key).orElse(null);
			if (holder != null && expanded.add(holder)) {
				changed = true;
			}
		}

		if (changed) {
			cir.setReturnValue(Set.copyOf(expanded));
		}
	}
}
