package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.EcologyBiomes;
import com.midas.ecology.worldgen.OceanHabitatPockets;
import com.midas.ecology.worldgen.OceanPelagicLayers;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces vanilla deep oceans with {@link EcologyBiomes#DEEP_OCEAN} and splits
 * shallow-ocean ParameterPoints into depth-banded habitat pockets.
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
	/**
	 * Per-builder flag (not static). A static latch skipped Open Ocean on later
	 * rebuilds in the same JVM — remapping then failed and Deep Ocean filled the
	 * whole column after save/quit/rejoin.
	 */
	@Unique
	private boolean ecology$pelagicRegistered;

	@Inject(method = "addSurfaceBiome", at = @At("HEAD"))
	private void ecology$registerPelagicParameterPoints(
		Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
		Climate.Parameter temperature,
		Climate.Parameter humidity,
		Climate.Parameter continentalness,
		Climate.Parameter erosion,
		Climate.Parameter weirdness,
		float offset,
		ResourceKey<Biome> biome,
		CallbackInfo ci
	) {
		if (this.ecology$pelagicRegistered) {
			return;
		}
		this.ecology$pelagicRegistered = true;

		// Unreachable climate so MultiNoise never picks it horizontally; it only
		// exists so possibleBiomes() contains Open Ocean for vertical remapping.
		Climate.Parameter impossibleContinentalness = Climate.Parameter.point(2.0f);
		acceptSurface(
			consumer,
			Climate.Parameter.span(-1.0f, 1.0f),
			Climate.Parameter.span(-1.0f, 1.0f),
			impossibleContinentalness,
			Climate.Parameter.span(-1.0f, 1.0f),
			Climate.Parameter.span(-1.0f, 1.0f),
			0.0f,
			EcologyBiomes.OPEN_OCEAN
		);
	}

	@Inject(method = "addSurfaceBiome", at = @At("HEAD"), cancellable = true)
	private void ecology$injectOceanHabitatPockets(
		Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
		Climate.Parameter temperature,
		Climate.Parameter humidity,
		Climate.Parameter continentalness,
		Climate.Parameter erosion,
		Climate.Parameter weirdness,
		float offset,
		ResourceKey<Biome> biome,
		CallbackInfo ci
	) {
		if (OceanPelagicLayers.isVanillaDeepOcean(biome)) {
			ci.cancel();
			acceptSurface(
				consumer,
				temperature,
				humidity,
				continentalness,
				erosion,
				weirdness,
				offset,
				EcologyBiomes.DEEP_OCEAN
			);
			return;
		}

		if (!OceanHabitatPockets.isShallowOceanContinentalness(continentalness)) {
			return;
		}

		List<OceanHabitatPockets.Placement> placements = OceanHabitatPockets.placementsFor(biome);
		if (placements == null) {
			return;
		}

		ci.cancel();

		for (OceanHabitatPockets.Placement placement : placements) {
			acceptSurface(
				consumer,
				temperature,
				humidity,
				placement.continentalness(),
				erosion,
				placement.weirdness(),
				offset,
				placement.biome()
			);
		}
	}

	private static void acceptSurface(
		Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
		Climate.Parameter temperature,
		Climate.Parameter humidity,
		Climate.Parameter continentalness,
		Climate.Parameter erosion,
		Climate.Parameter weirdness,
		float offset,
		ResourceKey<Biome> biome
	) {
		consumer.accept(Pair.of(
			Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(0.0f), weirdness, offset),
			biome
		));
		consumer.accept(Pair.of(
			Climate.parameters(temperature, humidity, continentalness, erosion, Climate.Parameter.point(1.0f), weirdness, offset),
			biome
		));
	}
}
