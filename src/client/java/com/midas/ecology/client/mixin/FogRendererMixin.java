package com.midas.ecology.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks underwater frames so {@code core/terrain} can skip water-surface opacity.
 * <p>
 * Encodes the flag in {@link FogData#cloudEnd} as {@link #ECOLOGY_UNDERWATER_CLOUD_END}.
 * Water fog already replaces cloud distance with a short environmental end; clouds are not
 * drawn while submerged, so this channel is unused for its original purpose underwater.
 */
@Mixin(FogRenderer.class)
public class FogRendererMixin {
	@Unique
	private static final float ECOLOGY_UNDERWATER_CLOUD_END = -1.0F;

	@Inject(method = "updateBuffer(Lnet/minecraft/client/renderer/fog/FogData;)V", at = @At("HEAD"))
	private void ecology$markCameraUnderwater(FogData fog, CallbackInfo ci) {
		Minecraft client = Minecraft.getInstance();
		if (client != null
			&& client.gameRenderer != null
			&& client.gameRenderer.mainCamera().getFluidInCamera() == FogType.WATER) {
			fog.cloudEnd = ECOLOGY_UNDERWATER_CLOUD_END;
		}
	}
}
