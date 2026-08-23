package com.midas.ecology.worldgen.feature;

import com.midas.ecology.worldgen.seafloor.SeafloorHelpers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Places a single waterlogged dead (gray) coral fan on the seafloor.
 */
public class DeadSeafloorFanFeature extends Feature<NoneFeatureConfiguration> {
	public DeadSeafloorFanFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos.MutableBlockPos floor = context.origin().mutable();
		if (!SeafloorHelpers.snapToSoftFloor(level, floor)) {
			return false;
		}
		return SeafloorHelpers.tryPlaceDeadFan(level, floor.above(), random);
	}
}
