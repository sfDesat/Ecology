package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.climate.HabitatPocketTable;
import com.midas.ecology.worldgen.climate.PelagicColumnResolver;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Thin adapter: replaces vanilla deep oceans and expands shallow-ocean parents
 * into {@link HabitatPocketTable} placements. Climate rules live in climate components.
 */
@Mixin(OverworldBiomeBuilder.class)
public abstract class OverworldBiomeBuilderMixin {
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
		if (PelagicColumnResolver.isVanillaDeepOcean(biome)) {
			ci.cancel();
			acceptSurface(
				consumer,
				temperature,
				humidity,
				HabitatPocketTable.clampDeepContinentalness(continentalness),
				erosion,
				weirdness,
				offset,
				PelagicColumnResolver.replaceVanillaDeepOcean(biome)
			);
			return;
		}

		if (!HabitatPocketTable.isShallowOceanParent(biome)) {
			return;
		}

		List<HabitatPocketTable.Placement> placements = HabitatPocketTable.placementsFor(biome);
		if (placements == null) {
			return;
		}

		ci.cancel();

		for (HabitatPocketTable.Placement placement : placements) {
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
