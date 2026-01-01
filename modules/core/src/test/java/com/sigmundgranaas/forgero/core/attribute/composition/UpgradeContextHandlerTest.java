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
 * Tests for {@link UpgradeContextHandler}.
 *
 * <p>Verifies that upgrade context attributes only apply when the component
 * is in an upgrade slot.</p>
 */
@DisplayName("UpgradeContextHandler Tests")
class UpgradeContextHandlerTest {

	private UpgradeContextHandler handler;

	@BeforeEach
	void setUp() {
		handler = UpgradeContextHandler.INSTANCE;
	}

	@Test
	@DisplayName("Handler has correct context ID")
	void contextIdIsUpgrade() {
		assertEquals(AttributeContext.UPGRADE, handler.contextId());
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
		@DisplayName("No upgrade sources returns empty list")
		void noUpgradeSources() {
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
			sources.put("material", List.of(attr));
			sources.put("self", List.of(attr));

			List<Attribute> result = handler.compose(sources);
			assertTrue(result.isEmpty(), "Without upgrade source, should return empty");
		}
	}

	@Nested
	@DisplayName("Upgrade Source Filtering")
	class UpgradeSourceTests {

		@Test
		@DisplayName("Includes attributes from upgrade sources")
		void includesUpgradeAttributes() {
			SimpleAttribute attr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					10f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put(UpgradeContextHandler.UPGRADE_SOURCE_PREFIX + "gem_slot", List.of(attr));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size());
			assertEquals(DefaultAttributes.ATTACK_DAMAGE, result.get(0).type());
			assertEquals(10f, result.get(0).value(), 0.001f);
		}

		@Test
		@DisplayName("Excludes attributes from non-upgrade sources")
		void excludesNonUpgradeAttributes() {
			SimpleAttribute upgradeAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					10f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute materialAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					100f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put(UpgradeContextHandler.UPGRADE_SOURCE_PREFIX + "socket", List.of(upgradeAttr));
			sources.put("material", List.of(materialAttr));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size(), "Only upgrade source attribute should be included");
			assertEquals(DefaultAttributes.ATTACK_DAMAGE, result.get(0).type());
		}

		@Test
		@DisplayName("Includes attributes from multiple upgrade sources")
		void includesMultipleUpgradeSources() {
			SimpleAttribute gemAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					5f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);
			SimpleAttribute reinforcementAttr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.DURABILITY,
					50f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put(UpgradeContextHandler.UPGRADE_SOURCE_PREFIX + "gem", List.of(gemAttr));
			sources.put(UpgradeContextHandler.UPGRADE_SOURCE_PREFIX + "reinforcement", List.of(reinforcementAttr));

			List<Attribute> result = handler.compose(sources);

			assertEquals(2, result.size());
		}

		@Test
		@DisplayName("Handles various upgrade slot naming conventions")
		void handlesVariousUpgradeNames() {
			SimpleAttribute attr = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.MINING_SPEED,
					2f,
					AdditionOperator.getInstance(),
					0,
					Optional.empty(),
					Optional.empty()
			);

			// Test different naming conventions
			String[] upgradeNames = {
					"upgrade:gem_slot",
					"upgrade:reinforcement",
					"upgrade:cosmetic",
					"upgrade:binding"
			};

			for (String name : upgradeNames) {
				Map<String, List<Attribute>> sources = new HashMap<>();
				sources.put(name, List.of(attr));

				List<Attribute> result = handler.compose(sources);
				assertEquals(1, result.size(), "Should include attribute from: " + name);
			}
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
					DefaultAttributes.ATTACK_DAMAGE,
					10f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeContext.UPGRADE),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put(UpgradeContextHandler.UPGRADE_SOURCE_PREFIX + "gem", List.of(attr));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size());
			assertTrue(result.get(0).context().isEmpty(),
					"Resolved attribute should have no context");
		}
	}

	@Nested
	@DisplayName("Real-World Scenarios")
	class RealWorldTests {

		@Test
		@DisplayName("Gem socketed in tool applies upgrade bonus")
		void gemSocketedInTool() {
			// Diamond gem with +3 attack damage when socketed
			SimpleAttribute gemBonus = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					3f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeContext.UPGRADE),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("upgrade:gem_socket", List.of(gemBonus));

			List<Attribute> result = handler.compose(sources);

			assertEquals(1, result.size());
			assertEquals(3f, result.get(0).value(), 0.001f);
		}

		@Test
		@DisplayName("Gem used as material does NOT apply upgrade bonus")
		void gemAsToDoesNotApply() {
			// Same gem, but used as material (not in upgrade slot)
			SimpleAttribute gemBonus = new SimpleAttribute(
					Optional.empty(),
					DefaultAttributes.ATTACK_DAMAGE,
					3f,
					AdditionOperator.getInstance(),
					0,
					Optional.of(AttributeContext.UPGRADE),
					Optional.empty()
			);

			Map<String, List<Attribute>> sources = new HashMap<>();
			sources.put("material", List.of(gemBonus)); // Not an upgrade source

			List<Attribute> result = handler.compose(sources);

			assertTrue(result.isEmpty(), "Upgrade bonus should NOT apply when used as material");
		}
	}
}
