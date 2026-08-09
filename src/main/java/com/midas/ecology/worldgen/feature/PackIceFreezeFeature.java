package com.midas.ecology.worldgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.LightLayer;

/**
 * Custom surface ice applied once per chunk after decoration (see
 * {@code ChunkGeneratorMixin}). Decides per column so ice is not gated by the
 * chunk-corner biome's feature list.
 *
 * <p>Also <em>opens</em> thin ice on Polynya / Ice Edge / pack holes. Vanilla
 * {@code freeze_top_layer} can still solid-freeze those columns when the chunk
 * corner is a land biome that shares that feature — without clearing, open
 * pockets stay iced and look like square cutoffs.
 */
public class PackIceFreezeFeature extends Feature<NoneFeatureConfiguration> {
	private static final PerlinSimplexNoise HOLE_NOISE = new PerlinSimplexNoise(
		new WorldgenRandom(new LegacyRandomSource(8742L)),
		ImmutableList.of(0)
	);
	private static final PerlinSimplexNoise HOLE_SHAPE_NOISE = new PerlinSimplexNoise(
		new WorldgenRandom(new LegacyRandomSource(8743L)),
		ImmutableList.of(0)
	);
	private static final PerlinSimplexNoise FLOE_NOISE = new PerlinSimplexNoise(
		new WorldgenRandom(new LegacyRandomSource(8744L)),
		ImmutableList.of(0)
	);
	private static final PerlinSimplexNoise PLATE_NOISE = new PerlinSimplexNoise(
		new WorldgenRandom(new LegacyRandomSource(8745L)),
		ImmutableList.of(0)
	);

	public PackIceFreezeFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		BlockPos origin = context.origin();
		applyToChunk(context.level(), new ChunkPos(origin.getX() >> 4, origin.getZ() >> 4));
		return true;
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
					// Undo vanilla freeze_top_layer on open pockets / pack holes.
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
		if (key.equals(Biomes.FROZEN_OCEAN) || key.equals(EcologyBiomes.SYMPAGIC_ZONE)) {
			return IceMode.PACK;
		}
		if (key.equals(EcologyBiomes.ICE_EDGE)) {
			return IceMode.EDGE;
		}
		if (key.equals(EcologyBiomes.POLYNYA)) {
			return IceMode.POLYNYA;
		}
		if (key.equals(Biomes.STONY_SHORE) || key.equals(Biomes.SNOWY_BEACH)) {
			return IceMode.SOLID;
		}
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

	static boolean isPackWaterHole(int x, int z) {
		double center = HOLE_NOISE.getValue(x * 0.028, z * 0.028, false);
		if (center < 0.40) {
			return false;
		}
		double shape = HOLE_SHAPE_NOISE.getValue(x * 0.11, z * 0.11, false);
		return (center * 0.7 + shape * 0.3) > 0.34;
	}

	static boolean isEdgeIce(int x, int z) {
		double plate = PLATE_NOISE.getValue(x * 0.022, z * 0.022, false);
		if (plate > 0.50) {
			double plateShape = HOLE_SHAPE_NOISE.getValue(x * 0.07, z * 0.07, false);
			if ((plate * 0.7 + plateShape * 0.3) > 0.42) {
				return true;
			}
		}
		return isSparseFloe(x, z, 0.58);
	}

	static boolean isSparseFloe(int x, int z, double threshold) {
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

	/** Thin freeze ice only — leave iceberg packed/blue ice alone. */
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
