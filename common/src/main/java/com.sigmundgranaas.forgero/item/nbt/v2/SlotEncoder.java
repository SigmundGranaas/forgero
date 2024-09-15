package com.sigmundgranaas.forgero.item.nbt.v2;

import static com.sigmundgranaas.forgero.item.nbt.v2.NbtConstants.*;

import java.util.Set;

import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.AbstractTypedSlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

/**
 * Encoder class to handle the encoding of Slot objects into Minecraft's NBT (Named Binary Tag) format.
 * It supports the encoding of both {@link EmptySlot} and {@link FilledSlot} objects and utilizes the {@link StateEncoder}
 * for handling specific encodings within the objects.
 *
 * <p>This class implements the {@link CompoundEncoder} interface, providing a standardized way to translate
 * Slot objects into their NBT format. This encoding is essential for saving and transmitting these objects
 * within the Minecraft environment.
 *
 * <p>The encoded NBT format includes various keys to represent the object's properties:
 * <ul>
 *     <li>"codecType": Represents the type of slot, e.g., "forgero:empty_slot" or "forgero:filled_slot"</li>
 *     <li>"index": The index of the slot</li>
 *     <li>"description": The description of the slot</li>
 *     <li>"categories": The set of categories associated with the slot, encoded as a list of strings</li>
 *     <li>"upgrade": Encoded state object present in the filled slot</li>
 * </ul>
 *
 * <p>Usage Example:
 * <pre>
 * {@code
 * Slot slot = ...;
 * StateEncoder stateEncoder = ...;
 * SlotEncoder encoder = new SlotEncoder(stateEncoder);
 * NbtCompound compound = encoder.encode(slot);
 * }
 * </pre>
 *
 * @see CompoundEncoder
 * @see EmptySlot
 * @see FilledSlot
 * @see StateEncoder
 */
public class SlotEncoder implements CompoundEncoder<Slot> {


	public static final String VALUE_EMPTY_SLOT_TYPE = "forgero:empty_slot";
	public static final String VALUE_FILLED_SLOT_TYPE = "forgero:filled_slot";

	private final StateEncoder stateEncoder;

	/**
	 * Constructor to initialize SlotEncoder with the state encoder.
	 *
	 * @param stateEncoder the encoder for states
	 */
	public SlotEncoder(@NotNull StateEncoder stateEncoder) {
		this.stateEncoder = stateEncoder;
	}

	/**
	 * Encodes the given Slot object into an {@link NbtCompound}.
	 *
	 * <p>This method identifies the specific type of Slot (either EmptySlot or FilledSlot) and
	 * encodes it accordingly. In the case of a FilledSlot, it also leverages the {@link StateEncoder}
	 * to encode the 'upgrade' field.
	 *
	 * @param element The Slot object to be encoded.
	 * @return The encoded NbtCompound representing the Slot.
	 */
	@Override
	@NotNull
	public NbtCompound encode(@NotNull Slot element) {
		if (element instanceof EmptySlot emptySlot) {
			return encodeEmptySlot(emptySlot);
		} else if (element instanceof FilledSlot filledSlot) {
			return encodeFilledSlot(filledSlot);
		}
		return stateEncoder.encode(element);
	}

	@NotNull
	private NbtCompound encodeEmptySlot(@NotNull EmptySlot emptySlot) {
		NbtCompound compound = encodeAbstractSlot(emptySlot);
		compound.putString(KEY_CODEC_TYPE, VALUE_EMPTY_SLOT_TYPE);
		return compound;
	}

	@NotNull
	private NbtCompound encodeFilledSlot(@NotNull FilledSlot filledSlot) {
		NbtCompound compound = encodeAbstractSlot(filledSlot);
		compound.putString(KEY_CODEC_TYPE, VALUE_FILLED_SLOT_TYPE);
		compound.put(KEY_UPGRADE, stateEncoder.encode(filledSlot.content()));
		return compound;
	}

	@NotNull
	private NbtCompound encodeAbstractSlot(@NotNull AbstractTypedSlot typedSlot) {
		NbtCompound compound = new NbtCompound();
		compound.putInt(KEY_INDEX, typedSlot.index());
		compound.putString(KEY_DESCRIPTION, typedSlot.description());
		compound.put(KEY_CATEGORIES, encodeCategories(typedSlot.category()));
		compound.putString(KEY_TYPE, typedSlot.type().typeName());
		return compound;
	}

	@NotNull
	private NbtList encodeCategories(@NotNull Set<Category> categories) {
		NbtList list = new NbtList();
		for (Category category : categories) {
			list.add(NbtString.of(category.toString()));
		}
		return list;
	}
}
