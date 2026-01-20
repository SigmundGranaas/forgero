package com.sigmundgranaas.forgero.fabric.toolstats;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.state.Identifiable;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.fabric.resources.ForgeroResourceGenerator;

import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class ToolStatTagGenerator {
	public static void generateTags() {
		if (ForgeroStateRegistry.STATES == null) {
			return;
		}
		var tagBuilder = TagBuilder.items("toolstats:items/ignored");
		ForgeroStateRegistry.STATES.all().stream()
				.map(Supplier::get)
				.map(Identifiable::identifier)
				.map(Identifier::new)
				.map(Identifier::toString)
				.forEach(tagBuilder::add);
		ForgeroResourceGenerator.getDynamicPack().addTag(tagBuilder);
	}
}
