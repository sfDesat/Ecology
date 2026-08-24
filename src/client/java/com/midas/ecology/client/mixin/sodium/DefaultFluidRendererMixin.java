package com.midas.ecology.client.mixin.sodium;

import com.midas.ecology.client.render.WaterFaceMarker;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.client.model.color.ColorProvider;
import net.caffeinemc.mods.sodium.client.model.light.LightPipeline;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadViewMutable;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tags Sodium-meshed water faces with vertex alpha {@link WaterFaceMarker#ALPHA}
 * so Ecology's Sodium {@code block_layer_opaque} shaders can detect water.
 * Skipped when Distant water is Off (no overlay, vanilla Sodium shaders).
 */
@Mixin(DefaultFluidRenderer.class)
public class DefaultFluidRendererMixin {
	@Inject(method = "updateQuad", at = @At("HEAD"))
	private void ecology$captureFluid(
		ModelQuadViewMutable quad,
		LevelSlice level,
		BlockPos pos,
		LightPipeline lighter,
		Direction dir,
		ModelQuadFacing facing,
		float brightness,
		ColorProvider<?> colorProvider,
		FluidState fluidState,
		CallbackInfo ci
	) {
		WaterFaceMarker.beginQuad(fluidState);
	}

	@Inject(method = "updateQuad", at = @At("RETURN"))
	private void ecology$clearFluid(CallbackInfo ci) {
		WaterFaceMarker.endQuad();
	}

	/** Applies to every {@code ColorARGB.toABGR(I)I} in {@code updateQuad}. */
	@ModifyArg(
		method = "updateQuad",
		at = @At(
			value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/api/util/ColorARGB;toABGR(I)I"
		),
		index = 0
	)
	private int ecology$markWaterFaceTint(int argbColor) {
		if (WaterFaceMarker.currentQuadIsWater()) {
			return ColorARGB.withAlpha(argbColor, WaterFaceMarker.ALPHA);
		}
		return argbColor;
	}
}
