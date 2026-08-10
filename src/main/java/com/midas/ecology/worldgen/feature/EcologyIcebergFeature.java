package com.midas.ecology.worldgen.feature;

import com.midas.ecology.worldgen.surface.ColumnIceSystem;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.IcebergFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

/**
 * Iceberg that may place without {@code biome} placement filter. Validates the
 * column is frozen-ocean-family water/ice so border chunks (stony shore corner,
 * etc.) still get icebergs on frozen-ocean columns instead of skipping the
 * whole feature.
 */
public class EcologyIcebergFeature extends IcebergFeature {
	public EcologyIcebergFeature(Codec<BlockStateConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
		WorldGenLevel level = context.level();
		BlockPos origin = context.origin();
		int seaLevel = context.chunkGenerator().getSeaLevel();
		BlockPos atSea = new BlockPos(origin.getX(), seaLevel, origin.getZ());

		Holder<Biome> biome = level.getBiome(atSea);
		if (!ColumnIceSystem.isFrozenOceanFamily(biome)) {
			return false;
		}
		if (!isSeaSurfaceWaterOrIce(level, atSea)) {
			return false;
		}

		return super.place(context);
	}

	private static boolean isSeaSurfaceWaterOrIce(WorldGenLevel level, BlockPos atSea) {
		BlockState state = level.getBlockState(atSea);
		if (state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.FROSTED_ICE)) {
			return true;
		}
		if (level.getFluidState(atSea).is(FluidTags.WATER)) {
			return true;
		}
		BlockState below = level.getBlockState(atSea.below());
		return below.is(Blocks.ICE)
			|| below.is(Blocks.PACKED_ICE)
			|| level.getFluidState(atSea.below()).is(FluidTags.WATER);
	}
}
