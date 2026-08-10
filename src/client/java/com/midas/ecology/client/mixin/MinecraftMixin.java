package com.midas.ecology.client.mixin;

import com.midas.ecology.client.render.WaterSurfaceShaderSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Arrays;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@ModifyArg(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/packs/repository/PackRepository;<init>([Lnet/minecraft/server/packs/repository/RepositorySource;)V"
		),
		index = 0
	)
	private RepositorySource[] ecology$addWaterSurfacePackSource(RepositorySource[] sources) {
		RepositorySource[] expanded = Arrays.copyOf(sources, sources.length + 1);
		expanded[sources.length] = WaterSurfaceShaderSupport.repositorySource();
		return expanded;
	}
}
