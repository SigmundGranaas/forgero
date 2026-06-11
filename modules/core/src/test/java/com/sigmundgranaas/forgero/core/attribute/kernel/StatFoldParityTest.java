package com.sigmundgranaas.forgero.core.attribute.kernel;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.attribute.impl.AttributeEngine;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.ComponentTraversal;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ADR-003 parity gate: the StatFold kernel must reproduce the current AttributeEngine's
 * values for every component the data pipeline generates, for every stat type, before any of
 * the old kernel may be deleted. Divergences are reported exhaustively, not on first failure,
 * so each can be classified (bug in fold vs. enumerated, accepted semantic change).
 */
class StatFoldParityTest {

	@Test
	void foldMatchesEngineForEveryGeneratedComponent() {
		var bundle = KernelTestBundle.bundle();
		var components = bundle.componentRegistry().all();
		assertTrue(components.size() > 0, "data bundle should generate components");

		AttributeEngine engine = new AttributeEngine();
		List<String> diffs = new ArrayList<>();
		int compared = 0;

		for (Component component : components) {
			BakedAttributes expected = engine.resolve(component);
			BakedAttributes actual = StatFold.fold(component);

			for (OpenIdentifier type : typesIn(component, expected, actual)) {
				float e = expected.get(type).value();
				float a = actual.get(type).value();
				compared++;
				if (Math.abs(e - a) > 0.0001f) {
					diffs.add(component.id() + " / " + type + ": engine=" + e + " fold=" + a);
				}
			}
		}

		assertTrue(compared > 0, "parity must compare at least one value");
		assertTrue(diffs.isEmpty(), "parity divergences (" + diffs.size() + "):\n" + String.join("\n", diffs));
	}

	private static Set<OpenIdentifier> typesIn(Component root, BakedAttributes a, BakedAttributes b) {
		Set<OpenIdentifier> types = new LinkedHashSet<>();
		for (Component c : ComponentTraversal.traverse(root)) {
			for (Attribute attr : c.properties(Attribute.KEY)) {
				types.add(attr.type());
			}
		}
		types.addAll(a.byType().keySet());
		types.addAll(b.byType().keySet());
		return types;
	}
}
