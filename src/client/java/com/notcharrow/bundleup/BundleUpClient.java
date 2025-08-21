package com.notcharrow.bundleup;

import com.notcharrow.bundleup.config.ConfigManager;
import com.notcharrow.bundleup.keybinds.BundleKeybind;
import com.notcharrow.bundleup.keybinds.KeybindRegistry;
import net.fabricmc.api.ClientModInitializer;

public class BundleUpClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeybindRegistry.registerKeybinds();
		BundleKeybind.register();

		ConfigManager.loadConfig();
	}
}