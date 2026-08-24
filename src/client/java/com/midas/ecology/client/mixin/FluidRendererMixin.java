package com.midas.ecology.client.mixin;

import com.midas.ecology.client.render.WaterFaceMarker;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BLOCK terrain format has no normals, so water faces are tagged via vertex-color alpha
 * ({@link WaterFaceMarker#ALPHA}) for the distant-water terrain shader.
 * <p>
 * All {@code ARGB.scaleRGB} calls in {@code tesselate} are tagged (top, bottom, and sides)
 * so Fog tint can fix empty-behind water columns, not only the surface.
 * Skipped when Distant water is Off so meshes stay vanilla.
 */
@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
	@Inject(method = "tesselate", at = @At("HEAD"))
	private void ecology$captureFluid(
		BlockAndTintGetter level,
		net.minecraft.core.BlockPos pos,
		FluidRenderer.Output output,
		net.minecraft.world.level.block.state.BlockState blockState,
		FluidState fluidState,
		CallbackInfo ci
	) {
		WaterFaceMarker.beginQuad(fluidState);
	}

	@Inject(method = "tesselate", at = @At(value = "RETURN"))
	private void ecology$clearFluid(CallbackInfo ci) {
		WaterFaceMarker.endQuad();
	}

	/** Applies to every {@code ARGB.scaleRGB} in {@code tesselate} (no ordinal — survives call reorders). */
	@ModifyArg(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;scaleRGB(IF)I"),
		index = 0
	)
	private int ecology$markWaterFaceTint(int tintColor) {
		if (WaterFaceMarker.currentQuadIsWater()) {
			return ARGB.color(WaterFaceMarker.ALPHA, tintColor);
		}
		return tintColor;
	}
}
