package com.notcharrow.bundleup.keybinds;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BundleKeybind {
	private static final Map<Integer, Integer> bundleSlots = new HashMap<>();
	private static final Map<Integer, Integer> spaceRequirements = new HashMap<>();
	private static int previousSlot = -1;
	private static boolean pressed = false;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && client.interactionManager != null
					&& (client.currentScreen instanceof InventoryScreen
						|| client.player.currentScreenHandler instanceof GenericContainerScreenHandler)) {
				if (InputUtil.isKeyPressed(client.getWindow().getHandle(),
						KeyBindingHelper.getBoundKeyOf(KeybindRegistry.bundleKeybind).getCode())
				&& !pressed) {
					pressed = true;

					updateMaps();

					while (totalSlotsLeft() > 0 && !spaceRequirements.isEmpty()) {
						Optional<Map.Entry<Integer, Integer>> entryWithLowestValue = spaceRequirements.entrySet()
								.stream()
								.min(Map.Entry.comparingByValue());

						if (entryWithLowestValue.isPresent()) {
							int slot = entryWithLowestValue.get().getKey();
							if (slot == previousSlot) {
								break;
							} else {
								previousSlot = slot;
							}
							int lowestValue = entryWithLowestValue.get().getValue();

							boolean bundledSomething = false;
							for (Map.Entry<Integer, Integer> entry : bundleSlots.entrySet()) {
								if (entry.getValue() >= lowestValue) {
									storeItems(slot, entry.getKey());
									bundledSomething = true;
									break;
								}
							}

							if (!bundledSomething) {
								if (totalSlotsLeft() >= lowestValue) {
									if (!spreadItems(slot)) {
										break;
									}
								}
							}
						}
					}
					previousSlot = -1;
				}
				else if (!InputUtil.isKeyPressed(client.getWindow().getHandle(),
						KeyBindingHelper.getBoundKeyOf(KeybindRegistry.bundleKeybind).getCode())) {
					pressed = false;
				}
			}
		});
	}

	private static void updateMaps() {
		bundleSlots.clear();
		spaceRequirements.clear();

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			PlayerInventory inventory = client.player.getInventory();
			if (client.currentScreen instanceof InventoryScreen) {
				for (int slot = 0; slot < inventory.size(); slot++) {
					ItemStack stack = inventory.getStack(slot);
					if (!stack.isEmpty()) {
						if (stack.getItem() instanceof BundleItem) {
							bundleSlots.put(slot, (int) (64 - (BundleItem.getAmountFilled(stack) * 64)));
						} else if (getSpacePerItem(inventory.getStack(slot)) != 64 && slot >= 9) {
								spaceRequirements.put(slot, getSpaceTaken(stack));
						}
					}
				}
			} else if (client.player.currentScreenHandler instanceof GenericContainerScreenHandler containerHandler) {
				Inventory chestInventory = containerHandler.getInventory();

				for (int slot = 0; slot < chestInventory.size(); slot++) {
					ItemStack stack = chestInventory.getStack(slot);
					if (!stack.isEmpty()) {
						if (stack.getItem() instanceof BundleItem) {
							bundleSlots.put(slot, (int) (64 - (BundleItem.getAmountFilled(stack) * 64)));
						} else if (getSpacePerItem(chestInventory.getStack(slot)) != 64) {
							spaceRequirements.put(slot, getSpaceTaken(stack));
						}
					}
				}
				for (int slot = 0; slot < inventory.size(); slot++) {
					ItemStack stack = inventory.getStack(slot);
					if (!stack.isEmpty()) {
						if (stack.getItem() instanceof BundleItem) {
							if (slot < 9) {
								bundleSlots.put(slot + chestInventory.size() + 27, (int) (64 - (BundleItem.getAmountFilled(stack) * 64)));
							} else {
								bundleSlots.put(slot + chestInventory.size() - 9, (int) (64 - (BundleItem.getAmountFilled(stack) * 64)));
							}
						} else if (getSpacePerItem(inventory.getStack(slot)) != 64 && slot >= 9) {
							spaceRequirements.put(slot + chestInventory.size() - 9, getSpaceTaken(stack));
						}
					}
				}
			}
		}
	}

	private static int getSpaceTaken(ItemStack stack) {
		int count = stack.getCount();
		int maxStackSize = stack.getMaxCount();
		return (count * (64 / maxStackSize));
	}

	private static int getSpacePerItem(ItemStack stack) {
		int maxStackSize = stack.getMaxCount();
		return (64 / maxStackSize);
	}

	private static int totalSlotsLeft() {
		updateMaps();
		return bundleSlots.values().stream().mapToInt(Integer::intValue).sum();
	}

	private static void storeItems(int itemSlot, int bundleSlot) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null && client.interactionManager != null) {

			client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, client.player);
			client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, bundleSlot, 0, SlotActionType.PICKUP, client.player);
		}
	}

	private static boolean spreadItems(int itemSlot) {
		boolean bundledItems = false;

		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null && client.interactionManager != null) {

			int spacePerItem;
			PlayerInventory inventory = client.player.getInventory();
			if (client.player.currentScreenHandler instanceof GenericContainerScreenHandler containerHandler) {
				Inventory chestInventory = containerHandler.getInventory();
				if (itemSlot < chestInventory.size()) {
					spacePerItem = getSpacePerItem(chestInventory.getStack(itemSlot));
				} else {
					spacePerItem = getSpacePerItem(inventory.getStack(itemSlot - chestInventory.size() + 9));
				}
			} else {
				spacePerItem = getSpacePerItem(inventory.getStack(itemSlot));
			}

			client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, itemSlot,
					0, SlotActionType.PICKUP, client.player);
			for (Map.Entry<Integer, Integer> entry : bundleSlots.entrySet()) {
				if (entry.getValue() >= spacePerItem) {
					int bundleSlot = entry.getKey();
					client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, bundleSlot,
							0, SlotActionType.PICKUP, client.player);
					bundledItems = true;
				}
			}
		}
		return bundledItems;
	}
}