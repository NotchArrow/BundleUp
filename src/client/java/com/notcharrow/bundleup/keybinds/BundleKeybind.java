package com.notcharrow.bundleup.keybinds;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BundleKeybind {
	private static final Map<Integer, Integer> bundleSlots = new HashMap<>();
	private static final Map<Integer, Integer> spaceRequirements = new HashMap<>();
	private static int previousSlot = -1;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && client.interactionManager != null
					&& client.currentScreen instanceof InventoryScreen) {
				if (InputUtil.isKeyPressed(client.getWindow().getHandle(),
						KeyBindingHelper.getBoundKeyOf(KeybindRegistry.bundleKeybind).getCode())) {

					bundleSlots.clear();
					spaceRequirements.clear();

					PlayerInventory inventory = client.player.getInventory();
					for (int slot = 0; slot < inventory.size(); slot++) {
						ItemStack stack = inventory.getStack(slot);
						if (!stack.isEmpty()) {
							if (stack.getItem() instanceof BundleItem) {
								bundleSlots.put(slot, (int) (64 - BundleItem.getAmountFilled(stack)));
							} else {
								if (getSpacePerItem(inventory.getStack(slot)) != 64 && slot >= 9) {
									spaceRequirements.put(slot, getSpaceTaken(stack));
								}
							}
						}
					}

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
									bundleSlots.put(entry.getKey(), entry.getValue() - lowestValue);
									spaceRequirements.remove(slot);

									bundledSomething = true;
									break;
								}
							}

							if (!bundledSomething) {
								break;
							}
						}
					}
					previousSlot = -1;
				}
			}
		});
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
		return bundleSlots.values().stream().mapToInt(Integer::intValue).sum();
	}

	private static void storeItems(int itemSlot, int bundleSlot) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null && client.interactionManager != null) {
			if (itemSlot < 9) {
				itemSlot += 36;
			}
			if (bundleSlot < 9) {
				bundleSlot += 36;
			}
			client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, itemSlot, 0, SlotActionType.PICKUP, client.player);
			client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, bundleSlot, 0, SlotActionType.PICKUP, client.player);
		}
	}
}