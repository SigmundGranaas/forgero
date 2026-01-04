package com.sigmundgranaas.forgero.data.pipeline;

import com.sigmundgranaas.forgero.core.ForgeroTest;
import com.sigmundgranaas.forgero.cof.dto.CofComponent;
import com.sigmundgranaas.forgero.data.pipeline.impl.TemplateGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to reproduce the template generator bug where part template attributes
 * are not included when generating equipment.
 * <p>
 * BUG SYMPTOMS:
 * - Iron pickaxe and diamond pickaxe both have 50 durability (should be 240 and 1550)
 * - Only handle material (oak = 50) attributes are being used
 * - Head template multipliers are missing from the attribute merge
 * <p>
 * ROOT CAUSE HYPOTHESIS:
 * In TemplateGenerator.getSourceDtosForComponent(), when a generated part has structure,
 * it only returns the DTOs of its slot contents (materials) but NOT the part template itself.
 * This means the part template's attributes (multipliers) are never included in equipment generation.
 */
class TemplateGeneratorAttributeBugTest extends ForgeroTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(TemplateGeneratorAttributeBugTest.class);

	/**
	 * This test validates the bug: when getSourceDtosForComponent is called on a generated part,
	 * does it include the part template's DTO?
	 * <p>
	 * EXPECTED BEHAVIOR:
	 * For iron-pickaxe_head (generated from pickaxe_head template + iron material):
	 * - Equipment should have durability from: [pickaxe template, pickaxe_head template (multiplier), iron material (base value), handle template, oak material]
	 * <p>
	 * ACTUAL BUGGY BEHAVIOR:
	 * - Equipment only has: [pickaxe template, iron material, handle template, oak material]
	 * - Missing: pickaxe_head template multipliers!
	 */
	@Test
	void generatedEquipmentShouldIncludePartTemplateAttributes() {
		// This test will use real data files from test resources to prove the bug
		// Setup: Load materials, part templates, and equipment templates from test resources
		// Then verify that generated equipment includes all expected DTOs in the merge

		LOGGER.info("Testing template attribute inclusion in equipment generation");

		// The test data should show:
		// - iron material: 240 durability (base value)
		// - pickaxe_head template: 1.0x durability multiplier (CURRENTLY MISSING!)
		// - oak material: 50 durability (base value)
		// - handle template: 1.0x durability multiplier
		// Expected pickaxe durability: Should use both iron (from head) and oak (from handle)
		// Actual buggy pickaxe durability: Only uses oak (50) because head template is missing

		// TODO: Set up test data and run template generation
		// This requires:
		// 1. Create minimal test JSON files (material, part template, equipment template)
		// 2. Set up TemplateGenerator with test data
		// 3. Generate equipment
		// 4. Verify the generated component has attributes from part template

		LOGGER.warn("Test implementation pending - need to set up test data infrastructure");
		LOGGER.info("See ForgeroDataInitializerTest for example of setting up test data");
	}

	/**
	 * Integration test: Generate an iron-pickaxe from templates and verify it has correct durability.
	 * <p>
	 * SETUP:
	 * - Iron material: 240 durability (context: part-composite)
	 * - Pickaxe head template: 1.0x durability multiplier (context: part-composite)
	 * - Oak material: 50 durability (context: part-composite)
	 * - Handle template: 1.0x durability multiplier (context: part-composite)
	 * <p>
	 * EXPECTED:
	 * - Iron-pickaxe_head: 240 × 1.0 = 240
	 * - Oak-handle: 50 × 1.0 = 50
	 * - Iron-pickaxe: Should compose both = 290 or more (depending on composition strategy)
	 * <p>
	 * ACTUAL (BUGGY):
	 * - Iron-pickaxe: 50 (only handle material, missing head!)
	 */
	@Test
	void generatedPickaxeShouldHaveCorrectDurability() {
		// TODO: Full integration test
		// This requires setting up:
		// 1. Material definitions (iron, oak)
		// 2. Part templates (pickaxe_head, handle)
		// 3. Equipment template (pickaxe)
		// 4. Running template generation
		// 5. Validating final attributes

		LOGGER.warn("Integration test not yet implemented - requires full template pipeline");
	}
}
