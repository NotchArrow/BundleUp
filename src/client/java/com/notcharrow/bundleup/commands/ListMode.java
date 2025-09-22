package com.notcharrow.bundleup.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.notcharrow.bundleup.config.BundleUpConfig;
import com.notcharrow.bundleup.config.ConfigManager;
import com.notcharrow.bundleup.helper.TextFormat;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ListMode {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	public static LiteralArgumentBuilder<FabricClientCommandSource> registerCommand() {
		return literal("listmode")
				.executes(ListMode::execute);
	}

	private static int execute(CommandContext<FabricClientCommandSource> context) {
		if (client.player != null) {
			if (ConfigManager.config.itemListMode.equals(BundleUpConfig.ItemListMode.WHITELIST)) {
				client.player.sendMessage(TextFormat.styledText("Set the list mode to Blacklist!"), false);
				ConfigManager.config.itemListMode = BundleUpConfig.ItemListMode.BLACKLIST;
			} else {
				client.player.sendMessage(TextFormat.styledText("Set the list mode to Whitelist!"), false);
				ConfigManager.config.itemListMode = BundleUpConfig.ItemListMode.WHITELIST;
			}
			ConfigManager.saveConfig();
		}

		return 1;
	}
}
