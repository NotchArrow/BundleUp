package com.notcharrow.bundleup.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CommandRegistry {
	public static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		dispatcher.register(
				literal("bundleup")
						.then(ModifyList.registerCommand())
						.then(ListMode.registerCommand())
						.then(SortMode.registerCommand())
		);
	}
}
