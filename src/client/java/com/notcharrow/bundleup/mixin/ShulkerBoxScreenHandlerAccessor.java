package com.notcharrow.bundleup.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ShulkerBoxScreenHandler.class)
public interface ShulkerBoxScreenHandlerAccessor {
	@Accessor("inventory")
	Inventory getInventory();
}
