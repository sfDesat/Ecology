package com.midas.ecology.client;

import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.config.EcologyClientConfig;
import com.midas.ecology.client.render.WaterSurfaceShaderSupport;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

public class EcologyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EcologyClientConfig.ensureLoaded();
		// Write settings pack before/with the normal resource load — do not force an extra reload.
		WaterSurfaceShaderSupport.applyConfigQuiet();
		ClientTickEvents.END_CLIENT_TICK.register(client -> WaterSurfaceShaderSupport.clientTick());
		EcologyMod.LOGGER.info(
			"Ecology client ready. cloth-config={} modmenu={} | {}",
			FabricLoader.getInstance().isModLoaded("cloth-config"),
			FabricLoader.getInstance().isModLoaded("modmenu"),
			WaterSurfaceShaderSupport.statusSummary()
		);
	}
}
