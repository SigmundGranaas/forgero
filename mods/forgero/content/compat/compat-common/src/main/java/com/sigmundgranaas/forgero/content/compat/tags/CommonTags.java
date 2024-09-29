package com.sigmundgranaas.forgero.content.compat.tags;

import java.util.List;
import java.util.function.Supplier;

public class CommonTags {
	private static final List<Supplier<CommonTagGenerator>> TAGS = List.of(
			BiomesWeveGone::new,
			BloomingNature::new,
			Create::new,
			Ecologics::new,
			Galosphere::new,
			BountifulFares::new,
			Meadow::new,
			ModernIndustrialization::new,
			MythicMetalsCommons::new,
			NaturesSpirit::new,
			TechReborn::new,
			RegionsUnexplored::new
	);

	public static void registerAndFilterCommonMaterialTags() {
		TAGS.stream()
		    .map(Supplier::get)
		    .filter(CommonTagGenerator::isModPresent)
		    .forEach(CommonTagGenerator::register);
	}
}
