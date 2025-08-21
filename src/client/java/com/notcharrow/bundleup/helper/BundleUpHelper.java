package com.notcharrow.bundleup.helper;

import com.notcharrow.bundleup.config.BundleUpConfig;
import com.notcharrow.bundleup.config.ConfigManager;
import com.notcharrow.bundleup.mixin.ShulkerBoxScreenHandlerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.*;

public class BundleUpHelper {
	public static Map<Item, Integer> getBundleContents(ItemStack bundleStack) {
		Map<Item, Integer> bundleContents = new HashMap<>();

		if (bundleStack.isIn(ItemTags.BUNDLES))	{
			if (bundleStack.get(DataComponentTypes.BUNDLE_CONTENTS) != null) {
				for (ItemStack stack : bundleStack.get(DataComponentTypes.BUNDLE_CONTENTS).iterate()) {
					bundleContents.put(stack.getItem(), stack.getCount());
				}
			}
		}
		return bundleContents;
	}

	public static int getRemainingSpace(ItemStack bundleStack) {
		if (bundleStack.isIn(ItemTags.BUNDLES))	{
			int amountFilled = (int) (BundleItem.getAmountFilled(bundleStack) * 64);
			return 64 - amountFilled;
		}
		return 0;
	}

	public static int getTotalSpace(Map<Integer, Integer> spaces) {
		return spaces.values().stream().mapToInt(Integer::intValue).sum();
	}

	public static int getSpacePerItem(ItemStack stack) {
		int maxStackSize = stack.getMaxCount();
		return (64 / maxStackSize);
	}

	public static List<Map<Integer, ItemStack>> updateMaps(PlayerInventory inventory) {
		Map<Integer, ItemStack> bundles = new HashMap<>();
		Map<Integer, ItemStack> items = new HashMap<>();
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			Screen screen = client.currentScreen;
			ScreenHandler screenHandler = client.player.currentScreenHandler;
			int containerSize = 0;

			Inventory containerInventory = null;
			if (screenHandler instanceof GenericContainerScreenHandler containerScreenHandler) {
				containerInventory = containerScreenHandler.getInventory();
			} else if (screenHandler instanceof ShulkerBoxScreenHandler containerScreenHandler){
				containerInventory = ((ShulkerBoxScreenHandlerAccessor) containerScreenHandler).getInventory();
			}

			if (containerInventory != null) {
				containerSize = containerInventory.size();
				for (int slot = 0; slot < containerSize; slot++) {
					ItemStack stack = containerInventory.getStack(slot);

					if (stack.isIn(ItemTags.BUNDLES)) {
						bundles.put(slot, stack);
					} else if (!stack.isEmpty() && getSpacePerItem(stack) < 64) {
						items.put(slot, stack);
					}
				}
			}

			for (int slot = 0; slot < inventory.size(); slot++) {
				ItemStack stack = inventory.getStack(slot);
				int realSlot = convertSlotID(slot, screen, containerSize);

				if (stack.isIn(ItemTags.BUNDLES)) {
					bundles.put(realSlot, stack);
				} else if (!stack.isEmpty() && getSpacePerItem(stack) < 64) {
					items.put(realSlot, stack);
				}
			}
			items = purgeItemsMap(items, containerSize);
		}

		return new ArrayList<>(Arrays.asList(bundles, items));
	}

	private static Map<Integer, ItemStack> purgeItemsMap(Map<Integer, ItemStack> items, int containerSize) {
		Map<Integer, ItemStack> newItemsList = new HashMap<>();

		int indexShift = 9;
		if (containerSize > 0) {
			indexShift = containerSize;
		}

		for (Map.Entry<Integer, ItemStack> entry: items.entrySet()) {

			ItemStack stack = entry.getValue();
			String itemID = Registries.ITEM.getId(stack.getItem()).toString();
			if (entry.getKey() < containerSize || !ConfigManager.config.slotBlacklist[entry.getKey() - indexShift]) {
				if (ConfigManager.config.itemListMode == BundleUpConfig.ItemListMode.WHITELIST) {
					if (ConfigManager.config.items.contains(itemID)) {
						newItemsList.put(entry.getKey(), entry.getValue());
					}
				} else {
					if (!ConfigManager.config.items.contains(itemID)) {
						newItemsList.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}

		return newItemsList;
	}

	public static Map<Integer, Integer> getSpaces(Map<Integer, ItemStack> items) {
		Map<Integer, Integer> spaces = new HashMap<>();

		for (Map.Entry<Integer, ItemStack> entry: items.entrySet()) {
			ItemStack stack = entry.getValue();
			int remainingSpace = getRemainingSpace(stack);
			spaces.put(entry.getKey(), remainingSpace);
		}
		return spaces;
	}

	public static Map<Integer, Integer> getSpaceRequirements(Map<Integer, ItemStack> items) {
		Map<Integer, Integer> spaceRequirements = new HashMap<>();

		for (Map.Entry<Integer, ItemStack> entry: items.entrySet()) {
			ItemStack stack = entry.getValue();
			int spaceRequirement = getSpacePerItem(stack) * stack.getCount();
			spaceRequirements.put(entry.getKey(), spaceRequirement);
		}
		return spaceRequirements;
	}

	private static int convertSlotID(int slot, Screen screen, int containerSize) {
		if (screen instanceof InventoryScreen) {
			if (slot < 9) {
				return slot + 36;
			} else {
				return slot;
			}
		} else if (slot < 9) {
			return slot + containerSize + 27;
		} else {
			return slot + containerSize - 9;
		}
	}

	public static void clickSlot(int slot) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null && client.interactionManager != null) {
			client.interactionManager.clickSlot(client.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, client.player);
		}
	}
}
