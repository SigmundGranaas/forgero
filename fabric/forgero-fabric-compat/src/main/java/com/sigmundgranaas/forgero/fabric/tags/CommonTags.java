package com.sigmundgranaas.forgero.fabric.tags;

import java.util.List;
import java.util.function.Supplier;

public class CommonTags {
	private static final List<Supplier<CommonTagGenerator>> TAGS = List.of(
			BiomesWeveGone::new,
			BloomingNature::new,
			Create::new,
			Galosphere::new,
			Meadow::new,
			ModernIndustrialization::new,
			MythicMetalsCommons::new,
			NaturesSpirit::new,
			TechReborn::new,
			RegionsUnexplored::new
	);

	public static void registerAndFilterCommonMaterialTags(){
		TAGS.stream()
				.map(Supplier::get)
				.filter(CommonTagGenerator::isModLoaded)
				.forEach(CommonTagGenerator::register);
	}
}
