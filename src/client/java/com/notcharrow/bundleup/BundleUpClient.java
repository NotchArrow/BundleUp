package com.notcharrow.bundleup;

import com.notcharrow.bundleup.commands.CommandRegistry;
import com.notcharrow.bundleup.config.ConfigManager;
import com.notcharrow.bundleup.keybinds.BundleKeybind;
import com.notcharrow.bundleup.keybinds.KeybindRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class BundleUpClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeybindRegistry.registerKeybinds();
		BundleKeybind.register();

		ConfigManager.loadConfig();

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandRegistry.registerCommands(dispatcher);
		});
	}
}