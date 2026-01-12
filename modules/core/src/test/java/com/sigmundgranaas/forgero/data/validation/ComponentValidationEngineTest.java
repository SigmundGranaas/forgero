package com.sigmundgranaas.forgero.data.validation;

import com.sigmundgranaas.forgero.common.identifier.api.IdentifierFactory;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.common.tags.api.TagResolver;
import com.sigmundgranaas.forgero.core.component.api.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Component Validation Engine Behavior")
class ComponentValidationEngineTest {

	private static final IdentifierFactory ID_FACTORY = new IdentifierFactory.Builder()
			.defaultNamespace("forgero")
			.build();

	private ValidationContext context;

	@BeforeEach
	void setUp() {
		context = new ValidationContext.Builder()
				.tagResolver(TagResolver.empty())
				.build();
	}

	private static OpenIdentifier id(String path) {
		return ID_FACTORY.of(path);
	}

	/**
	 * Simple fake component for testing.
	 */
	private static class FakeComponent implements Component {
		private final OpenIdentifier componentId;

		FakeComponent(OpenIdentifier componentId) {
			this.componentId = componentId;
		}

		@Override
		public OpenIdentifier id() {
			return componentId;
		}

		@Override
		public OpenIdentifier getTypeIdentifier() {
			return ID_FACTORY.of("static_component");
		}

		@Override
		public Set<OpenIdentifier> getTags() {
			return Set.of();
		}

		@Override
		public Map<String, List<?>> propertiesAsMap() {
			return Map.of();
		}

		@Override
		public Component withProperties(Map<String, List<?>> newProperties) {
			return this;
		}
	}

	/**
	 * Counting validator to track invocations.
	 */
	private static class CountingValidator implements ComponentValidator {
		int callCount = 0;
		final List<Component> validatedComponents = new ArrayList<>();
		final ValidationResult resultToReturn;

		CountingValidator(ValidationResult resultToReturn) {
			this.resultToReturn = resultToReturn;
		}

		@Override
		public ValidationResult validate(Component component, ValidationContext context) {
			callCount++;
			validatedComponents.add(component);
			return resultToReturn;
		}
	}

	@Nested
	@DisplayName("Validator Orchestration")
	class ValidatorOrchestration {

		@Test
		@DisplayName("runs all registered validators")
		void runsAllRegisteredValidators() {
			CountingValidator validator1 = new CountingValidator(ValidationResult.empty());
			CountingValidator validator2 = new CountingValidator(ValidationResult.empty());

			ComponentValidationEngine engine = new ComponentValidationEngine.Builder()
					.addValidator(validator1)
					.addValidator(validator2)
					.context(context)
					.build();

			Component component = new FakeComponent(id("test"));
			engine.validate(component);

			assertEquals(1, validator1.callCount);
			assertEquals(1, validator2.callCount);
		}

		@Test
		@DisplayName("aggregates results from all validators")
		void aggregatesResultsFromAllValidators() {
			ComponentValidator validator1 = (comp, ctx) -> ValidationResult.of(List.of(
					ValidationIssue.error(comp.id(), "Error from validator 1")
			));
			ComponentValidator validator2 = (comp, ctx) -> ValidationResult.of(List.of(
					ValidationIssue.warning(comp.id(), "Warning from validator 2")
			));

			ComponentValidationEngine engine = new ComponentValidationEngine.Builder()
					.addValidator(validator1)
					.addValidator(validator2)
					.context(context)
					.build();

			Component component = new FakeComponent(id("test"));
			ValidationResult result = engine.validate(component);

			assertEquals(2, result.issues().size());
			assertEquals(1, result.errorCount());
			assertEquals(1, result.warningCount());
		}
	}

	@Nested
	@DisplayName("Batch Validation")
	class BatchValidation {

		@Test
		@DisplayName("validateAll processes all components")
		void validateAllProcessesAllComponents() {
			CountingValidator validator = new CountingValidator(ValidationResult.empty());

			ComponentValidationEngine engine = new ComponentValidationEngine.Builder()
					.addValidator(validator)
					.context(context)
					.build();

			List<Component> components = List.of(
					new FakeComponent(id("test1")),
					new FakeComponent(id("test2")),
					new FakeComponent(id("test3"))
			);

			engine.validateAll(components);

			assertEquals(3, validator.callCount);
		}

		@Test
		@DisplayName("validateAll aggregates results from all components")
		void validateAllAggregatesResultsFromAllComponents() {
			ComponentValidator validator = (comp, ctx) -> ValidationResult.of(List.of(
					ValidationIssue.error(comp.id(), "Error for " + comp.id())
			));

			ComponentValidationEngine engine = new ComponentValidationEngine.Builder()
					.addValidator(validator)
					.context(context)
					.build();

			List<Component> components = List.of(
					new FakeComponent(id("test1")),
					new FakeComponent(id("test2"))
			);

			ValidationResult result = engine.validateAll(components);

			assertEquals(2, result.errorCount());
		}
	}

	@Nested
	@DisplayName("Error Handling")
	class ErrorHandling {

		@Test
		@DisplayName("catches validator exceptions and reports as errors")
		void catchesValidatorExceptionsAndReportsAsErrors() {
			ComponentValidator throwingValidator = (comp, ctx) -> {
				throw new RuntimeException("Validator crashed!");
			};

			ComponentValidationEngine engine = new ComponentValidationEngine.Builder()
					.addValidator(throwingValidator)
					.context(context)
					.build();

			Component component = new FakeComponent(id("test"));
			ValidationResult result = engine.validate(component);

			assertTrue(result.hasErrors());
			assertTrue(result.errors().get(0).message().contains("Validator"));
			assertTrue(result.errors().get(0).message().contains("crashed"));
		}
	}

	@Nested
	@DisplayName("Builder")
	class BuilderBehavior {

		@Test
		@DisplayName("throws when context is missing")
		void throwsWhenContextIsMissing() {
			ComponentValidationEngine.Builder builder = new ComponentValidationEngine.Builder()
					.addValidator((comp, ctx) -> ValidationResult.empty());

			assertThrows(IllegalStateException.class, builder::build);
		}

		@Test
		@DisplayName("addValidators adds multiple validators")
		void addValidatorsAddsMultipleValidators() {
			ComponentValidator v1 = (comp, ctx) -> ValidationResult.empty();
			ComponentValidator v2 = (comp, ctx) -> ValidationResult.empty();

			ComponentValidationEngine engine = new ComponentValidationEngine.Builder()
					.addValidators(List.of(v1, v2))
					.context(context)
					.build();

			assertEquals(2, engine.validators().size());
		}

		@Test
		@DisplayName("context accessor returns the set context")
		void contextAccessorReturnsTheSetContext() {
			ComponentValidationEngine engine = new ComponentValidationEngine.Builder()
					.context(context)
					.build();

			assertSame(context, engine.context());
		}
	}
}
