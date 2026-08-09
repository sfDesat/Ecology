package com.midas.ecology.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

/**
 * Places a single waterlogged coral fan on the seafloor (standing sea-fan).
 */
public class SeafloorFanFeature extends Feature<NoneFeatureConfiguration> {
	private static final Block[] FANS = {
		Blocks.TUBE_CORAL_FAN,
		Blocks.BRAIN_CORAL_FAN,
		Blocks.BUBBLE_CORAL_FAN,
		Blocks.FIRE_CORAL_FAN,
		Blocks.HORN_CORAL_FAN
	};

	public SeafloorFanFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		return placeFan(context.level(), context.origin(), context.random());
	}

	static Block randomFan(RandomSource random) {
		return FANS[random.nextInt(FANS.length)];
	}

	static boolean placeFan(WorldGenLevel level, BlockPos origin, RandomSource random) {
		BlockPos.MutableBlockPos floor = origin.mutable();
		if (!isSeafloor(level.getBlockState(floor))) {
			if (isSeafloor(level.getBlockState(floor.below()))) {
				floor.move(0, -1, 0);
			} else {
				return false;
			}
		}

		BlockPos above = floor.above();
		if (!level.getFluidState(above).is(Fluids.WATER)) {
			return false;
		}
		if (!level.getBlockState(above).canBeReplaced()) {
			return false;
		}

		BlockState state = randomFan(random).defaultBlockState();
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		level.setBlock(above, state, 3);
		return true;
	}

	static boolean isSeafloor(BlockState state) {
		return state.is(BlockTags.SAND)
			|| state.is(Blocks.GRAVEL)
			|| state.is(Blocks.CLAY)
			|| state.is(Blocks.STONE)
			|| state.is(Blocks.COBBLESTONE)
			|| state.is(Blocks.DIRT)
			|| state.is(Blocks.COARSE_DIRT)
			|| state.is(Blocks.MOSS_BLOCK);
	}
}
