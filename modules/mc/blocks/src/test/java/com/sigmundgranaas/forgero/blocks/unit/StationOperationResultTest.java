package com.sigmundgranaas.forgero.blocks.unit;

import com.sigmundgranaas.forgero.blocks.api.StationOperationResult;
import com.sigmundgranaas.forgero.blocks.api.StationOperationResult.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StationOperationResult sealed interface.
 */
class StationOperationResultTest {

	@Test
	void success_isSuccess_returnsTrue() {
		StationOperationResult result = new Success(null, null);
		assertTrue(result.isSuccess());
	}

	@Test
	void failure_isSuccess_returnsFalse() {
		StationOperationResult result = new Failure("error.key");
		assertFalse(result.isSuccess());
	}

	@Test
	void noOp_isSuccess_returnsFalse() {
		StationOperationResult result = new NoOp();
		assertFalse(result.isSuccess());
	}

	@Test
	void failure_toText_returnsTranslatableText() {
		Failure failure = new Failure("forgero.station.error.test", "arg1", "arg2");
		assertNotNull(failure.toText());
	}

	@Test
	void failure_commonFactories_createCorrectKeys() {
		assertEquals("forgero.station.error.no_compatible_slot", Failure.noCompatibleSlot().errorKey());
		assertEquals("forgero.station.error.slot_already_filled", Failure.slotAlreadyFilled().errorKey());
		assertEquals("forgero.station.error.conversion_failed", Failure.conversionFailed().errorKey());
		assertEquals("forgero.station.error.not_forgero_item", Failure.notForgeroItem().errorKey());
	}

	@Test
	void failure_invalidUpgrade_includesSlotType() {
		Failure failure = Failure.invalidUpgrade("gem");
		assertEquals("forgero.station.error.invalid_upgrade", failure.errorKey());
		assertEquals("gem", failure.args()[0]);
	}

	@Test
	void failure_slotNotFound_includesSlotId() {
		Failure failure = Failure.slotNotFound("slot_1");
		assertEquals("forgero.station.error.slot_not_found", failure.errorKey());
		assertEquals("slot_1", failure.args()[0]);
	}

	@Test
	void success_componentOrThrow_returnsComponent() {
		Success success = new Success(null, null);
		// Would return null in this test case (component is null)
		assertNull(success.componentOrThrow());
	}

	@Test
	void failure_componentOrThrow_throws() {
		Failure failure = Failure.noCompatibleSlot();
		assertThrows(IllegalStateException.class, failure::componentOrThrow);
	}

	@Test
	void noOp_singleton_exists() {
		// NoOp.INSTANCE is the recommended way to get NoOp
		// Note: new NoOp() creates a new instance, use INSTANCE for shared reference
		assertNotNull(NoOp.INSTANCE);
		assertFalse(NoOp.INSTANCE.isSuccess());
		// Two NoOp instances are equals() due to record semantics
		assertEquals(new NoOp(), NoOp.INSTANCE);
	}

	@Test
	void patternMatching_worksCorrectly() {
		StationOperationResult result = Failure.noCompatibleSlot();

		// Java 17 compatible pattern matching using if-else
		String message;
		if (result instanceof Success) {
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
}
