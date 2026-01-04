package com.sigmundgranaas.forgero.core.condition.predicate;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.context.ResolutionContext;
import org.junit.jupiter.api.Test;

import static com.sigmundgranaas.forgero.testutils.ForgeroTestFactory.*;
import static com.sigmundgranaas.forgero.testutils.TestIdentifiers.*;
import static org.junit.jupiter.api.Assertions.*;

class SlotContainsConditionTest extends ForgeroTest {

	@Test
	void conditionPassesWhenSlotContainsComponentWithTag() {
		Component iron = material(IRON_ID, METAL_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();

		SlotContainsCondition condition = new SlotContainsCondition(
				id("forgero:slot_contains"),
				TOOL_MATERIAL_ID,
				METAL_TAG
		);

		ResolutionContext context = new ResolutionContext(pickaxeHead, pickaxeHead);

		assertTrue(condition.test(context), "Condition should pass when slot contains component with tag");
	}

	@Test
	void conditionFailsWhenSlotContainsComponentWithoutTag() {
		Component oak = material(OAK_ID, WOOD_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, oak))
				.build();

		SlotContainsCondition condition = new SlotContainsCondition(
				id("forgero:slot_contains"),
				TOOL_MATERIAL_ID,
				METAL_TAG
		);

		ResolutionContext context = new ResolutionContext(pickaxeHead, pickaxeHead);

		assertFalse(condition.test(context), "Condition should fail when slot contains component without tag");
	}

	@Test
	void conditionFailsWhenSlotTypeDoesNotExist() {
		Component pickaxeHead = part(PICKAXE_HEAD_ID).build();

		SlotContainsCondition condition = new SlotContainsCondition(
				id("forgero:slot_contains"),
				TOOL_MATERIAL_ID,
				METAL_TAG
		);

		ResolutionContext context = new ResolutionContext(pickaxeHead, pickaxeHead);

		assertFalse(condition.test(context), "Condition should fail when slot type does not exist");
	}

	@Test
	void conditionWorksAcrossComponentHierarchy() {
		Component iron = material(IRON_ID, METAL_TAG);
		Component pickaxeHead = part(PICKAXE_HEAD_ID)
				.withStructureSlot(structureSlot("material_slot", TOOL_MATERIAL_ID, iron))
				.build();
		Component handle = material(HANDLE_ID, WOOD_TAG);
		Component pickaxe = part(PICKAXE_ID)
				.withStructureSlot(structureSlot("head_slot", PICKAXE_HEAD_ID, pickaxeHead))
				.withStructureSlot(structureSlot("handle_slot", HANDLE_ID, handle))
				.build();

		SlotContainsCondition condition = new SlotContainsCondition(
				id("forgero:slot_contains"),
				TOOL_MATERIAL_ID,
				METAL_TAG
		);

		// Context is evaluating the handle, but it should search the root (pickaxe)
		ResolutionContext context = new ResolutionContext(handle, pickaxe);

		assertTrue(condition.test(context), "Condition should search root for slot type");
	}

	@Test
	void hasCorrectType() {
		SlotContainsCondition condition = new SlotContainsCondition(
				id("forgero:slot_contains"),
				TOOL_MATERIAL_ID,
				METAL_TAG
		);

		assertEquals(id("forgero:slot_contains"), condition.type());
	}

	@Test
	void hasCorrectSlotType() {
		SlotContainsCondition condition = new SlotContainsCondition(
				id("forgero:slot_contains"),
				TOOL_MATERIAL_ID,
				METAL_TAG
		);

		assertEquals(TOOL_MATERIAL_ID, condition.slotType());
	}

	@Test
	void hasCorrectTag() {
		SlotContainsCondition condition = new SlotContainsCondition(
				id("forgero:slot_contains"),
				TOOL_MATERIAL_ID,
				METAL_TAG
		);

		assertEquals(METAL_TAG, condition.tag());
	}
}
