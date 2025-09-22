package com.notcharrow.bundleup.keybinds;

import com.notcharrow.bundleup.config.BundleUpConfig;
import com.notcharrow.bundleup.config.ConfigManager;
import com.notcharrow.bundleup.helper.BundleUpHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.sound.SoundEvents;

import java.util.*;

public class BundleKeybind {
	private static int previousSlot = -1;
	private static boolean pressed = false;
	private static boolean sorting = false;

	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && client.interactionManager != null
					&& (client.currentScreen instanceof InventoryScreen
						|| client.player.currentScreenHandler instanceof GenericContainerScreenHandler
						|| client.player.currentScreenHandler instanceof ShulkerBoxScreenHandler)) {
				if (InputUtil.isKeyPressed(client.getWindow().getHandle(),
						KeyBindingHelper.getBoundKeyOf(KeybindRegistry.bundleKeybind).getCode())
						&& !pressed) {
					pressed = true;
					client.player.playSound(SoundEvents.ITEM_BUNDLE_INSERT);

					PlayerInventory inventory = client.player.getInventory();

					List<Map<Integer, ItemStack>> maps = BundleUpHelper.updateMaps(inventory);
					Map<Integer, ItemStack> bundles = maps.removeFirst(); // slot, bundleStack
					Map<Integer, ItemStack> items = maps.removeFirst(); // slot, itemStack
					Map<Integer, Integer> spaces = BundleUpHelper.getSpaces(bundles); // slot, bundleSpace
					Map<Integer, Integer> spaceRequirements = BundleUpHelper.getSpaceRequirements(items); // slot, spaceRequirement

					if (Screen.hasShiftDown()) {
						sorting = ConfigManager.config.bundleSortMode == BundleUpConfig.BundleSortMode.NORMAL;
					} else {
						sorting = ConfigManager.config.bundleSortMode != BundleUpConfig.BundleSortMode.NORMAL;
					}

					// Sorting Mode
					if (sorting) {
						List<Map.Entry<Integer, Integer>> spaceRequirementsList =
								spaceRequirements.entrySet().stream()
										.sorted(Map.Entry.comparingByValue())
										.toList();

						for (Map.Entry<Integer, Integer> itemEntry: spaceRequirementsList) {

							maps = BundleUpHelper.updateMaps(inventory);
							bundles = maps.removeFirst(); // slot, bundleStack
							spaces = BundleUpHelper.getSpaces(bundles); // slot, bundleSpace

							int itemSlot = itemEntry.getKey();
							ItemStack itemStack = items.get(itemSlot);
							int spacePerItem = BundleUpHelper.getSpacePerItem(itemStack);
							int spaceRequirement = itemEntry.getValue();

							List<Integer> suitableSpaces = new ArrayList<>();
							int totalSpace = 0;
							for (Map.Entry<Integer, Integer> bundleEntry : spaces.entrySet()) {
								ItemStack bundle = bundles.get(bundleEntry.getKey());
								if (BundleUpHelper.getBundleContents(bundle).containsKey(itemStack.getItem())) {
									suitableSpaces.add(bundleEntry.getKey());
									totalSpace += bundleEntry.getValue();
								}
							}

							if (totalSpace > spaceRequirement) {
								BundleUpHelper.clickSlot(itemSlot);
								for (int bundleSlot: suitableSpaces) {
									if (spaceRequirement > 0) {
										BundleUpHelper.clickSlot(bundleSlot);
									}
									spaceRequirement -= (spaces.get(bundleSlot) / spacePerItem) * spacePerItem;
								}
							}
						}
					}

					// Normal Mode
					while (BundleUpHelper.getTotalSpace(spaces) > 0 && !spaceRequirements.isEmpty()
							&& !sorting) {

						maps = BundleUpHelper.updateMaps(inventory);
						bundles = maps.removeFirst(); // slot, bundleStack
						items = maps.removeFirst(); // slot, itemStack
						spaces = BundleUpHelper.getSpaces(bundles); // slot, bundleSpace
						spaceRequirements = BundleUpHelper.getSpaceRequirements(items); // slot, spaceRequirement

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

							int lowestSpaceReq = entryWithLowestValue.get().getValue();
							int spacePerItem = BundleUpHelper.getSpacePerItem(items.get(slot));

							if (spacePerItem == 64) {
								break;
							}

							if (BundleUpHelper.getTotalSpace(spaces) >= lowestSpaceReq) {
								BundleUpHelper.clickSlot(slot);
								for (Map.Entry<Integer, Integer> entry : spaces.entrySet()) {
									if (entry.getValue() >= spacePerItem && lowestSpaceReq > 0) {
										BundleUpHelper.clickSlot(entry.getKey());
										int spaceUsed = (entry.getValue() / spacePerItem) * spacePerItem;
										lowestSpaceReq -= spaceUsed;
									}
								}
							}
						}
					}
				}
				else if (!InputUtil.isKeyPressed(client.getWindow().getHandle(),
						KeyBindingHelper.getBoundKeyOf(KeybindRegistry.bundleKeybind).getCode())) {
					pressed = false;
				}
			}
		});
	}
}