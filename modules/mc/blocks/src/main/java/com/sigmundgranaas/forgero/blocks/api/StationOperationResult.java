package com.sigmundgranaas.forgero.blocks.api;

import com.sigmundgranaas.forgero.core.component.api.Component;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Result type for station operations (install, remove, swap upgrades).
 * <p>
 * This sealed interface provides type-safe handling of operation outcomes
 * with proper error messages for the UI layer.
 * <p>
 * Usage:
 * <pre>{@code
 * StationOperationResult result = handler.installUpgrade(tool, slotId, upgradeStack);
 *
 * switch (result) {
 *     case Success s -> {
 *         Component updated = s.component();
 *         ItemStack stack = s.stack();
 *         // Update UI with new state
 *     }
 *     case Failure f -> {
 *         Text errorText = f.toText();
 *         // Show error message to player
 *     }
 *     case NoOp n -> {
 *         // Nothing changed
 *     }
 * }
 * }</pre>
 */
public sealed interface StationOperationResult {

	/**
	 * Successful operation result.
	 *
	 * @param component The updated component after the operation
	 * @param stack     The updated ItemStack representation
	 */
	record Success(Component component, ItemStack stack) implements StationOperationResult {
		/**
		 * Returns true indicating success.
		 */
		public boolean isSuccess() {
			return true;
		}
	}

	/**
	 * Failed operation result with translatable error message.
	 *
	 * @param errorKey Translation key for the error message
	 * @param args     Arguments for the translation
	 */
	record Failure(String errorKey, Object... args) implements StationOperationResult {
		/**
		 * Converts this failure to a translatable Text component for UI display.
		 *
		 * @return Translatable text with error message
		 */
		public Text toText() {
			return Text.translatable(errorKey, args);
		}

		/**
		 * Returns false indicating failure.
		 */
		public boolean isSuccess() {
			return false;
		}

		/**
		 * Common failure: no compatible slot found.
		 */
		public static Failure noCompatibleSlot() {
			return new Failure("forgero.station.error.no_compatible_slot");
		}

		/**
		 * Common failure: slot is already filled.
		 */
		public static Failure slotAlreadyFilled() {
			return new Failure("forgero.station.error.slot_already_filled");
		}

		/**
		 * Common failure: invalid upgrade for slot.
		 */
		public static Failure invalidUpgrade(String slotType) {
			return new Failure("forgero.station.error.invalid_upgrade", slotType);
		}

		/**
		 * Common failure: slot not found.
		 */
		public static Failure slotNotFound(String slotId) {
			return new Failure("forgero.station.error.slot_not_found", slotId);
		}

		/**
		 * Common failure: conversion failed.
		 */
		public static Failure conversionFailed() {
			return new Failure("forgero.station.error.conversion_failed");
		}

		/**
		 * Common failure: not a valid Forgero item.
		 */
		public static Failure notForgeroItem() {
			return new Failure("forgero.station.error.not_forgero_item");
		}
	}

	/**
	 * No-operation result - nothing changed.
	 * <p>
	 * This is returned when an operation completes but has no effect,
	 * such as trying to remove an upgrade from an already empty slot.
	 */
	record NoOp() implements StationOperationResult {
		/**
		 * Singleton instance for no-op results.
		 */
		public static final NoOp INSTANCE = new NoOp();
	}

	/**
	 * Checks if this result represents a successful operation.
	 *
	 * @return true if Success, false otherwise
	 */
	default boolean isSuccess() {
		return this instanceof Success;
	}

	/**
	 * Returns the component if successful, or throws if failed.
	 *
	 * @return The updated component
	 * @throws IllegalStateException if not a Success
	 */
	default Component componentOrThrow() {
		if (this instanceof Success s) {
			return s.component();
		}
		throw new IllegalStateException("Operation failed: " + this);
	}

	/**
	 * Returns the stack if successful, or throws if failed.
	 *
	 * @return The updated ItemStack
	 * @throws IllegalStateException if not a Success
	 */
	default ItemStack stackOrThrow() {
		if (this instanceof Success s) {
			return s.stack();
		}
		throw new IllegalStateException("Operation failed: " + this);
	}
}
