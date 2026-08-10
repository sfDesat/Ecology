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
 * Places a single waterlogged coral fan on the seafloor (standing sea-fan).
 */
public class SeafloorFanFeature extends Feature<NoneFeatureConfiguration> {
	public SeafloorFanFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		return placeFan(context.level(), context.origin(), context.random());
	}

	static boolean placeFan(WorldGenLevel level, BlockPos origin, RandomSource random) {
		BlockPos.MutableBlockPos floor = origin.mutable();
		if (!SeafloorHelpers.snapToSoftFloor(level, floor)) {
			return false;
		}
		return SeafloorHelpers.tryPlaceFan(level, floor.above(), random);
	}
}
