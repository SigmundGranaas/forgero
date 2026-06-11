package com.sigmundgranaas.forgero.core.property.compiled;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.component.api.EquipmentComponent;
import com.sigmundgranaas.forgero.core.component.impl.StaticEquipment;
import com.sigmundgranaas.forgero.core.property.api.CompilerPass;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the compiler-in-the-factory inversion: a terminal component pre-compiles every
 * registered property pass at construction, and serves the results by key from its compiled
 * artifact — no tree traversal at read time.
 */
class CompiledPropertiesTest {

	private static final ResolutionKey<List<String>> TEST_KEY =
			new ResolutionKey<>(new OpenIdentifier("forgero", "test/compiled_marker"));

	/** A trivial pass that compiles to a fixed marker list, proving registered passes run at construction. */
	static final class MarkerEngine implements CompilerPass<List<String>> {
		public ResolutionKey<List<String>> key() { return TEST_KEY; }
		public List<String> compile(Stream<com.sigmundgranaas.forgero.core.component.api.Component> components) {
			return List.of("compiled-at-construction");
		}
	}

	@Test
	void terminalPreCompilesRegisteredPassAtConstruction() {
		CompilerPasses.register(TEST_KEY, MarkerEngine::new);

		List<Attribute> attrs = List.of(new SimpleAttribute(DefaultAttributes.ATTACK_DAMAGE, 7f));
		StaticEquipment tool = StaticEquipment.create(
				new OpenIdentifier("forgero", "test_tool"),
				Set.of(new OpenIdentifier("forgero", "tool")),
				Map.of(Attribute.KEY.key(), attrs));

		// The registered property pass ran at construction and the result is read by key.
		assertEquals(List.of("compiled-at-construction"), tool.properties(TEST_KEY));

		// Attributes live in the same compiled artifact.
		assertEquals(7f, tool.getAttribute(DefaultAttributes.ATTACK_DAMAGE));
		assertEquals(tool.compiled().attributes(), tool.bakedAttributes());
	}

	@Test
	void unregisteredKeyReadsEmpty() {
		EquipmentComponent tool = StaticEquipment.create(
				new OpenIdentifier("forgero", "bare_tool"),
				Set.of(),
				Map.of());
		ResolutionKey<List<String>> absent =
				new ResolutionKey<>(new OpenIdentifier("forgero", "test/absent"));
		assertTrue(tool.properties(absent).isEmpty());
	}
}
