package com.sigmundgranaas.forgero.quilt.patchouli;

import net.minecraft.util.Identifier;

import static com.sigmundgranaas.forgero.quilt.resources.ARRPGenerator.RESOURCE_PACK;

public class BookDropOnAdvancement {
	private static final String Advancement = "{\"type\":\"advancement_reward\",\"pools\":[{\"rolls\":1,\"entries\":[{\"type\":\"item\",\"name\":\"patchouli:guide_book\",\"functions\":[{\"function\":\"set_nbt\",\"tag\":\"{\\\"patchouli:book\\\":\\\"forgero:forgero_guide\\\"}\"}]}]}]}";

	public static void registerBookDrop() {
		var id = new Identifier("forgero:loot_tables/grant_book_on_advancement.json");

		RESOURCE_PACK.addData(id, Advancement.getBytes());
	}
}
