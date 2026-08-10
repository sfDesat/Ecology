package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.structure.StructureFloorAligner;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Replaces the hardcoded monument floor Y so room pieces sit relative to the seafloor.
 */
@Mixin(OceanMonumentPieces.MonumentBuilding.class)
public abstract class OceanMonumentPiecesMonumentBuildingMixin {
	@ModifyArg(
		method = "<init>(Lnet/minecraft/util/RandomSource;IILnet/minecraft/core/Direction;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/structure/structures/OceanMonumentPieces$MonumentBuilding;makeBoundingBox(IIILnet/minecraft/core/Direction;III)Lnet/minecraft/world/level/levelgen/structure/BoundingBox;"
		),
		index = 1
	)
	private static int ecology$seafloorMinY(int vanillaMinY) {
		return StructureFloorAligner.resolveMinY(vanillaMinY);
	}
}
