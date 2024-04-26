package com.sigmundgranaas.forgero.fabric.nbt.v2;

import static com.sigmundgranaas.forgero.core.type.Type.TOOL_PART_HEAD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.Durability;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.resource.data.v2.data.TypeData;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.composite.Constructed;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedComposite;
import com.sigmundgranaas.forgero.core.state.composite.ConstructedState;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.type.TypeTree;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompositeParser;
import com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.testutil.Materials;
import com.sigmundgranaas.forgero.testutil.ToolParts;
import com.sigmundgranaas.forgero.testutil.Tools;
import com.sigmundgranaas.forgero.testutil.Upgrades;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.NbtCompound;

public class StateNbtConversionTest {
	private static final CompoundEncoder<State> encoder = new CompositeEncoder();
	private static final CompositeParser parser = new CompositeParser(NbtToStateTest::ingredientSupplier);

	@BeforeAll
	public static void setup() {
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
	void encodeCompoundParseCompound() {
		NbtCompound compound = encoder.encode(Tools.IRON_PICKAXE.get());

		var pickaxe = parser.parse(compound).orElseThrow();
		Assertions.assertEquals("iron-pickaxe", pickaxe.name());
	}

	@Test
	void encodeCompoundParseCompoundWithIngredients() {
		NbtCompound compound = encoder.encode(Tools.IRON_PICKAXE.get());
		var pickaxe = parser.parse(compound).map(ConstructedState.class::cast).orElseThrow();
		Assertions.assertEquals(2, pickaxe.parts().size());
		Assertions.assertEquals("oak-handle", pickaxe.parts().get(1).name());
		Assertions.assertEquals("iron-pickaxe_head", pickaxe.parts().get(0).name());
	}

	@Test
	void encodeCompoundParseCompoundWithProperties() {
		NbtCompound compound = encoder.encode(Tools.IRON_PICKAXE.get().upgrade(Upgrades.BINDING));
		var pickaxe = parser.parse(compound).map(ConstructedState.class::cast).orElseThrow();
		Assertions.assertEquals(0, pickaxe.stream().applyAttribute(AttackDamage.KEY));
		Assertions.assertEquals(0, pickaxe.stream().applyAttribute(Durability.KEY));
	}

	@Test
	void encodeStateIntoNewToolFormat() {
		TypeTree tree = new TypeTree();
		tree.addNode(new TypeData(TOOL_PART_HEAD.typeName(), Optional.empty(), Collections.emptyList()));
		tree.addNode(new TypeData(Type.PICKAXE_HEAD.typeName(), Optional.of(TOOL_PART_HEAD.typeName()), Collections.emptyList()));

		ForgeroStateRegistry.TREE = tree;

		State head = ToolParts.PICKAXE_HEAD.upgrade(Materials.IRON);
		State handle = ToolParts.HANDLE.upgrade(Materials.IRON);
		ArrayList<Slot> slots = new ArrayList<>();
		slots.add(new EmptySlot(0, Type.BINDING, "", Set.of(Category.ALL)));

		State tool = ConstructedComposite.ConstructBuilder.builder()
				.addIngredient(head)
				.addIngredient(handle)
				.addSlotContainer(new SlotContainer(slots))
				.addUpgrade(ToolParts.OAK_BINDING)
				.type(Type.PICKAXE)
				.id("forgero:iron-pickaxe")
				.build();
		NbtCompound compound = encoder.encode(tool);
		var pickaxe = parser.parse(compound);

		Assertions.assertTrue(pickaxe.isPresent() && pickaxe.get() instanceof Constructed constructedTool && constructedTool.parts().size() == 2);
	}
}
