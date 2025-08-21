package com.notcharrow.bundleup.keybinds;

import com.notcharrow.bundleup.helper.BundleUpHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.sound.SoundEvents;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BundleKeybind {
	private static int previousSlot = -1;
	private static boolean pressed = false;

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

					// Sorting Mode
					if (Screen.hasShiftDown()) {
						for (Map.Entry<Integer, Integer> bundle : spaces.entrySet()) {
							int bundleSlot = bundle.getKey();
							int spaceLeft = spaces.get(bundleSlot);
							maps = BundleUpHelper.updateMaps(inventory);
							maps.removeFirst();
							items = maps.removeFirst(); // slot, itemStack
							spaceRequirements = BundleUpHelper.getSpaceRequirements(items); // slot, spaceRequirement

							Map<Item, Integer> bundleContents = BundleUpHelper.getBundleContents(bundles.get(bundleSlot));
							Set<Item> itemSet = bundleContents.keySet();

							for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
								int itemSlot = entry.getKey();
								if (itemSet.contains(entry.getValue().getItem())
									&& spaceRequirements.get(itemSlot) <= spaceLeft) {
									BundleUpHelper.clickSlot(itemSlot);
									BundleUpHelper.clickSlot(bundleSlot);
									spaceLeft -= spaceRequirements.get(itemSlot);
								}
							}
						}
					}

					// Normal Mode
					while (BundleUpHelper.getTotalSpace(spaces) > 0 && !spaceRequirements.isEmpty() && !Screen.hasShiftDown()) {

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