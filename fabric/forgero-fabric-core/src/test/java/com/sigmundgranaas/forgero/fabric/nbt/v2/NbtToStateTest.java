package com.sigmundgranaas.forgero.fabric.nbt.v2;

import static com.sigmundgranaas.forgero.fabric.nbt.NBTs.PICKAXE_NBT;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedState;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.StateParser;
import com.sigmundgranaas.forgero.testutil.Materials;
import com.sigmundgranaas.forgero.testutil.Schematics;
import com.sigmundgranaas.forgero.testutil.ToolParts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.NbtCompound;


public class NbtToStateTest {

	public static Optional<State> ingredientSupplier(String id) {
		return switch (id) {
			case "forgero:oak-handle" -> Optional.of(ToolParts.HANDLE);
			case "forgero:iron-pickaxe_head" -> Optional.of(ToolParts.PICKAXE_HEAD);
			case "c:iron" -> Optional.of(Materials.IRON);
			case "forgero:oak" -> Optional.of(Materials.OAK);
			case "forgero:pickaxe_head-schematic" -> Optional.of(Schematics.PICKAXE_HEAD_SCHEMATIC);
			case "forgero:handle-schematic" -> Optional.of(Schematics.HANDLE_SCHEMATIC);
			case "forgero:binding-schematic" -> Optional.of(Schematics.BINDING_SCHEMATIC);
			default -> Optional.empty();
		};
	}

	@BeforeEach
	void genData() {
		PipelineBuilder
				.builder()
				.register(FabricPackFinder.supplier())
				.state(ForgeroStateRegistry.stateListener())
				.state(ForgeroStateRegistry.compositeListener())
				.inflated(ForgeroStateRegistry.constructListener())
				.inflated(ForgeroStateRegistry.containerListener())
				.recipes(ForgeroStateRegistry.recipeListener())
				.build()
				.execute();
	}

	@Test
	void parseInvalidNbt() {
		Assertions.assertEquals(Optional.empty(), StateParser.STATE_PARSER.parse(new NbtCompound()));
	}

	@Test
	void parseSimplePickaxe() {
		Assertions.assertEquals("iron-pickaxe", StateParser.STATE_PARSER.parse(PICKAXE_NBT).map(State::name).orElse("invalid"));
	}

	@Test
	void parseSimplePickaxeWithIngredients() {
		List<State> ingredients = StateParser.STATE_PARSER.parse(PICKAXE_NBT).map(ConstructedState.class::cast).map(ConstructedState::parts).orElse(Collections.emptyList());
		Assertions.assertEquals(2, ingredients.size());
		Assertions.assertEquals("oak-handle", ingredients.get(0).name());
		Assertions.assertEquals("iron-pickaxe_head", ingredients.get(1).name());
	}


	@Test
	void assertPropertiesParsed() {
		var pickaxe = StateParser.STATE_PARSER.parse(PICKAXE_NBT).orElseThrow();
		Assertions.assertEquals(0, pickaxe.stream().applyAttribute(AttackDamage.KEY));
	}
}
