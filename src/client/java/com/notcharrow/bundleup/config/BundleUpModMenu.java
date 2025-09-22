package com.notcharrow.bundleup.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class BundleUpModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			ConfigBuilder builder = ConfigBuilder.create()
					.setTransparentBackground(false)
					.setParentScreen(parent)
					.setTitle(Text.literal("Bundle Up Config"))
					.setSavingRunnable(ConfigManager::saveConfig);

			ConfigEntryBuilder entryBuilder = builder.entryBuilder();

			// general category
			ConfigCategory general = builder.getOrCreateCategory(Text.literal("Item List"));

			// mode selector (whitelist/blacklist)
			general.addEntry(entryBuilder.startEnumSelector(Text.literal("Item List Mode"),
							BundleUpConfig.ItemListMode.class, ConfigManager.config.itemListMode)
					.setSaveConsumer(newValue -> ConfigManager.config.itemListMode = newValue)
					.build());

			// mode selector (normal/sort)
			general.addEntry(entryBuilder.startEnumSelector(Text.literal("Bundle Default Behavior"),
							BundleUpConfig.BundleSortMode.class, ConfigManager.config.bundleSortMode)
					.setSaveConsumer(newValue -> ConfigManager.config.bundleSortMode = newValue)
					.build());

			// item list (string list)
			general.addEntry(entryBuilder.startStrList(Text.literal("Item ID List"), ConfigManager.config.items)
					.setDefaultValue(List.of())
					.setExpanded(true)
					.setInsertInFront(true)
					.setSaveConsumer(newList -> ConfigManager.config.items = new ArrayList<>(newList))
					.build());

			ConfigCategory slots = builder.getOrCreateCategory(Text.literal("Slot Blacklist"));

			slots.addEntry(entryBuilder
					.startTextDescription(Text.literal("────── ✩ Hotbar ✩ ──────")
							.setStyle(Style.EMPTY.withBold(true).withColor(Formatting.GOLD)))
					.build()
			);

			for (int i = 27; i < 36; i++) {
				final int slot = i;
				slots.addEntry(entryBuilder
						.startBooleanToggle(
								Text.literal("Hotbar Slot " + ((slot % 9) + 1)),
								ConfigManager.config.slotBlacklist[slot]
						)
						.setSaveConsumer(newValue -> ConfigManager.config.slotBlacklist[slot] = newValue)
						.setTooltip(Text.literal("Should this slot be blacklisted from bundles pulling items"))
						.build()
				);
			}

			for (int row = 0; row < 3; row++) {
				slots.addEntry(entryBuilder
						.startTextDescription(Text.literal("────── ✩ Row " + (row + 1) + " ✩ ──────")
								.setStyle(Style.EMPTY.withBold(true).withColor(Formatting.GOLD)))
						.build()
				);
				for (int col = 0; col < 9; col++) {
					final int slot = row * 9 + col;
					slots.addEntry(entryBuilder
							.startBooleanToggle(
									Text.literal("Row " + (row + 1) + " | Slot " + (col + 1)),
									ConfigManager.config.slotBlacklist[slot]
							)
							.setSaveConsumer(newValue -> ConfigManager.config.slotBlacklist[slot] = newValue)
							.setTooltip(Text.literal("Should this slot be blacklisted from bundles pulling items"))
							.build()
					);
				}
			}

			return builder.build();
		};
	}
}