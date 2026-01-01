package com.sigmundgranaas.forgero.core.attribute.composition;

import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeContext;
import com.sigmundgranaas.forgero.core.attribute.api.DefaultAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.AdditionOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link LocalContextHandler}.
 *
 * <p>Verifies that local context attributes only apply to the component
 * they're defined on and don't propagate to parents.</p>
 */
@DisplayName("LocalContextHandler Tests")
class LocalContextHandlerTest {

	private LocalContextHandler handler;

	@BeforeEach
	void setUp() {
		handler = LocalContextHandler.INSTANCE;
	}

	@Test
	@DisplayName("Handler has correct context ID")
	void contextIdIsLocal() {
		assertEquals(AttributeContext.LOCAL, handler.contextId());
	}

	@Nested
	@DisplayName("Empty Input")
	class EmptyInputTests {

		@Test
		@DisplayName("Empty sources returns empty list")
		void emptySources() {
			List<Attribute> result = handler.compose(Map.of());
			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("No self source returns empty list")
		void noSelfSource() {
			SimpleAttribute attr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					4f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("child_material", List.of(attr));

			List<Attribute> result = handler.compose(sources);
			assertTrue(result.isEmpty(), "Without 'self' source, should return empty");
		}
	}

	@Nested
	@DisplayName("Self Source Filtering")
	class SelfSourceTests {

		@Test
		@DisplayName("Includes attributes from self source")
		void includesSelfAttributes() {
			SimpleAttribute selfAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put(LocalContextHandler.SELF_SOURCE, List.of(selfAttr));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size());
			assertEquals(DefaultAttributes.DURABILITY, result.get(0).type());
			assertEquals(100f, result.get(0).value(), 0.001f);
		}

		@Test
		@DisplayName("Excludes attributes from child sources")
		void excludesChildAttributes() {
			SimpleAttribute selfAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute childAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					5f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put(LocalContextHandler.SELF_SOURCE, List.of(selfAttr));
			sources.put("child_material", List.of(childAttr));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size(), "Only self attribute should be included");
			assertEquals(DefaultAttributes.DURABILITY, result.get(0).type());
		}

		@Test
		@DisplayName("Includes multiple attributes from self")
		void includesMultipleSelfAttributes() {
			SimpleAttribute attr1 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute attr2 = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.MINING_SPEED,
					5f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put(LocalContextHandler.SELF_SOURCE, List.of(attr1, attr2));

			List<Attribute> result = handler.compose(sources);

			assertEquals(2, result.size());
		}
	}

	@Nested
	@DisplayName("Resolved Attribute Properties")
	class ResolvedPropertiesTests {

		@Test
		@DisplayName("Resolved attributes have no context")
		void resolvedHasNoContext() {
			SimpleAttribute attr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeContext.LOCAL),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put(LocalContextHandler.SELF_SOURCE, List.of(attr));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size());
			assertTrue(result.get(0).context().isEmpty(),
					"Resolved attribute should have no context");
		}
	}
}
