package com.sigmundgranaas.forgero.mc.testcommon.scenario;

import net.minecraft.test.TestContext;

/**
 * Fluent builder for creating gameplay test scenarios.
 * <p>
 * Simplifies writing deep GameTests by providing a declarative API for:
 * <ul>
 *   <li>Setting up players with equipment and positions</li>
 *   <li>Spawning and configuring target entities</li>
 *   <li>Defining actions (attacks, mining, etc.)</li>
 *   <li>Declaring expectations and verifying results</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * @GameTest(templateName = EMPTY_STRUCTURE)
 * public void fireEffect_setsTargetOnFire(TestContext context) {
 *     ScenarioBuilder.create(context)
 *         .player()
 *             .at(1, 64, 1)
 *             .holding(fireSword)
 *         .target()
 *             .entity(EntityType.PIG)
 *             .at(2, 64, 2)
 *         .action()
 *             .attack()
 *         .expect()
 *             .targetOnFire(100) // 5 seconds
 *         .verify();
 * }
 * }</pre>
 *
 * <h2>Benefits</h2>
 * <ul>
 *   <li><b>Reduces boilerplate</b>: No more repetitive player/entity setup code</li>
 *   <li><b>Improves readability</b>: Tests read like specifications</li>
 *   <li><b>Enforces patterns</b>: Consistent test structure across the codebase</li>
 *   <li><b>Type-safe</b>: Builder pattern prevents invalid configurations</li>
 * </ul>
 */
public class ScenarioBuilder {

	private final TestContext context;
	private final ScenarioState state;

	private ScenarioBuilder(TestContext context) {
		this.context = context;
		this.state = new ScenarioState();
	}

	/**
	 * Creates a new scenario builder for the given test context.
	 *
	 * @param context The GameTest context
	 * @return A new ScenarioBuilder instance
	 */
	public static PlayerBuilder create(TestContext context) {
		ScenarioBuilder builder = new ScenarioBuilder(context);
		return new PlayerBuilder(context, builder.state);
	}

	/**
	 * Internal state shared between builder stages.
	 */
	static class ScenarioState {
		ScenarioPlayer player;
		ScenarioTarget target;
		ScenarioAction action;
		ScenarioExpectation expectation;
	}
}
