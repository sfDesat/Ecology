package com.midas.ecology.client.mixin;

import com.midas.ecology.client.render.fog.FogUboLayout;
import com.midas.ecology.client.render.fog.FogUboState;
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

import java.nio.ByteBuffer;

/**
 * Replaces the Fog UBO size and write path with {@link FogUboLayout} so Ecology extras
 * ({@code EcologyWaterFogColor}, {@code EcologyCameraUnderwater}) are first-class fields.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Shadow
	@Final
	@Mutable
	public static int FOG_UBO_SIZE;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void ecology$useExtendedFogUboSize(CallbackInfo ci) {
		FOG_UBO_SIZE = FogUboLayout.SIZE;
	}

	@Inject(method = "updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V", at = @At("HEAD"))
	private void ecology$prepareFogExtras(FogData fog, CallbackInfo ci) {
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
