package com.sigmundgranaas.forgero.blocks.integration;

import com.sigmundgranaas.forgero.blocks.api.StationOperationResult;
import com.sigmundgranaas.forgero.blocks.api.StationOperationResult.*;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for slot validation scenarios in the upgrade station.
 * <p>
 * These tests verify that the StationOperationResult type correctly
 * represents various validation outcomes.
 */
class SlotValidationTest {

	@Nested
	@DisplayName("Success Result")
	class SuccessResultTests {

		@Test
		@DisplayName("Success isSuccess returns true")
		void success_isSuccess() {
			StationOperationResult result = new Success(null, null);
			assertTrue(result.isSuccess());
		}

		@Test
		@DisplayName("Success can hold component and stack")
		void success_holdsData() {
			Success result = new Success(null, null);
			assertNull(result.component());
			assertNull(result.stack());
		}
	}

	@Nested
	@DisplayName("Failure Result")
	class FailureResultTests {

		@Test
		@DisplayName("Failure isSuccess returns false")
		void failure_isNotSuccess() {
			StationOperationResult result = Failure.noCompatibleSlot();
			assertFalse(result.isSuccess());
		}

		@Test
		@DisplayName("Failure.noCompatibleSlot has correct error key")
		void noCompatibleSlot_correctKey() {
			Failure failure = Failure.noCompatibleSlot();
			assertEquals("forgero.station.error.no_compatible_slot", failure.errorKey());
		}

		@Test
		@DisplayName("Failure.slotAlreadyFilled has correct error key")
		void slotAlreadyFilled_correctKey() {
			Failure failure = Failure.slotAlreadyFilled();
			assertEquals("forgero.station.error.slot_already_filled", failure.errorKey());
		}

		@Test
		@DisplayName("Failure.invalidUpgrade includes slot type in args")
		void invalidUpgrade_includesSlotType() {
			Failure failure = Failure.invalidUpgrade("gem");
			assertEquals("forgero.station.error.invalid_upgrade", failure.errorKey());
			assertEquals(1, failure.args().length);
			assertEquals("gem", failure.args()[0]);
		}

		@Test
		@DisplayName("Failure.slotNotFound includes slot ID")
		void slotNotFound_includesSlotId() {
			Failure failure = Failure.slotNotFound("slot_gem_1");
			assertEquals("forgero.station.error.slot_not_found", failure.errorKey());
			assertEquals("slot_gem_1", failure.args()[0]);
		}

		@Test
		@DisplayName("Failure.conversionFailed has correct key")
		void conversionFailed_correctKey() {
			Failure failure = Failure.conversionFailed();
			assertEquals("forgero.station.error.conversion_failed", failure.errorKey());
		}

		@Test
		@DisplayName("Failure.notForgeroItem has correct key")
		void notForgeroItem_correctKey() {
			Failure failure = Failure.notForgeroItem();
			assertEquals("forgero.station.error.not_forgero_item", failure.errorKey());
		}

		@Test
		@DisplayName("Custom failure with multiple args")
		void customFailure_multipleArgs() {
			Failure failure = new Failure("forgero.station.error.custom", "arg1", 42, "arg3");
			assertEquals("forgero.station.error.custom", failure.errorKey());
			assertEquals(3, failure.args().length);
			assertEquals("arg1", failure.args()[0]);
			assertEquals(42, failure.args()[1]);
			assertEquals("arg3", failure.args()[2]);
		}

		@Test
		@DisplayName("Failure toText returns non-null")
		void failure_toTextNotNull() {
			Failure failure = Failure.noCompatibleSlot();
			assertNotNull(failure.toText());
		}

		@Test
		@DisplayName("componentOrThrow on Failure throws IllegalStateException")
		void failure_componentOrThrow_throws() {
			Failure failure = Failure.noCompatibleSlot();
			assertThrows(IllegalStateException.class, failure::componentOrThrow);
		}
	}

	@Nested
	@DisplayName("NoOp Result")
	class NoOpResultTests {

