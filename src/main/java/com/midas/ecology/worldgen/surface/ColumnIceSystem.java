package com.midas.ecology.worldgen.surface;

import com.google.common.collect.ImmutableList;
import com.midas.ecology.worldgen.EcologyBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.LightLayer;

/**
 * Column-based sea-ice system. Applied once per chunk after biome decoration so
 * ice is decided per column (not gated by the chunk-corner biome feature list).
 *
 * <p>Also opens thin ice on Polynya / Ice Edge / pack holes when vanilla
 * {@code freeze_top_layer} iced them from a neighboring land biome.
 */
public final class ColumnIceSystem {
	private static final long HOLE_SEED = 8742L;
	private static final long HOLE_SHAPE_SEED = 8743L;
	private static final long FLOE_SEED = 8744L;
	private static final long PLATE_SEED = 8745L;

	private static final PerlinSimplexNoise HOLE_NOISE = new PerlinSimplexNoise(
		new WorldgenRandom(new LegacyRandomSource(HOLE_SEED)),
		ImmutableList.of(0)
	);
	private static final PerlinSimplexNoise HOLE_SHAPE_NOISE = new PerlinSimplexNoise(
		new WorldgenRandom(new LegacyRandomSource(HOLE_SHAPE_SEED)),
		ImmutableList.of(0)
	);
	private static final PerlinSimplexNoise FLOE_NOISE = new PerlinSimplexNoise(
		new WorldgenRandom(new LegacyRandomSource(FLOE_SEED)),
		ImmutableList.of(0)
	);
	private static final PerlinSimplexNoise PLATE_NOISE = new PerlinSimplexNoise(
		new WorldgenRandom(new LegacyRandomSource(PLATE_SEED)),
		ImmutableList.of(0)
	);

	private ColumnIceSystem() {
	}

	public static void applyToChunk(WorldGenLevel level, ChunkPos chunkPos) {
		BlockPos.MutableBlockPos topPos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos surfacePos = new BlockPos.MutableBlockPos();
		int originX = chunkPos.getMinBlockX();
		int originZ = chunkPos.getMinBlockZ();

		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = originX + dx;
				int z = originZ + dz;
				int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
				topPos.set(x, y, z);
				surfacePos.set(topPos).move(Direction.DOWN, 1);

				Holder<Biome> biomeHolder = level.getBiome(surfacePos);
				Biome biome = biomeHolder.value();
				IceMode mode = iceMode(biomeHolder, biome);
				if (mode == IceMode.NONE) {
					continue;
				}

				boolean wantIce = shouldPlaceIce(mode, x, z);
				if (wantIce) {
					if (canFreezeWater(level, surfacePos)) {
						level.setBlock(surfacePos, Blocks.ICE.defaultBlockState(), 2);
					}
					if (biome.shouldSnow(level, topPos)) {
						level.setBlock(topPos, Blocks.SNOW.defaultBlockState(), 2);
						BlockState belowState = level.getBlockState(surfacePos);
						if (belowState.hasProperty(SnowyBlock.SNOWY)) {
							level.setBlock(surfacePos, belowState.setValue(SnowyBlock.SNOWY, true), 2);
						}
					}
				} else if (mode.opensWater()) {
					openThinIce(level, surfacePos);
					openThinIce(level, topPos);
					clearSnowAboveWater(level, topPos, surfacePos);
				}
			}
		}
	}

	public static boolean isFrozenOceanFamily(Holder<Biome> biome) {
		ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
		if (key == null) {
			return false;
		}
		return key.equals(Biomes.FROZEN_OCEAN)
			|| key.equals(EcologyBiomes.SYMPAGIC_ZONE)
			|| key.equals(EcologyBiomes.ICE_EDGE)
			|| key.equals(EcologyBiomes.POLYNYA);
	}

	private static IceMode iceMode(Holder<Biome> holder, Biome biome) {
		ResourceKey<Biome> key = holder.unwrapKey().orElse(null);
		if (key == null) {
			return IceMode.NONE;
		}
		if (key.equals(Biomes.FROZEN_OCEAN)
			|| key.equals(EcologyBiomes.SYMPAGIC_ZONE)) {
			return IceMode.PACK;
		}
		if (key.equals(EcologyBiomes.ICE_EDGE)) {
			return IceMode.EDGE;
		}
		if (key.equals(EcologyBiomes.POLYNYA)) {
			return IceMode.POLYNYA;
		}
		// Do not ice stony shore / temperate coasts — only frozen-ocean-family biomes
		// above get pack/edge/polynya ice. Truly cold land (<0.15) may still freeze
		// standing water (snowy beach, etc.); stony shore is ~0.2 and must stay open.
		if (biome.getBaseTemperature() < 0.15F) {
			return IceMode.SOLID;
		}
		return IceMode.NONE;
	}

	private static boolean shouldPlaceIce(IceMode mode, int x, int z) {
		return switch (mode) {
			case PACK -> !isPackWaterHole(x, z);
			case EDGE -> isEdgeIce(x, z);
			case POLYNYA -> isSparseFloe(x, z, 0.82);
			case SOLID -> true;
			case NONE -> false;
		};
	}

	private static boolean isPackWaterHole(int x, int z) {
		double center = HOLE_NOISE.getValue(x * 0.028, z * 0.028, false);
		if (center < 0.40) {
			return false;
		}
		double shape = HOLE_SHAPE_NOISE.getValue(x * 0.11, z * 0.11, false);
		return (center * 0.7 + shape * 0.3) > 0.34;
	}

	private static boolean isEdgeIce(int x, int z) {
		double plate = PLATE_NOISE.getValue(x * 0.022, z * 0.022, false);
		if (plate > 0.50) {
			double plateShape = HOLE_SHAPE_NOISE.getValue(x * 0.07, z * 0.07, false);
			if ((plate * 0.7 + plateShape * 0.3) > 0.42) {
				return true;
			}
		}
		return isSparseFloe(x, z, 0.58);
	}

	private static boolean isSparseFloe(int x, int z, double threshold) {
		double floe = FLOE_NOISE.getValue(x * 0.09, z * 0.09, false);
		double detail = HOLE_SHAPE_NOISE.getValue(x * 0.22, z * 0.22, false);
		return (floe * 0.8 + detail * 0.2) > threshold;
	}

	private static boolean canFreezeWater(WorldGenLevel level, BlockPos pos) {
		if (!level.isInsideBuildHeight(pos.getY()) || level.getBrightness(LightLayer.BLOCK, pos) >= 10) {
			return false;
		}
		BlockState blockState = level.getBlockState(pos);
		return level.getFluidState(pos).is(Fluids.WATER) && blockState.getBlock() instanceof LiquidBlock;
	}

	private static boolean isThinIce(BlockState state) {
		return state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE);
	}

	private static void openThinIce(WorldGenLevel level, BlockPos pos) {
		if (!level.isInsideBuildHeight(pos.getY())) {
			return;
		}
		if (isThinIce(level.getBlockState(pos))) {
			level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
		}
	}

	private static void clearSnowAboveWater(WorldGenLevel level, BlockPos topPos, BlockPos surfacePos) {
		if (level.getBlockState(topPos).is(Blocks.SNOW) && level.getFluidState(surfacePos).is(Fluids.WATER)) {
			level.setBlock(topPos, Blocks.AIR.defaultBlockState(), 2);
		}
	}

	private enum IceMode {
		NONE,
		PACK,
		EDGE,
		POLYNYA,
		SOLID;

		boolean opensWater() {
			return this == PACK || this == EDGE || this == POLYNYA;
		}
	}
}
