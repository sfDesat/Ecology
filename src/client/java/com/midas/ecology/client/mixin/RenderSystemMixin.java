package com.midas.ecology.client.mixin;

import com.midas.ecology.client.render.fog.FogTintMatrices;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
	@Inject(method = "bindDefaultUniforms", at = @At("TAIL"))
	private static void ecology$bindFogTintMatrices(RenderPass renderPass, CallbackInfo ci) {
		FogTintMatrices.bindIfTransparencyPass(renderPass);
	}
}
