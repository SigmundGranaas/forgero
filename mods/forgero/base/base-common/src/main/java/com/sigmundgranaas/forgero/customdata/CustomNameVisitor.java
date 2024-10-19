package com.sigmundgranaas.forgero.customdata;

import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.customdata.ClassBasedVisitor;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.state.State;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * A visitor for {@link NameReplacementData}.
 * <p>
 * This class is designed to extract {@link NameReplacementData} from a {@link DataContainer}, which indicates parts of a name
 * to be replaced for dynamic Forgero tools and weapons.
 * The name replacements are applied to a string (e.g., name of an {@link State}) using {@link NameReplacementData#replace(String)}.
 * Example:
 * <p>
 * Given the following custom data:
 *
 * <pre>
 * "custom_data": {
 *     "name_replacement": {
 *       "from": "sword",
 *       "to": "knife"
 *     }
 * }
 * </pre>
 * <p>
 * An item with the name "cobblestone-sword" will be transformed into "cobblestone-knife".
 */
public class CustomNameVisitor extends ClassBasedVisitor<CustomNameVisitor.NameReplacementData> {
	public static final String KEY = "name_replacement";

	private CustomNameVisitor() {
		super(NameReplacementData.class, KEY);
	}

	/**
	 * Extracts the first instance of {@link NameReplacementData} from the provided {@link DataContainer}.
	 *
	 * @param dataContainer The container possibly containing name replacement data.
	 * @return The first name replacement data, if present.
	 */
	public static Optional<NameReplacementData> of(DataContainer dataContainer) {
		return dataContainer.accept(new CustomNameVisitor());
	}

	/**
	 * Extracts all instances of {@link NameReplacementData} from the provided {@link DataContainer}.
	 *
	 * @param dataContainer The container possibly containing multiple name replacement data.
	 * @return All name replacements found, if any.
	 */
	public static List<NameReplacementData> ofAll(DataContainer dataContainer) {
		return new CustomNameVisitor().visitMultiple(dataContainer);
	}

	/**
	 * A data structure for storing name replacement information.
	 */
	@Data
	@Accessors(fluent = true)
	public static class NameReplacementData {
		private String from;
		private String to;

		/**
		 * Replaces occurrences of the "from" string with the "to" string in the provided name.
		 *
		 * @param name The original name/string where replacements should be performed.
		 * @return The modified name with replacements.
		 */
		public String replace(String name) {
			return name.replace(from, to);
		}
	}
}
