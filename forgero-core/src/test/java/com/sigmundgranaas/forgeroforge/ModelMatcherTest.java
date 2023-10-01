package com.sigmundgranaas.forgeroforge;

import static com.sigmundgranaas.forgeroforge.TypeTreeTest.createDataList;
import static com.sigmundgranaas.forgeroforge.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgeroforge.testutil.Tools.IRON_PICKAXE;
import static com.sigmundgranaas.forgeroforge.testutil.Upgrades.BINDING;
import static com.sigmundgranaas.forgeroforge.testutil.Upgrades.LEATHER;

import java.util.List;

import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.core.model.CompositeModelTemplate;
import com.sigmundgranaas.forgero.core.model.ModelRegistry;
import com.sigmundgranaas.forgero.core.model.match.PredicateFactory;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringIdentifierBuilder;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringModelBuilder;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringNameBuilder;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringSlotBuilder;
import com.sigmundgranaas.forgero.core.model.match.builders.string.StringTypeBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.DataResource;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.ModelData;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.PaletteData;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModelMatcherTest {
	public static TypeTree loadedTypeTree() {
		var tree = new TypeTree();
		createDataList().forEach(tree::addNode);
		tree.resolve();
		return tree;
	}

	@BeforeEach
	void setupModelHandlers() {
		PredicateFactory.register(new StringModelBuilder());
		PredicateFactory.register(new StringIdentifierBuilder());
		PredicateFactory.register(new StringModelBuilder());
		PredicateFactory.register(new StringSlotBuilder());
		PredicateFactory.register(new StringTypeBuilder());
		PredicateFactory.register(new StringNameBuilder());
	}

	@Test
	void getProperModel() {
		var registry = new ModelRegistry();
		PipeLineTest.defaultResourcePipeLineTest()
				.data(registry.paletteListener())
				.data(registry.modelListener())
				.build().execute();
		var pickaxe = IRON_PICKAXE;
		var pickaxeModel = registry.find(pickaxe, MatchContext.of());
		Assertions.assertTrue(pickaxeModel.isPresent());
		Assertions.assertEquals(2, ((CompositeModelTemplate) pickaxeModel.get().getTemplate()).getModels().size());
	}

	@Test
	void getHandleModel() {
		var tree = loadedTypeTree();
		var oakPalette = PaletteData.builder().name("oak").target("oak").build();
		var ironPalette = PaletteData.builder().name("iron").target("iron").build();
		tree.find("WOOD").ifPresent(node -> node.addResource(oakPalette, PaletteData.class));
		tree.find("METAL").ifPresent(node -> node.addResource(ironPalette, PaletteData.class));
		var handle = HANDLE;
		var handleModelData = ModelData.builder().predicate(List.of(new JsonPrimitive("type:MATERIAL"), new JsonPrimitive("type:HANDLE_SCHEMATIC"))).modelType("BASED_COMPOSITE").template("default_handle").build();
		var handleVariantData = ModelData.builder().name("default_handle").modelType("GENERATE").palette("MATERIAL").template("handle.png").order(20).build();
		var registry = new ModelRegistry(tree);
		registry.register(DataResource.builder().type("HANDLE").models(List.of(handleModelData, handleVariantData)).build());
		var model = registry.find(handle, MatchContext.of());
		//TODO fix dependencies
		//Assertions.assertTrue(model.isPresent());
	}

	@Test
	void getModelFromPipeLine() {
		var registry = new ModelRegistry();
		PipeLineTest.defaultResourcePipeLineTest().data(registry.paletteListener()).data(registry.modelListener()).build().execute();
		var handle = HANDLE;
		var pickaxe = IRON_PICKAXE;
		var model = registry.find(handle);
		Assertions.assertTrue(model.isPresent());
		var pickaxeModel = registry.find(pickaxe);
		Assertions.assertTrue(pickaxeModel.isPresent());
	}

	@Test
	void getModelWithUpgrade() {
		var registry = new ModelRegistry();
		PipeLineTest.defaultResourcePipeLineTest().data(registry.paletteListener()).data(registry.modelListener()).build().execute();
		var pickaxe = IRON_PICKAXE.upgrade(BINDING);
		var pickaxeModel = registry.find(pickaxe, MatchContext.of());
		Assertions.assertTrue(pickaxeModel.isPresent());
		Assertions.assertEquals(3, ((CompositeModelTemplate) pickaxeModel.get().getTemplate()).getModels().size());
	}

	@Test
	void getHandleModelWithUpgrade() {
		var registry = new ModelRegistry();
		PipeLineTest.defaultResourcePipeLineTest().data(registry.paletteListener()).data(registry.modelListener()).build().execute();
		var handle = HANDLE.upgrade(LEATHER);
		var handleModel = registry.find(handle, MatchContext.of());
		Assertions.assertTrue(handleModel.isPresent());
		//Assertions.assertEquals(2, ((CompositeModelTemplate) handleModel.get()).getModels().size());
	}

}
