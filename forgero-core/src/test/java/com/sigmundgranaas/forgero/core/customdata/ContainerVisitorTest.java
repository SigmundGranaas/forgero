package com.sigmundgranaas.forgero.core.customdata;

import static com.sigmundgranaas.forgeroforge.testutil.Materials.IRON;

import java.util.Collections;
import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedSchematicPart;
import com.sigmundgranaas.forgero.core.type.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ContainerVisitorTest {

	@Test
	void stateCanContainPrimitives() {
		var map = new HashMap<String, JsonElement>();
		map.put("ingredient_count", new JsonPrimitive(1));
		State schematic = State.of("schematic", "forgero", Type.SCHEMATIC, Collections.emptyList(), new CustomJsonDataContainer(map));

		var result = schematic.accept(ContainerVisitor.VISITOR)
				.flatMap(container -> container.accept(new ClassBasedVisitor<>(Integer.class, "ingredient_count")));
		Assertions.assertTrue(result.isPresent());
		Assertions.assertEquals(1, result.get());
	}

	@Test
	void canUseContextedObjects() {
		var map = new HashMap<String, JsonElement>();
		var jsonObject = new JsonObject();
		jsonObject.addProperty("context", "LOCAL");
		jsonObject.addProperty("value", 2);
		map.put("ingredient_count", jsonObject);
		State schematic = State.of("schematic", "forgero", Type.SCHEMATIC, Collections.emptyList(), new CustomJsonDataContainer(map));

		var result = schematic.accept(ContainerVisitor.VISITOR)
				.flatMap(container -> container.accept(new ClassBasedVisitor<>(ContextAwareData.class, "ingredient_count")));
		Assertions.assertTrue(result.isPresent());
		Assertions.assertEquals(2.0, result.get().value());
	}

	@Test
	void localObjectsWillNotBeTransferred() {
		var map = new HashMap<String, JsonElement>();
		var jsonObject = new JsonObject();
		jsonObject.addProperty("context", "LOCAL");
		jsonObject.addProperty("value", 2);
		map.put("ingredient_count", jsonObject);
		State schematic = State.of("schematic", "forgero", Type.SCHEMATIC, Collections.emptyList(), new CustomJsonDataContainer(map));

		ConstructedSchematicPart part = new ConstructedSchematicPart.SchematicPartBuilder(schematic, IRON).build();


		var result = part.accept(ContainerVisitor.VISITOR)
				.flatMap(container -> container.accept(new ClassBasedVisitor<>(ContextAwareData.class, "ingredient_count")));
		Assertions.assertTrue(result.isEmpty());
	}
}
