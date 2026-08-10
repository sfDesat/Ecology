package com.midas.ecology.client;

import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.config.EcologyConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EcologyModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (!FabricLoader.getInstance().isModLoaded("cloth-config")) {
			EcologyMod.LOGGER.warn("Mod Menu opened Ecology config but cloth-config is not loaded");
			return parent -> new MissingClothConfigScreen(parent);
		}
		return EcologyConfigScreen::create;
	}

	private static final class MissingClothConfigScreen extends Screen {
		private final Screen parent;

		private MissingClothConfigScreen(Screen parent) {
			super(Component.literal("Ecology Config"));
			this.parent = parent;
		}

		@Override
		protected void init() {
			this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(
				Component.literal("Cloth Config is required for this screen"),
				button -> this.minecraft.gui.setScreen(this.parent)
			).bounds(this.width / 2 - 120, this.height / 2 - 10, 240, 20).build());
		}
	}
}
