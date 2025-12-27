package com.sigmundgranaas.forgero.common.tooltip;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tooltip.api.TooltipSection;
import com.sigmundgranaas.forgero.common.tooltip.api.writer.SectionWriter;
import com.sigmundgranaas.forgero.common.tooltip.section.SectionRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TooltipSectionRegistryTest {

	@BeforeEach
	void setUp() {
		SectionRegistry.clear();
	}

	@Test
	void registerAndRetrieveSection() {
		TooltipSection section = TooltipSection.forgero("test", 100);

		SectionRegistry.register(section, ctx -> SectionWriter.EMPTY);

		var retrieved = SectionRegistry.get(section.id());
		assertTrue(retrieved.isPresent());
		assertEquals(section, retrieved.get().section());
	}

	@Test
	void getAllSortedReturnsSectionsByPriority() {
		TooltipSection high = TooltipSection.forgero("high", 300);
		TooltipSection low = TooltipSection.forgero("low", 100);
		TooltipSection mid = TooltipSection.forgero("mid", 200);

		SectionRegistry.register(high, ctx -> SectionWriter.EMPTY);
		SectionRegistry.register(low, ctx -> SectionWriter.EMPTY);
		SectionRegistry.register(mid, ctx -> SectionWriter.EMPTY);

		List<SectionRegistry.RegisteredSection> sorted = SectionRegistry.getAllSorted();

		assertEquals(3, sorted.size());
		assertEquals("low", sorted.get(0).section().path());
		assertEquals("mid", sorted.get(1).section().path());
		assertEquals("high", sorted.get(2).section().path());
	}

	@Test
	void priorityOverrideAffectsSorting() {
		TooltipSection first = TooltipSection.forgero("first", 100);
		TooltipSection second = TooltipSection.forgero("second", 200);

		SectionRegistry.register(first, ctx -> SectionWriter.EMPTY);
		SectionRegistry.register(second, ctx -> SectionWriter.EMPTY);

		// Override second to have lower priority (appear first)
		SectionRegistry.setPriorityOverride(second.id(), 50);

		List<SectionRegistry.RegisteredSection> sorted = SectionRegistry.getAllSorted();

		assertEquals("second", sorted.get(0).section().path());
		assertEquals("first", sorted.get(1).section().path());
	}

	@Test
	void clearPriorityOverrideRevertsToDerault() {
		TooltipSection section = TooltipSection.forgero("test", 100);
		SectionRegistry.register(section, ctx -> SectionWriter.EMPTY);

		SectionRegistry.setPriorityOverride(section.id(), 50);
		assertEquals(50, SectionRegistry.getEffectivePriority(section.id()));

		SectionRegistry.clearPriorityOverride(section.id());
		assertEquals(100, SectionRegistry.getEffectivePriority(section.id()));
	}

	@Test
	void isRegisteredReturnsTrueForRegisteredSection() {
		TooltipSection section = TooltipSection.forgero("test", 100);
		SectionRegistry.register(section, ctx -> SectionWriter.EMPTY);

		assertTrue(SectionRegistry.isRegistered(section.id()));
		assertFalse(SectionRegistry.isRegistered(OpenIdentifier.parse("forgero:nonexistent")));
	}

	@Test
	void unregisterRemovesSection() {
		TooltipSection section = TooltipSection.forgero("test", 100);
		SectionRegistry.register(section, ctx -> SectionWriter.EMPTY);

		assertTrue(SectionRegistry.unregister(section.id()));
		assertFalse(SectionRegistry.isRegistered(section.id()));
	}

	@Test
	void sizeReturnsCorrectCount() {
		assertEquals(0, SectionRegistry.size());

		SectionRegistry.register(TooltipSection.forgero("a", 100), ctx -> SectionWriter.EMPTY);
		SectionRegistry.register(TooltipSection.forgero("b", 200), ctx -> SectionWriter.EMPTY);

		assertEquals(2, SectionRegistry.size());
	}
}
