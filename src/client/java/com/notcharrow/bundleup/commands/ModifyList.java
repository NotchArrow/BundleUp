package com.notcharrow.bundleup.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.notcharrow.bundleup.config.BundleUpConfig;
import com.notcharrow.bundleup.config.ConfigManager;
import com.notcharrow.bundleup.helper.TextFormat;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ModifyList {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	public static LiteralArgumentBuilder<FabricClientCommandSource> registerCommand() {
		return literal("modifylist")
				.then(literal("add")
						.executes(ModifyList::executeAdd))
				.then(literal("list")
						.executes(ModifyList::executeList))
				.then(literal("remove")
						.executes(ModifyList::executeRemove));
	}

	private static int executeList(CommandContext<FabricClientCommandSource> context) {
		if (client.player != null) {
			List<String> configList = ConfigManager.config.items;
			Collections.sort(configList);
			if (ConfigManager.config.itemListMode.equals(BundleUpConfig.ItemListMode.WHITELIST)) {
				client.player.sendMessage(TextFormat.styledText("Current items in your whitelist:"), false);
			} else {
				client.player.sendMessage(TextFormat.styledText("Current items in your blacklist:"), false);
			}

			StringBuilder displayText = new StringBuilder();
			for (String itemID: configList) {
				String itemName = Registries.ITEM.get(Identifier.of(itemID)).getName().getString();
				displayText.append(itemName);
				if (!configList.getLast().equals(itemID)) {
					displayText.append(", ");
				} else {
					displayText.append(".");
				}
			}
			client.player.sendMessage(TextFormat.styledText(displayText.toString()), false);
		}

		return 1;
	}

	private static int executeAdd(CommandContext<FabricClientCommandSource> context) {
		if (client.player != null) {
			ItemStack stack = client.player.getMainHandStack();
			if (stack.getItem() == Items.AIR) {
				client.player.sendMessage(TextFormat.styledText("You aren't holding anything!"), false);
			} else {
				String itemID = String.valueOf(Registries.ITEM.getId(stack.getItem()));
				String itemName = stack.getItem().getName().getString();
				List<String> configList = ConfigManager.config.items;
				if (configList.contains(itemID)) {
					if (ConfigManager.config.itemListMode.equals(BundleUpConfig.ItemListMode.WHITELIST)) {
						client.player.sendMessage(TextFormat.styledText(itemName + " is already in your whitelist!"), false);
					} else {
						client.player.sendMessage(TextFormat.styledText(itemName + " is already in your blacklist!"), false);
					}
				} else {
					ConfigManager.config.items.add(itemID);
					ConfigManager.saveConfig();
					if (ConfigManager.config.itemListMode.equals(BundleUpConfig.ItemListMode.WHITELIST)) {
						client.player.sendMessage(TextFormat.styledText("Added " + itemName + " to your whitelist!"), false);
					} else {
						client.player.sendMessage(TextFormat.styledText("Added " + itemName + " to your blacklist!"), false);
					}
				}
			}
		}

		return 1;
	}

	private static int executeRemove(CommandContext<FabricClientCommandSource> context) {
		if (client.player != null) {
			ItemStack stack = client.player.getMainHandStack();
			if (stack.getItem() == Items.AIR) {
				client.player.sendMessage(TextFormat.styledText("You aren't holding anything!"), false);
			} else {
				String itemID = String.valueOf(Registries.ITEM.getId(stack.getItem()));
				String itemName = stack.getItem().getName().getString();
				List<String> configList = ConfigManager.config.items;
				if (!configList.contains(itemID)) {
					if (ConfigManager.config.itemListMode.equals(BundleUpConfig.ItemListMode.WHITELIST)) {
						client.player.sendMessage(TextFormat.styledText(itemName + " is not in your whitelist!"), false);
					} else {
						client.player.sendMessage(TextFormat.styledText(itemName + " is not in your blacklist!"), false);
					}
				} else {
					ConfigManager.config.items.remove(itemID);
					ConfigManager.saveConfig();
					if (ConfigManager.config.itemListMode.equals(BundleUpConfig.ItemListMode.WHITELIST)) {
						client.player.sendMessage(TextFormat.styledText("Removed " + itemName + " from your whitelist!"), false);
					} else {
						client.player.sendMessage(TextFormat.styledText("Removed " + itemName + " from your blacklist!"), false);
					}
				}
			}
		}

		return 1;
	}
}
