package com.sigmundgranaas.forgero.fabric.nbt.v2;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.INGREDIENTS_IDENTIFIER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.NAME_IDENTIFIER;

import com.sigmundgranaas.forgero.core.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.core.resource.PipelineBuilder;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.fabric.resources.FabricPackFinder;
import com.sigmundgranaas.forgero.item.nbt.v2.CompositeEncoder;
import com.sigmundgranaas.forgero.item.nbt.v2.CompoundEncoder;
import com.sigmundgranaas.forgero.testutil.Tools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

public class StateToNbtTest {
	private static final CompoundEncoder<State> encoder = new CompositeEncoder();

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
	void encodeCompound() {
		NbtCompound compound = encoder.encode(Tools.IRON_PICKAXE.get());
		Assertions.assertEquals("iron-pickaxe", compound.getString(NAME_IDENTIFIER));
	}

	@Test
	void encodeCompoundWithIngredients() {
		NbtCompound compound = encoder.encode(Tools.IRON_PICKAXE.get());
		NbtList list = compound.getList(INGREDIENTS_IDENTIFIER, NbtElement.COMPOUND_TYPE);
		Assertions.assertEquals(2, list.size());
		Assertions.assertEquals(((NbtCompound) list.get(0)).getString(NAME_IDENTIFIER), "iron-pickaxe_head");
		Assertions.assertEquals(((NbtCompound) list.get(1)).getString(NAME_IDENTIFIER), "oak-handle");
	}
}
