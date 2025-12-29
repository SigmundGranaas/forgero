package com.sigmundgranaas.forgero.bows;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sigmundgranaas.forgero.bows.entity.DynamicArrowEntityRegistry;
import com.sigmundgranaas.forgero.bows.handlers.ConsumeProjectileHandler;
import com.sigmundgranaas.forgero.bows.handlers.IncrementStatHandler;
import com.sigmundgranaas.forgero.bows.handlers.LaunchProjectileHandler;
import com.sigmundgranaas.forgero.bows.handlers.MountProjectileHandler;
import com.sigmundgranaas.forgero.bows.handlers.PlaySoundHandler;
import com.sigmundgranaas.forgero.bows.item.ForgeroArrowItem;
import com.sigmundgranaas.forgero.bows.item.ForgeroBowItem;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.properties.minecraft.useinteraction.UseInteractionPropertiesPlugin;

import net.minecraft.item.Item;

/**
 * Forgero Bow data plugin.
 * Registers bow item creators and bow-specific UseHandler implementations.
 *
 * <p>This plugin follows the same pattern as {@link com.sigmundgranaas.forgero.armor.ArmorPlugin}:</p>
 * <ul>
 *   <li>Registers item creators for bow items</li>
 *   <li>Registers handlers with UseInteractionPropertiesPlugin</li>
 * </ul>
 *
 * <p>Registered handlers:</p>
 * <ul>
 *   <li>{@link MountProjectileHandler} - Initiates bow use and projectile mounting</li>
 *   <li>{@link LaunchProjectileHandler} - Launches projectiles with attributes</li>
 *   <li>{@link ConsumeProjectileHandler} - Consumes ammunition</li>
 *   <li>{@link PlaySoundHandler} - Plays bow sound effects with pitch variation</li>
 *   <li>{@link IncrementStatHandler} - Tracks bow usage in player statistics</li>
 * </ul>
 */
public class BowPlugin implements DataPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(BowPlugin.class);
	public static final String BOW_ITEM_CLASS = "forgero:bow_item";
	public static final String ARROW_ITEM_CLASS = "forgero:arrow_item";

	static {
		// Register DynamicArrowEntity type FIRST (must be registered before use)
		DynamicArrowEntityRegistry.register();

		// Register bow-specific handlers with the UseInteraction system
		UseInteractionPropertiesPlugin.registerHandler(MountProjectileHandler.TYPE, MountProjectileHandler.CODEC);
		UseInteractionPropertiesPlugin.registerHandler(LaunchProjectileHandler.TYPE, LaunchProjectileHandler.CODEC);
		UseInteractionPropertiesPlugin.registerHandler(ConsumeProjectileHandler.TYPE, ConsumeProjectileHandler.CODEC);
		UseInteractionPropertiesPlugin.registerHandler(PlaySoundHandler.TYPE, PlaySoundHandler.CODEC);
		UseInteractionPropertiesPlugin.registerHandler(IncrementStatHandler.TYPE, IncrementStatHandler.CODEC);
	}

	@Override
	public void register(PluginRegistrationContext context) {
		context.registerItemCreator(BOW_ITEM_CLASS, this::createBowItem);
		context.registerItemCreator(ARROW_ITEM_CLASS, this::createArrowItem);
	}

	@Override
	public String getId() {
		return "forgero:bow-plugin";
	}

	private Item createBowItem(Component component, CreateData data, Resolver resolver) {
		return new ForgeroBowItem(new Item.Settings(), component);
	}

	private Item createArrowItem(Component component, CreateData data, Resolver resolver) {
		return new ForgeroArrowItem(new Item.Settings(), component);
	}
}
