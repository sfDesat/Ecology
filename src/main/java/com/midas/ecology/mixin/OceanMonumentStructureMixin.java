package com.midas.ecology.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.midas.ecology.worldgen.structure.StructureFloorAligner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Supplies a seafloor min Y while ocean monument pieces are built (call-scoped).
 */
@Mixin(OceanMonumentStructure.class)
public abstract class OceanMonumentStructureMixin {
	@WrapMethod(method = "generatePieces")
	private static void ecology$alignToSeafloor(
		StructurePiecesBuilder builder,
		Structure.GenerationContext context,
		Operation<Void> original
	) {
		StructureFloorAligner.runWithMinY(
			StructureFloorAligner.monumentTargetMinY(context),
			() -> original.call(builder, context)
		);
	}

	@WrapMethod(method = "regeneratePiecesAfterLoad")
	private static PiecesContainer ecology$alignSavedPieces(
		ChunkPos chunkPos,
		long seed,
		PiecesContainer original,
		Operation<PiecesContainer> operation
	) {
		if (original.isEmpty()) {
			return operation.call(chunkPos, seed, original);
		}
		int minY = original.pieces().getFirst().getBoundingBox().minY();
		final PiecesContainer[] result = new PiecesContainer[1];
		StructureFloorAligner.runWithMinY(minY, () -> result[0] = operation.call(chunkPos, seed, original));
		return result[0];
	}
}
