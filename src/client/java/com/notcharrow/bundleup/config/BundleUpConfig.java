package com.notcharrow.bundleup.config;

import java.util.List;

public class BundleUpConfig {
	public enum ItemListMode { WHITELIST, BLACKLIST }

	public ItemListMode itemListMode = ItemListMode.BLACKLIST;
	public List<String> items = List.of("minecraft:firework_rocket");
	public boolean[] slotBlacklist = new boolean[36];
	{
		// set hotbar blacklisted to true by default
		for (int i = 27; i < 36; i++) {
			slotBlacklist[i] = true;
		}
	}
}
