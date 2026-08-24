package com.midas.ecology.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.midas.ecology.client.render.SwimFogDistances;
import com.midas.ecology.client.render.fog.FogUboLayout;
import com.midas.ecology.client.render.fog.FogUboState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

/**
 * Replaces the Fog UBO size and write path with {@link FogUboLayout} so Ecology extras
 * ({@code EcologyWaterFogColor}, {@code EcologyCameraUnderwater}) are first-class fields.
 * <p>
 * Mutates {@link FogData} environmental fog in {@code setupFog} so Sodium's
 * {@code FogParameters} capture (and the vanilla UBO) both see stretched swim fog.
 * Priority {@code 900} (below Sodium's default 1000): at {@code RETURN}, lower priority
 * runs first, so FogData is stretched before Sodium copies it.
 */
@Mixin(value = FogRenderer.class, priority = 900)
public class FogRendererMixin {
	@Shadow
	@Final
	@Mutable
	public static int FOG_UBO_SIZE;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void ecology$useExtendedFogUboSize(CallbackInfo ci) {
		FOG_UBO_SIZE = FogUboLayout.SIZE;
	}

	/**
	 * Stretch swim fog on FogData and prepare UBO extras once — Sodium copies FogParameters
	 * after this RETURN, then vanilla {@code updateBuffer} writes the same values.
	 */
	@Inject(method = "setupFog", at = @At("RETURN"))
	private void ecology$stretchFogData(
		Camera camera,
		int renderDistanceInChunks,
		DeltaTracker deltaTracker,
		float darkenWorldAmount,
		ClientLevel level,
		CallbackInfoReturnable<?> cir,
		@Local FogData fog
	) {
		SwimFogDistances.apply(fog);
		FogUboState.prepare(fog.color);
	}

	@Inject(
		method = "updateBuffer(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void ecology$writeExtendedFogUbo(
		ByteBuffer buffer,
		int offset,
		Vector4f color,
		float environmentalStart,
		float environmentalEnd,
		float renderDistanceStart,
		float renderDistanceEnd,
		float skyEnd,
		float cloudEnd,
		CallbackInfo ci
	) {
		FogUboLayout.write(
			buffer,
			offset,
			color,
			environmentalStart,
			environmentalEnd,
			renderDistanceStart,
			renderDistanceEnd,
			skyEnd,
			cloudEnd,
			FogUboState.waterFogColor(),
			FogUboState.cameraUnderwater()
		);
		ci.cancel();
	}
}
