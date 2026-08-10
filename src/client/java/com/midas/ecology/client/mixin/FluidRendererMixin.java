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
 * BLOCK terrain format has no normals, so water <b>UP</b> faces are tagged via vertex-color alpha
 * ({@value #ECOLOGY_WATER_TOP_MARKER_ALPHA}) for the water-surface terrain shader.
 * <p>
 * Targets the first {@code ARGB.scaleRGB} in {@code tesselate}, which is the top face
 * ({@code scaleRGB(tint, cardinalLighting.up())}). Bottom/side faces use later ordinals.
 * Always applied so toggles do not require chunk rebuilds; the vertex shader strips the marker.
 */
@Mixin(FluidRenderer.class)
public class FluidRendererMixin {
	@Unique
	private static final int ECOLOGY_WATER_TOP_MARKER_ALPHA = 253;

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

	@Inject(method = "tesselate", at = @At("RETURN"))
	private void ecology$clearFluid(CallbackInfo ci) {
		ECOLOGY_FLUID.remove();
	}

	@ModifyArg(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;scaleRGB(IF)I", ordinal = 0),
		index = 0
	)
	private int ecology$markWaterTopTint(int tintColor) {
		FluidState state = ECOLOGY_FLUID.get();
		if (state != null && state.is(FluidTags.WATER)) {
			return ARGB.color(ECOLOGY_WATER_TOP_MARKER_ALPHA, tintColor);
		}
		return tintColor;
	}
}
