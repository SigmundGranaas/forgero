package com.sigmundgranaas.forgero.core.attribute.kernel;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.attribute.api.Attribute;
import com.sigmundgranaas.forgero.core.attribute.api.AttributeScope;
import com.sigmundgranaas.forgero.core.attribute.api.BakedAttributes;
import com.sigmundgranaas.forgero.core.attribute.api.SimpleAttribute;
import com.sigmundgranaas.forgero.core.attribute.api.operator.MultiplicationOperator;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentPart;
import com.sigmundgranaas.forgero.core.component.api.structure.ComponentStructure;
import com.sigmundgranaas.forgero.core.component.impl.StaticComponent;
import com.sigmundgranaas.forgero.core.component.impl.StructuredEquipment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the StatFold kernel's distinguishing rules: the offer/accept whitelist,
 * the rise rule, sealing, and local semantics (ADR-003).
 */
class StatFoldTest {

	private static final OpenIdentifier DUR = OpenIdentifier.parse("forgero:durability");
	private static final OpenIdentifier ARMOR = OpenIdentifier.parse("forgero:armor");
	private static final OpenIdentifier WEIGHT = OpenIdentifier.parse("forgero:weight");

	private static OpenIdentifier id(String s) {
		return OpenIdentifier.parse("forgero:" + s);
	}

	private static Attribute offer(OpenIdentifier type, float v) {
		return new SimpleAttribute(type, v).withScope(AttributeScope.PART_COMPOSITE);
	}

	private static Attribute offerMul(OpenIdentifier type, float v) {
		return new SimpleAttribute(type, v, MultiplicationOperator.getInstance(), 0).withScope(AttributeScope.PART_COMPOSITE);
	}

	private static Attribute add(OpenIdentifier type, float v) {
		return new SimpleAttribute(type, v);
	}

	private static Attribute mul(OpenIdentifier type, float v) {
		return new SimpleAttribute(type, v, MultiplicationOperator.getInstance(), 0);
	}

	private static Component leaf(String name, OpenIdentifier tag, Attribute... attrs) {
		return new StaticComponent(id(name), Set.of(tag), Map.of(Attribute.KEY.key(), List.of(attrs)));
	}

	private static Component assemble(String name, Map<String, List<?>> props, ComponentPart... parts) {
		return StructuredEquipment.create(id(name), Set.of(id("tool")), props, ComponentStructure.of(parts));
	}

	private static ComponentPart slot(String slotName, OpenIdentifier requiredTag, Component content) {
		return ComponentPart.ofType(id(slotName), requiredTag, "test slot", content);
	}

	@Test
	void offerAcceptWhitelist_unacceptedOfferIsInert() {
		// material offers durability AND armor; the template multiplies only durability.
		OpenIdentifier matTag = id("mat");
		Component material = leaf("iron", matTag, offer(DUR, 250f), offer(ARMOR, 5f));
		Component part = assemble("head",
				Map.of(Attribute.KEY.key(), List.of(offerMul(DUR, 0.7f))),
				slot("material", matTag, material));

		BakedAttributes baked = StatFold.fold(part);
		assertEquals(175f, baked.get(DUR).value(), 0.001f, "accepted offer: 250 x 0.7");
		assertEquals(0f, baked.get(ARMOR).value(), 0.001f, "unaccepted offer must be inert (the whitelist)");
	}

	@Test
	void offerRisesUntilAccepted() {
		// material offers attack damage; the part has no multiplier; the tool accepts it.
		OpenIdentifier ad = id("attack_damage");
		OpenIdentifier matTag = id("mat");
		OpenIdentifier partTag = id("tool");
		Component material = leaf("iron", matTag, offer(ad, 4f));
		Component head = assemble("head", Map.of(), slot("material", matTag, material));
		Component tool = assemble("sword",
				Map.of(Attribute.KEY.key(), List.of(mul(ad, 1.5f))),
				slot("head", partTag, head));

		assertEquals(6f, StatFold.fold(tool).get(ad).value(), 0.001f, "risen offer accepted at the tool: 4 x 1.5");
	}

	@Test
	void plainAddsNeedNoAcceptance_andMultiplierScalesOwnNode() {
		Component item = leaf("simple", id("tool"), add(DUR, 10f), mul(DUR, 2f));
		assertEquals(20f, StatFold.fold(item).get(DUR).value(), 0.001f);
	}

	@Test
	void unmetMultiplierRisesToBase() {
		// a handle's weight multiplier has no base at the handle; it applies at the tool.
		OpenIdentifier hTag = id("handle_t");
		Component handle = leaf("handle", hTag, mul(WEIGHT, 0.6f));
		Component tool = assemble("tool",
				Map.of(Attribute.KEY.key(), List.of(add(WEIGHT, 10f))),
				slot("handle", hTag, handle));
		assertEquals(6f, StatFold.fold(tool).get(WEIGHT).value(), 0.001f);
	}

	@Test
	void sealingPreventsRetroactiveScaling() {
		// the part's own multiplier scales the part only; the tool's plain base is added after.
		OpenIdentifier pTag = id("part_t");
		Component part = leaf("part", pTag, add(DUR, 10f), mul(DUR, 2f)); // seals at 20
		Component tool = assemble("tool",
				Map.of(Attribute.KEY.key(), List.of(add(DUR, 5f))),
				slot("part", pTag, part));
		assertEquals(25f, StatFold.fold(tool).get(DUR).value(), 0.001f,
				"sealed part (20) + tool base (5); a flat fold would wrongly give 30");
	}

	@Test
	void localCountsOnlyOnTheQueriedRoot() {
		OpenIdentifier pTag = id("part_t");
		Attribute localAttr = new SimpleAttribute(DUR, 7f).withScope(AttributeScope.LOCAL);
		Component part = new StaticComponent(id("part"), Set.of(pTag), Map.of(Attribute.KEY.key(), List.of(localAttr)));

		// queried directly: counts
		assertEquals(7f, StatFold.fold(part).get(DUR).value(), 0.001f);

		// composed into a tool: does not propagate
		Component tool = assemble("tool", Map.of(), slot("part", pTag, part));
		assertEquals(0f, StatFold.fold(tool).get(DUR).value(), 0.001f);
	}
}
