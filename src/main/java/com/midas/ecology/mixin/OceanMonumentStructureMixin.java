package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.OceanMonumentSeafloor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Supplies a seafloor min Y while ocean monument pieces are built, so the outer
 * shell, interior rooms, and elder guardians all share the same vertical offset.
 */
@Mixin(OceanMonumentStructure.class)
public abstract class OceanMonumentStructureMixin {
	@Inject(method = "generatePieces", at = @At("HEAD"))
	private static void ecology$prepareSeafloorY(
		StructurePiecesBuilder builder,
		Structure.GenerationContext context,
		CallbackInfo ci
	) {
		OceanMonumentSeafloor.setPendingMinY(OceanMonumentSeafloor.targetMinY(context));
	}

	@Inject(method = "generatePieces", at = @At("RETURN"))
	private static void ecology$clearSeafloorY(
		StructurePiecesBuilder builder,
		Structure.GenerationContext context,
		CallbackInfo ci
	) {
		OceanMonumentSeafloor.clearPendingMinY();
	}

	@Inject(method = "regeneratePiecesAfterLoad", at = @At("HEAD"))
	private static void ecology$prepareSavedY(
		ChunkPos chunkPos,
		long seed,
		PiecesContainer original,
		CallbackInfoReturnable<PiecesContainer> cir
	) {
		if (!original.isEmpty()) {
			OceanMonumentSeafloor.setPendingMinY(original.pieces().getFirst().getBoundingBox().minY());
		}
	}

	@Inject(method = "regeneratePiecesAfterLoad", at = @At("RETURN"))
	private static void ecology$clearSavedY(
		ChunkPos chunkPos,
		long seed,
		PiecesContainer original,
		CallbackInfoReturnable<PiecesContainer> cir
	) {
		OceanMonumentSeafloor.clearPendingMinY();
	}
}
