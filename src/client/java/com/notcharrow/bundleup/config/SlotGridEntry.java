package com.notcharrow.bundleup.config;

import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SlotGridEntry extends AbstractConfigEntry<boolean[]> {
	private final boolean[] slots;
	private final boolean[] defaultSlots;

	public SlotGridEntry(boolean[] slots) {
		this.slots = slots;
		this.defaultSlots = slots.clone(); // default = copy of initial
	}

	@Override
	public boolean[] getValue() {
		return slots;
	}

	@Override
	public boolean isRequiresRestart() {
		return false;
	}

	@Override
	public void setRequiresRestart(boolean b) {

	}

	@Override
	public Text getFieldName() {
		return null;
	}

	@Override
	public Optional<boolean[]> getDefaultValue() {
		return Optional.ofNullable(defaultSlots);
	}

	@Override
	public void save() {
		// cloth config calls this when applying changes,
		// we don’t need to do anything special since slots[] is live
	}

	@Override
	public void render(DrawContext drawContext, int index, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
		// simple grid of 36 slots (9 x 4)
		int size = 18; // slot square size
		for (int row = 0; row < 4; row++) {
			for (int col = 0; col < 9; col++) {
				int slotIndex = row * 9 + col;
				int sx = x + col * size;
				int sy = y + row * size;

				// background
				int color = slots[slotIndex] ? 0x8800FF00 : 0x88FF0000; // green=blacklisted, red=allowed
				drawContext.fill(sx, sy, sx + size, sy + size, color);

				// border
				drawContext.drawBorder(sx, sy, size, size, 0xFFFFFFFF);
			}
		}
	}

	@Override
	public List<? extends Element> children() {
		return Collections.emptyList();
	}

	@Override
	public List<? extends Selectable> narratables() {
		return Collections.emptyList();
	}
}