		@Test
		@DisplayName("NoOp isSuccess returns false")
		void noOp_isNotSuccess() {
			StationOperationResult result = NoOp.INSTANCE;
			assertFalse(result.isSuccess());
		}

		@Test
		@DisplayName("NoOp INSTANCE is available")
		void noOp_instanceAvailable() {
			assertNotNull(NoOp.INSTANCE);
		}

		@Test
		@DisplayName("NoOp instances are equal")
		void noOp_instancesEqual() {
			NoOp a = new NoOp();
			NoOp b = new NoOp();
			assertEquals(a, b);
		}
	}

	@Nested
	@DisplayName("Validation Error Scenarios")
	class ValidationScenariosTests {

		@Test
		@DisplayName("Slot type mismatch produces invalidUpgrade failure")
		void slotTypeMismatch_invalidUpgrade() {
			// Simulate: trying to put a binding in a gem slot
			Failure failure = Failure.invalidUpgrade("forgero:gem");

			assertEquals("forgero.station.error.invalid_upgrade", failure.errorKey());
			assertTrue(failure.args()[0].toString().contains("gem"));
		}

		@Test
		@DisplayName("Trying to install in non-existent slot")
		void nonExistentSlot_slotNotFound() {
			OpenIdentifier slotId = OpenIdentifier.of("non_existent_slot");
			Failure failure = Failure.slotNotFound(slotId.toString());

			assertEquals("forgero.station.error.slot_not_found", failure.errorKey());
			assertTrue(failure.args()[0].toString().contains("non_existent_slot"));
		}

		@Test
		@DisplayName("Installing in already filled slot")
		void filledSlot_slotAlreadyFilled() {
			Failure failure = Failure.slotAlreadyFilled();
			assertEquals("forgero.station.error.slot_already_filled", failure.errorKey());
		}

		@Test
		@DisplayName("Non-Forgero item rejected")
		void nonForgeroItem_notForgeroItem() {
			// Simulate: vanilla item put in upgrade slot
			Failure failure = Failure.notForgeroItem();
			assertEquals("forgero.station.error.not_forgero_item", failure.errorKey());
		}

		@Test
		@DisplayName("All slots full produces noCompatibleSlot")
		void allSlotsFull_noCompatibleSlot() {
			Failure failure = Failure.noCompatibleSlot();
			assertEquals("forgero.station.error.no_compatible_slot", failure.errorKey());
		}
	}

	@Nested
	@DisplayName("Pattern Matching")
	class PatternMatchingTests {

		@Test
		@DisplayName("Can pattern match on Success")
		void patternMatch_success() {
			StationOperationResult result = new Success(null, null);

			String message;
			if (result instanceof Success s) {
				message = "success";
			} else if (result instanceof Failure f) {
				message = "failure: " + f.errorKey();
			} else if (result instanceof NoOp) {
				message = "no-op";
			} else {
				message = "unknown";
			}

			assertEquals("success", message);
		}

		@Test
		@DisplayName("Can pattern match on Failure")
		void patternMatch_failure() {
			StationOperationResult result = Failure.noCompatibleSlot();

			String message;
			if (result instanceof Success s) {
				message = "success";
			} else if (result instanceof Failure f) {
				message = "failure: " + f.errorKey();
			} else if (result instanceof NoOp) {
				message = "no-op";
			} else {
				message = "unknown";
			}

			assertEquals("failure: forgero.station.error.no_compatible_slot", message);
		}

		@Test
		@DisplayName("Can pattern match on NoOp")
		void patternMatch_noOp() {
			StationOperationResult result = NoOp.INSTANCE;

			String message;
			if (result instanceof Success s) {
				message = "success";
			} else if (result instanceof Failure f) {
				message = "failure: " + f.errorKey();
			} else if (result instanceof NoOp) {
				message = "no-op";
			} else {
				message = "unknown";
			}

			assertEquals("no-op", message);
		}
	}
}
