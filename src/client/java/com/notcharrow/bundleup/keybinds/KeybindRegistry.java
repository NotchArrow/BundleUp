package com.notcharrow.bundleup.keybinds;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class KeybindRegistry {
	public static KeyBinding bundleKeybind;

	public static void registerKeybinds() {
		bundleKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.bundle-up.bundleKeybind",
				InputUtil.GLFW_KEY_B,
				KeyBinding.Category.create(Identifier.of("bundle-up"))));
	}
}
