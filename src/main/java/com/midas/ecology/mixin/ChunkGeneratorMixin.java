package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.feature.PackIceFreezeFeature;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs pack-ice freeze once per chunk after biome features.
 *
 * <p>Vanilla {@code freeze_top_layer} / icebergs use {@code BiomeFilter} at the
 * chunk origin: if the corner is stony shore / snowy plains, frozen-ocean ice
 * features never run for that 16×16 (and the reverse skips land freeze). This
 * pass ices frozen-ocean family columns plus cold land/coast water so borders
 * stay continuous.
 */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
	@Inject(method = "applyBiomeDecoration", at = @At("TAIL"))
	private void ecology$applyPackIce(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci) {
		PackIceFreezeFeature.applyToChunk(level, chunk.getPos());
	}
}
