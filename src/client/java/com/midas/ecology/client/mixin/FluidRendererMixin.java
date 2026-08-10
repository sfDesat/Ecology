package com.midas.ecology.client.mixin;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BLOCK terrain format has no normals, so water faces are tagged via vertex-color alpha
 * ({@value #ECOLOGY_WATER_FACE_MARKER_ALPHA}) for the distant-water terrain shader.
 * <p>
 * All {@code ARGB.scaleRGB} calls in {@code tesselate} are tagged (top, bottom, and sides)
 * so Fog tint can fix empty-behind water columns, not only the surface.
 * Always applied so toggles do not require chunk rebuilds; the vertex shader strips the marker.
 */
@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
	@Unique
	private static final int ECOLOGY_WATER_FACE_MARKER_ALPHA = 253;

	@Unique
	private static final ThreadLocal<FluidState> ECOLOGY_FLUID = new ThreadLocal<>();

	@Inject(method = "tesselate", at = @At("HEAD"))
	private void ecology$captureFluid(
		BlockAndTintGetter level,
		net.minecraft.core.BlockPos pos,
		FluidRenderer.Output output,
		net.minecraft.world.level.block.state.BlockState blockState,
		FluidState fluidState,
		CallbackInfo ci
	) {
		ECOLOGY_FLUID.set(fluidState);
	}

	@Inject(method = "tesselate", at = @At(value = "RETURN"), cancellable = false)
	private void ecology$clearFluid(CallbackInfo ci) {
		ECOLOGY_FLUID.remove();
	}

	@ModifyArg(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;scaleRGB(IF)I", ordinal = 0),
		index = 0
	)
	private int ecology$markWaterFaceTint0(int tintColor) {
		return ecology$markWaterFaceTint(tintColor);
	}

	@ModifyArg(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;scaleRGB(IF)I", ordinal = 1),
		index = 0
	)
	private int ecology$markWaterFaceTint1(int tintColor) {
		return ecology$markWaterFaceTint(tintColor);
	}

	@ModifyArg(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;scaleRGB(IF)I", ordinal = 2),
		index = 0
	)
	private int ecology$markWaterFaceTint2(int tintColor) {
		return ecology$markWaterFaceTint(tintColor);
	}

	@Unique
	private int ecology$markWaterFaceTint(int tintColor) {
		FluidState state = ECOLOGY_FLUID.get();
		if (state != null && state.is(FluidTags.WATER)) {
			return ARGB.color(ECOLOGY_WATER_FACE_MARKER_ALPHA, tintColor);
		}
		return tintColor;
	}
}
