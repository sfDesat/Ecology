package com.midas.ecology;

import com.midas.ecology.worldgen.EcologyBiomes;
import com.midas.ecology.worldgen.feature.EcologyFeatures;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcologyMod implements ModInitializer {
	public static final String MOD_ID = "ecology";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		EcologyFeatures.register();
		LOGGER.info("Ecology initialized ({} habitat pockets, {} ocean biomes)", EcologyBiomes.POCKETS.size(), EcologyBiomes.ALL.size());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
