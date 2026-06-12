package com.sigmundgranaas.forgero.tests;

import com.sigmundgranaas.forgero.common.api.ConditionContext;
import com.sigmundgranaas.forgero.common.api.ForgeroApi;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.ComponentUpgradeSlot;
import com.sigmundgranaas.forgero.core.component.api.slot.InstallationResult;
import com.sigmundgranaas.forgero.core.property.compilation.ResolutionContext;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroGameTest;
import com.sigmundgranaas.forgero.mc.testcommon.gametest.ForgeroTestUtils;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end proof that the public condition-authoring path (ConditionContext) reflects a real
 * assembled component — i.e. a downstream {@code registerStaticCondition(...)} predicate would see
 * the truth. The adapter is the only non-trivial part of the F1 public extension API; this exercises
 * it against an actual installed upgrade rather than a hand-built tree.
 */
public class PublicApiExtensionTest implements ForgeroGameTest {

	private static final OpenIdentifier OFFENSIVE = OpenIdentifier.parse("forgero:contexts/offensive");

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void condition_context_reflects_root(TestContext context) {
		Component head = ForgeroTestUtils.forgero(context).component("forgero:diamond-pickaxe_head").orElseThrow();

		ConditionContext ctx = ConditionContext.of(new ResolutionContext(head, head));

		assertTrue(ctx.isRoot(), "The queried root component must report isRoot()");
		assertEquals(0, ctx.depth(), "Root depth must be 0");
		assertFalse(ctx.isInSlotType(OFFENSIVE), "The root is not in any slot");

		context.complete();
	}

	@GameTest(templateName = EMPTY_STRUCTURE, required = true)
	public void condition_context_reflects_installed_upgrade_slot(TestContext context) {
		Component head = ForgeroTestUtils.forgero(context).component("forgero:diamond-pickaxe_head").orElseThrow();
		Component iron = ForgeroTestUtils.forgero(context).component("forgero:iron").orElseThrow();

		InstallationResult result = ForgeroApi.slotManager().install(head, iron);
		assertTrue(result.success(), "Iron must install into the reinforcement slot");
		Component upgraded = result.component().orElseThrow();

		// The iron instance now sitting in the offensive reinforcement slot.
		Component installed = ((CustomizableComponent) upgraded).upgrades().allUpgradeSlots().stream()
				.filter(slot -> slot.id().toString().contains("reinforcement"))
				.map(ComponentUpgradeSlot::getContent)
				.flatMap(java.util.Optional::stream)
				.findFirst()
				.orElseThrow();

		ConditionContext ctx = ConditionContext.of(new ResolutionContext(installed, upgraded));

		assertFalse(ctx.isRoot(), "An installed upgrade is not the root");
		assertTrue(ctx.depth() > 0, "An installed upgrade is below the root");
		assertTrue(ctx.isInSlotType(OFFENSIVE),
				"The public ConditionContext must report the slot's offensive context — this is what a "
						+ "downstream registerStaticCondition(\"...\", c -> c.isInSlotType(...)) predicate sees");

		context.complete();
	}
}
