package com.sigmundgranaas.forgero.core.property.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.variant.StaticComponent;
import com.sigmundgranaas.forgero.core.component.variant.StructuredPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

class ResolutionContextTest extends ForgeroTest {

	private StructuredPart pickaxe;
	private StructuredPart pickaxeHead;
	private StaticComponent iron;
	private StaticComponent handle;
	private StaticComponent binding;

	@BeforeEach
	void setUp() {
		iron = material(IRON_ID, METAL_TAG);
		pickaxeHead = new StructuredPart(PICKAXE_HEAD_ID, Set.of(), List.of(), new ComponentStructure(List.of(slot(idFactory.of("material_slot"), MATERIAL_ID, iron))));
		handle = part(HANDLE_ID, WOOD_TAG);
		binding = part(idFactory.of("binding"), METAL_TAG);
		pickaxe = new StructuredPart(PICKAXE_ID, Set.of(), List.of(), new ComponentStructure(List.of(slot(HEAD_SLOT_ID, PICKAXE_HEAD_TAG, pickaxeHead), slot(HANDLE_SLOT_ID, HANDLE_TAG, handle), slot(BINDING_SLOT_ID, BINDING_TAG, binding))));
	}

	@Test
	void contextCorrectlyIdentifiesParent() {
		// To test a component's context, a new context must be created for it.
		ResolutionContext ironContext = new ResolutionContext(iron, pickaxe);
		assertEquals(Optional.of(pickaxeHead), ironContext.getParent());

		ResolutionContext headContext = new ResolutionContext(pickaxeHead, pickaxe);
		assertEquals(Optional.of(pickaxe), headContext.getParent());

		ResolutionContext rootContext = new ResolutionContext(pickaxe, pickaxe);
		assertEquals(Optional.empty(), rootContext.getParent(), "Root component should have no parent.");
	}

	@Test
	void contextReportsCorrectDepth() {
		assertEquals(0, new ResolutionContext(pickaxe, pickaxe).getDepth());
		assertEquals(1, new ResolutionContext(pickaxeHead, pickaxe).getDepth());
		assertEquals(2, new ResolutionContext(iron, pickaxe).getDepth());
	}

	@Test
	void contextIdentifiesSiblings() {
		ResolutionContext headContext = new ResolutionContext(pickaxeHead, pickaxe);
		List<Component> headSiblings = headContext.getSiblings();
		assertEquals(2, headSiblings.size());
		assertTrue(headSiblings.contains(handle));
		assertTrue(headSiblings.contains(binding));
		assertFalse(headSiblings.contains(pickaxeHead));
	}

	@Test
	void findInRootCanLocateSlotContent() {
		ResolutionContext anyContext = new ResolutionContext(iron, pickaxe);

		// findInRoot should be called with the SLOT TYPE TAG
		Optional<Component> foundHandle = anyContext.findInRoot(HANDLE_TAG);
		assertTrue(foundHandle.isPresent());
		assertEquals(handle, foundHandle.get());

		Optional<Component> foundIron = anyContext.findInRoot(MATERIAL_ID);
		assertTrue(foundIron.isPresent());
		assertEquals(iron, foundIron.get());
	}
}
