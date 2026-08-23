package com.midas.ecology.worldgen.feature;

import com.midas.ecology.worldgen.seafloor.SeafloorHelpers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

/**
 * Rare deep-basin hydrothermal vent field: basalt / blackstone mound with
 * several magma chimneys. Distinct from vanilla underwater_magma ore.
 */
public class HydrothermalVentFeature extends Feature<NoneFeatureConfiguration> {
	private static final BlockState[] CHIMNEY = {
		Blocks.BASALT.defaultBlockState(),
		Blocks.BLACKSTONE.defaultBlockState(),
		Blocks.SMOOTH_BASALT.defaultBlockState()
	};

	public HydrothermalVentFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos.MutableBlockPos floor = context.origin().mutable();

		if (!SeafloorHelpers.snapToSoftFloor(level, floor) || !SeafloorHelpers.hasWaterAbove(level, floor)) {
			return false;
		}

		int waterDepth = SeafloorHelpers.countWaterAbove(level, floor);
		if (waterDepth < 10) {
			return false;
		}

		BlockState rock = CHIMNEY[random.nextInt(CHIMNEY.length)];
		int padRadius = 4 + random.nextInt(3); // 4–6
		boolean placed = placePad(level, floor, padRadius, rock, random);

		int chimneyCount = 3 + random.nextInt(3); // 3–5 stacks
		for (int i = 0; i < chimneyCount; i++) {
			int ox = random.nextInt(padRadius * 2 + 1) - padRadius;
			int oz = random.nextInt(padRadius * 2 + 1) - padRadius;
			if (ox * ox + oz * oz > padRadius * padRadius) {
				continue;
			}
			BlockPos.MutableBlockPos base = floor.mutable().move(ox, 0, oz);
			int localY = SeafloorHelpers.findLocalSoftFloorY(level, base.getX(), floor.getY(), base.getZ(), padRadius + 2);
			if (localY == Integer.MIN_VALUE) {
				continue;
			}
			base.setY(localY);
			int height = 7 + random.nextInt(6); // 7–12
			height = Math.min(height, waterDepth - 2);
			if (placeChimney(level, base, height, rock, random)) {
				placed = true;
			}
		}

		return placed;
	}

	private static boolean placePad(
		WorldGenLevel level,
		BlockPos floor,
		int radius,
		BlockState rock,
		RandomSource random
	) {
		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				int distSq = dx * dx + dz * dz;
				if (distSq > radius * radius + random.nextInt(3)) {
					continue;
				}
				int localY = SeafloorHelpers.findLocalSoftFloorY(
					level,
					floor.getX() + dx,
					floor.getY(),
					floor.getZ() + dz,
					radius + 2
				);
				if (localY == Integer.MIN_VALUE) {
					continue;
				}
				// Raise a low mound toward the center.
				int rise = Math.max(0, (radius - (int) Math.sqrt(distSq) + 1) / 2);
				for (int y = localY; y <= localY + rise; y++) {
					cursor.set(floor.getX() + dx, y, floor.getZ() + dz);
					BlockState existing = level.getBlockState(cursor);
					boolean water = level.getFluidState(cursor).is(Fluids.WATER);
					if (y == localY) {
						if (SeafloorHelpers.isSoftSeafloor(existing) || SeafloorHelpers.isRock(existing)) {
							level.setBlock(cursor, rock, 3);
							placed = true;
						}
					} else if (SeafloorHelpers.canReplaceWaterOrAir(existing, water)) {
						level.setBlock(cursor, rock, 3);
						placed = true;
					}
				}
			}
		}
		return placed;
	}

	private static boolean placeChimney(
		WorldGenLevel level,
		BlockPos floor,
		int height,
		BlockState rock,
		RandomSource random
	) {
		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int trunkRadius = random.nextFloat() < 0.45f ? 1 : 0;

		for (int y = 1; y <= height; y++) {
			boolean tip = y == height;
			for (int dx = -trunkRadius; dx <= trunkRadius; dx++) {
				for (int dz = -trunkRadius; dz <= trunkRadius; dz++) {
					if (trunkRadius > 0 && (Math.abs(dx) + Math.abs(dz) > trunkRadius) && random.nextBoolean()) {
						continue;
					}
					// Tip is a single center magma only — no side magma on the spire.
					if (tip && (dx != 0 || dz != 0)) {
						continue;
					}
					cursor.set(floor.getX() + dx, floor.getY() + y, floor.getZ() + dz);
					if (!level.getFluidState(cursor).is(Fluids.WATER) && !level.getBlockState(cursor).canBeReplaced()) {
						continue;
					}
					BlockState column = tip ? Blocks.MAGMA_BLOCK.defaultBlockState() : rock;
					level.setBlock(cursor, column, 3);
					placed = true;
				}
			}
		}

		return placed;
	}
}
