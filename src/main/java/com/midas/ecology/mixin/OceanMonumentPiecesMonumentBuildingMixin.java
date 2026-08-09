package com.midas.ecology.mixin;

import com.midas.ecology.worldgen.OceanMonumentSeafloor;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Replaces the hardcoded monument floor Y (39) so room pieces are placed in
 * world space relative to the seafloor instead of sea level.
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
		return OceanMonumentSeafloor.resolveMinY(vanillaMinY);
	}
}
