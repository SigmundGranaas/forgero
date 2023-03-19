package com.sigmundgranaas.forgero.minecraft.common.customdata;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.sigmundgranaas.forgero.core.customdata.ClassBasedVisitor;
import com.sigmundgranaas.forgero.core.customdata.DataContainer;
import com.sigmundgranaas.forgero.core.customdata.DataVisitor;
import com.sigmundgranaas.forgero.core.property.Target;

import com.sigmundgranaas.forgero.core.property.TargetTypes;
import com.sigmundgranaas.forgero.core.property.attribute.TypeTarget;
import com.sigmundgranaas.forgero.core.resource.data.PropertyPojo;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.utils.RegistryUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;

/**
 * A {@link DataVisitor} for {@link EnchantmentData}.
 * <p>
 * This visitor is used to extract {@link EnchantmentData} from a {@link DataContainer}.
 * The enchantments are applied to the {@link ItemStack} using {@link EnchantmentData#embed(ItemStack)}.
 */
public class EnchantmentVisitor extends ClassBasedVisitor<EnchantmentVisitor.EnchantmentData> {
	public static final String KEY = "enchantment";

	private EnchantmentVisitor() {
		super(EnchantmentData.class, KEY);
	}

	/**
	 * Will extract the first {@link EnchantmentData} from the {@link DataContainer}.
	 *
	 * @param dataContainer any container can contain enchantments
	 * @return the first enchantment, if present
	 */
	public static Optional<EnchantmentData> of(DataContainer dataContainer) {
		return dataContainer.accept(new EnchantmentVisitor());
	}

	/**
	 * Will extract all {@link EnchantmentData} from the {@link DataContainer}.
	 *
	 * @param dataContainer any container can contain enchantments
	 * @return all enchantments, if present
	 */
	public static List<EnchantmentData> ofAll(DataContainer dataContainer) {
		return new EnchantmentVisitor().visitMultiple(dataContainer);
	}

	/**
	 * A data class for storing enchantment data.
	 */
	@Data
	@Accessors(fluent = true)
	public static class EnchantmentData {
		private String id;
		private int level;
		@Nullable
		private PropertyPojo.Condition condition;

		/**
		 * Embeds the enchantment into the {@link ItemStack} using native methods.
		 *
		 * @param stack the stack to embed the enchantment into
		 */
		public void embed(ItemStack stack) {
			RegistryUtils.safeId(id())
					.flatMap(id -> RegistryUtils.safeRegistryLookup(Registry.ENCHANTMENT, id))
					.filter(enchant -> enchant.isAcceptableItem(stack))
					.filter(enchant -> isConditionMet(stack))
					.ifPresent(enchant -> stack.addEnchantment(enchant, level()));
		}

		public boolean isConditionMet(ItemStack stack) {
			if(condition == null) {
				return true;
			}
			Target target =	 new TypeTarget(Set.of(StateConverter.of(stack).orElseThrow().type().toString()));

			return target.isApplicable(new HashSet<>(condition.tag), TargetTypes.TYPE);
		}
	}
}
