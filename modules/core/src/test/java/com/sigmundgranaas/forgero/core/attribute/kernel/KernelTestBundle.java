package com.sigmundgranaas.forgero.core.attribute.kernel;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.engine.TagGraph;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.utility.resource.loader.api.ResourceProvider;
import com.sigmundgranaas.forgero.utility.resource.loader.implementation.ClassPathResourceProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Loads the core test-resource data set through the real pipeline (mirrors ForgeroDataInitializerTest). */
final class KernelTestBundle {
	private static ForgeroDataBundle cached;

	private KernelTestBundle() {
	}

	static synchronized ForgeroDataBundle bundle() {
		if (cached != null) {
			return cached;
		}
		IdentifierFactory idFactory = new IdentifierFactory.Builder().defaultNamespace("forgero").build();
		var tagMap = new HashMap<OpenIdentifier, Set<OpenIdentifier>>();
		for (String tag : new String[]{
				"forgero:armor_plate_shape", "forgero:pickaxe_head_shape", "forgero:default_pickaxe_head",
				"forgero:default_armor_plate", "forgero:base_shape", "forgero:mastercrafted",
				"forgero:default_handle", "forgero:handle"}) {
			tagMap.put(idFactory.of(tag), new HashSet<>());
		}
		for (String tag : new String[]{
				"forgero:materials/tool_material", "forgero:materials/metal", "forgero:materials/armor_material",
				"forgero:parts/handle_type", "forgero:parts/pickaxe_head_type", "forgero:parts/armor_plate_type",
				"forgero:tools/pickaxe", "forgero:armor/chest_plate"}) {
			tagMap.put(OpenIdentifier.parse(tag), new HashSet<>());
		}
		var tagGraph = new TagGraph(tagMap);
		ForgeroDataInitializer.Config config = new ForgeroDataInitializer.Config(
				"forgero", provider(), tagGraph, new HashMap<>(), new HashMap<>(), new HashMap<>());
		cached = new ForgeroDataInitializer(config).getDataBundle();
		return cached;
	}

	private static ResourceProvider provider() {
		try {
			Path dataPath = Paths.get(Objects.requireNonNull(KernelTestBundle.class.getResource("/data/")).toURI());
			return new ClassPathResourceProvider(dataPath);
		} catch (Exception e) {
			throw new RuntimeException("Could not create test resource provider", e);
		}
	}
}
