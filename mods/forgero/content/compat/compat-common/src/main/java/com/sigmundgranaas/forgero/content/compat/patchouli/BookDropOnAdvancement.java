package com.sigmundgranaas.forgero.content.compat.patchouli;

import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.resources.DynamicResourcePackGenerator.RESOURCE_PACK;

public class BookDropOnAdvancement {
	private static final String Advancement = "{\"type\":\"advancement_reward\",\"pools\":[{\"rolls\":1,\"entries\":[{\"type\":\"item\",\"name\":\"modonomicon:modonomicon\",\"functions\":[{\"function\":\"set_nbt\",\"tag\":\"{\\\"modonomicon:book_id\\\":\\\"forgero:guidebook\\\"}\"}]}]}]}";

	public static void registerBookDrop() {
		var id = new Identifier("forgero:loot_tables/grant_book_on_advancement.json");

		RESOURCE_PACK.addData(id, Advancement.getBytes());
	}
}
