package com.sigmundgranaas.forgero.fabric.gametest;

import static com.sigmundgranaas.forgero.minecraft.common.client.forgerotool.model.implementation.EmptyBakedModel.EMPTY;
import static com.sigmundgranaas.forgero.minecraft.common.match.MinecraftContextKeys.STACK;

import java.util.Collections;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.model.ModelResult;
import com.sigmundgranaas.forgero.core.state.SimpleState;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.type.Type;
import com.sigmundgranaas.forgero.core.util.match.MatchContext;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.BakedModelResult;
import com.sigmundgranaas.forgero.minecraft.common.client.model.baked.strategy.DefaultModelStrategy;
import org.junit.jupiter.api.Assertions;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;

/**
 * This test class can only be tested in client environments
 */
public class ModelStrategyTest {
	public State state;
	public MatchContext context;
	public BakedModelResult result;
	public ItemStack itemStack;
	public DefaultModelStrategy strategy;

	void setUp() {
		result = new BakedModelResult(ModelResult.EMPTY, EMPTY);
		itemStack = new ItemStack(Items.OAK_PLANKS);
		state = new SimpleState("forgero:test-state", Type.of("test"), Collections.emptyList());
		context = MatchContext.of();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "modelStrategy")
	public void returnsResultWhenItemStackPresentAndNoNBT(TestContext testContext) {
		setUp();
		// Setup context with ItemStack having no NBT
		context.put(STACK, itemStack);

		strategy = new DefaultModelStrategy(result, state.hashCode());

		Optional<BakedModelResult> modelResult = strategy.getModel(state, context);
		Assertions.assertTrue(modelResult.isPresent(), "Model result should be present when ItemStack has no NBT");
		Assertions.assertEquals(result, modelResult.get(), "Returned model should match the provided result");
		testContext.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "modelStrategy")
	public void returnsEmptyWhenItemStackHasNBT(TestContext testContext) {
		setUp();

		// Setup context with ItemStack having NBT
		itemStack.getOrCreateNbt();
		context.put(STACK, itemStack);

		// Strategy where code doesn't match
		strategy = new DefaultModelStrategy(result, state.hashCode() + 1);

		Optional<BakedModelResult> modelResult = strategy.getModel(state, context);
		Assertions.assertFalse(modelResult.isPresent(), "Model result should be absent when ItemStack has NBT and code does not match");
		testContext.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "modelStrategy")
	public void returnsResultWhenCodeMatchesStateHashCode(TestContext testContext) {
		setUp();

		// Context without ItemStack
		strategy = new DefaultModelStrategy(result, state.hashCode());

		Optional<BakedModelResult> modelResult = strategy.getModel(state, context);
		Assertions.assertTrue(modelResult.isPresent(), "Model result should be present when code matches state's hash code");
		Assertions.assertEquals(result, modelResult.get(), "Returned model should match the provided result");
		testContext.complete();
	}

	@GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = "modelStrategy")
	public void returnsEmptyWhenCodeDoesNotMatchStateHashCode(TestContext testContext) {
		setUp();

		// Context without ItemStack
		strategy = new DefaultModelStrategy(result, state.hashCode() + 1);

		Optional<BakedModelResult> modelResult = strategy.getModel(state, context);
		Assertions.assertFalse(modelResult.isPresent(), "Model result should be absent when code does not match state's hash code");
		testContext.complete();
	}
}
