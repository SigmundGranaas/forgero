package com.sigmundgranaas.forgero.content.compat.toolstats;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

import java.util.function.Supplier;

import static com.sigmundgranaas.forgero.resources.ARRPGenerator.RESOURCE_PACK;

public class ToolStatTagGenerator {
	public static void generateTags() {
		if (ForgeroStateRegistry.STATES == null) {
			return;
		}
		var tag = new JTag();
		ForgeroStateRegistry.STATES.all().stream()
		                           .map(Supplier::get)
		                           .map(Identifiable::identifier)
		                           .map(Identifier::new)
		                           .forEach(tag::add);
		RESOURCE_PACK.addTag(new Identifier("toolstats", "items/ignored"), tag);
	}
}
