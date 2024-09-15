package com.sigmundgranaas.forgero.item.nbt.v2;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.STATE_TYPE_IDENTIFIER;
import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.UPGRADES_IDENTIFIER;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.sigmundgranaas.forgero.core.state.Composite;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.SlotContainer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;


/**
 * Slot Container Parser: Responsible for parsing slot containers, including both legacy and new formats
 *
 * <p>This class is responsible for parsing the slot container from the given NBT compound. It considers
 * both legacy formats (represented by strings or custom state objects) and new formats (represented by slots).
 * Depending on the detection, the parser follows either the legacy path or the new format path.
 *
 * <p>Keys Used:
 * <ul>
 *     <li>"UPGRADES_IDENTIFIER": Identifier for the upgrades section in the compound.</li>
 *     <li>"STATE_TYPE_IDENTIFIER": Identifier for the state type within the compound.</li>
 * </ul>
 *
 * <p>Usage Example:
 * <pre>
 * {@code
 * Composite defaultState = ...;
 * SlotParser slotParser = ...;
 * CompositeParser compositeParser = ...;
 * SlotContainerParser parser = new SlotContainerParser(defaultState, slotParser, compositeParser);
 * Optional<SlotContainer> slotContainer = parser.parse(compound);
 * }
 * </pre>
 *
 * @see Composite
 * @see SlotParser
 * @see CompositeParser
 */
public class SlotContainerParser implements CompoundParser<SlotContainer> {
	private final Composite defaultState;
	private final CompositeParser compositeParser;
	private final SlotParser slotParser;

	/**
	 * Constructor to initialize SlotContainerParser with the required parsers
	 *
	 * @param defaultState    the default state of the composite
	 * @param slotParser      the parser for individual slots
	 * @param compositeParser the parser for composites
	 */
	public SlotContainerParser(Composite defaultState, SlotParser slotParser, CompositeParser compositeParser) {
		this.defaultState = defaultState;
		this.compositeParser = compositeParser;
		this.slotParser = slotParser;
	}

	@Override
	public Optional<SlotContainer> parse(NbtCompound compound) {
		if (compound.contains(UPGRADES_IDENTIFIER) && containsAnyLegacy(compound)) {
			return parseLegacy(compound);
		} else {
			return parseSlots(compound.getList(NbtConstants.UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE));
		}
	}

	/**
	 * Checks if the compound contains any legacy format
	 *
	 * @param compound the NBT compound to check
	 * @return true if legacy format is detected, false otherwise
	 */
	private boolean containsAnyLegacy(@Nullable NbtCompound compound) {
		if (compound == null) {
			return false;
		}
		boolean isLegacy = false;
		if (compound.contains(NbtConstants.UPGRADES_IDENTIFIER)) {
			isLegacy = compound.getList(NbtConstants.UPGRADES_IDENTIFIER, NbtElement.COMPOUND_TYPE).stream().anyMatch(this::isLegacy)
					|| compound.getList(NbtConstants.UPGRADES_IDENTIFIER, NbtElement.STRING_TYPE).stream().anyMatch(this::isLegacy);
		}
		return isLegacy;
	}

	/**
	 * Checks if the given NbtElement is in legacy format
	 *
	 * @param element the NBT element to check
	 * @return true if the element is in legacy format, false otherwise
	 */
	private boolean isLegacy(@Nullable NbtElement element) {
		if (element == null) {
			return true;
		}
		if (element.getType() == NbtElement.STRING_TYPE) {
			return true;
		} else
			return element.getType() == NbtElement.COMPOUND_TYPE && ((NbtCompound) element).contains(STATE_TYPE_IDENTIFIER);
	}


	/**
	 * Parses the legacy format of upgrades
	 *
	 * @param upgrades the NBT compound containing upgrades
	 * @return the parsed SlotContainer wrapped in an Optional
	 */
	public Optional<SlotContainer> parseLegacy(NbtCompound upgrades) {
		ArrayList<State> states = new ArrayList<>();
		var slotContainer = defaultState.getSlotContainer().copy();
		compositeParser.parseUpgrades(states::add, upgrades);
		states.forEach(slotContainer::set);
		return Optional.of(slotContainer);
	}

	/**
	 * Parses the new format of slots
	 *
	 * @param upgrades the NbtList containing the slots
	 * @return the parsed SlotContainer wrapped in an Optional
	 */
	public Optional<SlotContainer> parseSlots(NbtList upgrades) {
		List<Slot> slots = upgrades.stream()
				.filter(NbtCompound.class::isInstance)
				.map(NbtCompound.class::cast)
				.map(slotParser::parse)
				.flatMap(Optional::stream)
				.sorted(Comparator.comparingInt(Slot::index))
				.collect(Collectors.toList());
		return Optional.of(new SlotContainer(slots));
	}
}
