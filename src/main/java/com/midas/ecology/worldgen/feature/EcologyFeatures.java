package com.midas.ecology.worldgen.feature;

import com.midas.ecology.EcologyMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class EcologyFeatures {
	public static final Feature<UnderwaterRockConfiguration> UNDERWATER_ROCK =
		Registry.register(
			BuiltInRegistries.FEATURE,
			EcologyMod.id("underwater_rock"),
			new UnderwaterRockFeature(UnderwaterRockConfiguration.CODEC)
		);

	public static final Feature<PatchReefIslandConfiguration> PATCH_REEF_ISLAND =
		Registry.register(
			BuiltInRegistries.FEATURE,
			EcologyMod.id("patch_reef_island"),
			new PatchReefIslandFeature(PatchReefIslandConfiguration.CODEC)
		);

	public static final Feature<CoralReefConfiguration> CORAL_REEF =
		Registry.register(
			BuiltInRegistries.FEATURE,
			EcologyMod.id("coral_reef"),
			new CoralReefFeature(CoralReefConfiguration.CODEC)
		);

	public static final Feature<UnderwaterRockConfiguration> CORAL_ENCRUSTED_ROCK =
		Registry.register(
			BuiltInRegistries.FEATURE,
			EcologyMod.id("coral_encrusted_rock"),
			new CoralEncrustedRockFeature(UnderwaterRockConfiguration.CODEC)
		);

	public static final Feature<NoneFeatureConfiguration> SEAFLOOR_FAN =
		Registry.register(
			BuiltInRegistries.FEATURE,
			EcologyMod.id("seafloor_fan"),
			new SeafloorFanFeature(NoneFeatureConfiguration.CODEC)
		);

	public static final Feature<NoneFeatureConfiguration> DEAD_SEAFLOOR_FAN =
		Registry.register(
			BuiltInRegistries.FEATURE,
			EcologyMod.id("dead_seafloor_fan"),
			new DeadSeafloorFanFeature(NoneFeatureConfiguration.CODEC)
		);

	public static final Feature<SoftCoralClumpConfiguration> SOFT_CORAL_CLUMP =
		Registry.register(
			BuiltInRegistries.FEATURE,
			EcologyMod.id("soft_coral_clump"),
			new SoftCoralClumpFeature(SoftCoralClumpConfiguration.CODEC)
		);

	/** Ecology-owned iceberg with frozen-ocean column checks (no biome filter needed). */
	public static final Feature<BlockStateConfiguration> ICEBERG =
		Registry.register(
			BuiltInRegistries.FEATURE,
			EcologyMod.id("iceberg"),
			new EcologyIcebergFeature(BlockStateConfiguration.CODEC)
		);

	private EcologyFeatures() {
	}

	public static void register() {
		EcologyMod.LOGGER.info("Registered worldgen features");
	}
}
