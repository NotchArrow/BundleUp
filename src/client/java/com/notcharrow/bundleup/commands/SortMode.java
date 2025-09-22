package com.notcharrow.bundleup.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.notcharrow.bundleup.config.BundleUpConfig;
import com.notcharrow.bundleup.config.ConfigManager;
import com.notcharrow.bundleup.helper.TextFormat;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class SortMode {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	public static LiteralArgumentBuilder<FabricClientCommandSource> registerCommand() {
		return literal("sortmode")
				.executes(SortMode::execute);
	}

	private static int execute(CommandContext<FabricClientCommandSource> context) {
		if (client.player != null) {
			if (ConfigManager.config.bundleSortMode.equals(BundleUpConfig.BundleSortMode.NORMAL)) {
				client.player.sendMessage(TextFormat.styledText("Set the default behavior to sorting!"), true);
				ConfigManager.config.bundleSortMode = BundleUpConfig.BundleSortMode.SORT;
			} else {
				client.player.sendMessage(TextFormat.styledText("Set the default behavior to normal!"), true);
				ConfigManager.config.bundleSortMode = BundleUpConfig.BundleSortMode.NORMAL;
			}
			ConfigManager.saveConfig();
		}

		return 1;
	}
}
