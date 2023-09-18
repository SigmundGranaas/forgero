package com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2;

import static com.sigmundgranaas.forgero.minecraft.common.item.nbt.v2.NbtConstants.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.sigmundgranaas.forgero.core.property.attribute.Category;
import com.sigmundgranaas.forgero.core.state.Slot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.AbstractTypedSlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.EmptySlot;
import com.sigmundgranaas.forgero.core.state.upgrade.slot.FilledSlot;
import com.sigmundgranaas.forgero.core.type.Type;
import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

/**
 * The {@code SlotParser} class is responsible for parsing {@link Slot} objects
 * from the given NbtCompound format. It forms part of the system that handles
 * the serialization and deserialization of slots in Minecraft's NBT (Named Binary Tag) format.
 *
 * <p>This class adheres to the {@link CompoundParser} interface, making it possible
 * to parse the specific NbtCompound into a Slot object that can represent either an
 * empty or filled slot.
 *
 * <p>For detailed information on the specific NBT structure being parsed,
 * refer to the related classes {@link EmptySlot}, {@link FilledSlot}, and
 * {@link AbstractTypedSlot}.
 *
 * <p>Usage Example:
 * <pre>
 * {@code
 * NbtCompound compound = ...;
 * SlotParser parser = new SlotParser();
 * Optional<Slot> slot = parser.parse(compound);
 * }
 * </pre>
 *
 * @see CompoundParser
 * @see EmptySlot
 * @see FilledSlot
 * @see AbstractTypedSlot
 */
public class SlotParser implements CompoundParser<Slot> {

	private final StateParser stateParser;

	public SlotParser(StateParser stateParser) {
		this.stateParser = stateParser;
	}

	/**
	 * Parses the given {@link NbtCompound} to an Optional {@link Slot} object.
	 *
	 * <p>This method will parse the given NbtCompound, identifying whether it represents
	 * an empty or filled slot, and will create the corresponding Slot object accordingly.
	 * If the NbtCompound does not contain the required information to form a Slot object,
	 * an empty Optional is returned.
	 *
	 * @param compound The NbtCompound to parse.
	 * @return An Optional containing the parsed Slot object if successful; otherwise, an empty Optional.
	 */
	@Override
	public Optional<Slot> parse(@NotNull NbtCompound compound) {
		if (!compound.contains(KEY_CODEC_TYPE)) {
			return Optional.empty();
		}

		int index = compound.getInt(KEY_INDEX);
		String description = compound.getString(KEY_DESCRIPTION);
		Set<Category> categories = parseCategories(compound.getList(KEY_CATEGORIES, NbtElement.STRING_TYPE));
		Type type = Type.of(compound.getString(KEY_TYPE));

		return switch (compound.getString(KEY_CODEC_TYPE)) {
			case VALUE_EMPTY_SLOT_TYPE -> Optional.of(new EmptySlot(index, type, description, categories));
			case VALUE_FILLED_SLOT_TYPE -> stateParser.parse(compound.getCompound(KEY_UPGRADE))
					.map(upgrade -> new FilledSlot(index, type, upgrade, description, categories));
			default -> Optional.empty();
		};
	}

	private Set<Category> parseCategories(NbtList categoryList) {
		Set<Category> categories = new HashSet<>();
		for (int i = 0; i < categoryList.size(); i++) {
			categories.add(Category.valueOf(categoryList.getString(i)));
		}
		return categories;
	}
}
