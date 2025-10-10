package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.loader.api.ForgeroApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class DevClientPlugin implements ClientModInitializer {
	private static KeyBinding viewUpgradesKey;

	@Override
	public void onInitializeClient() {
		viewUpgradesKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.forgero.view_upgrades", // The translation key of the keybinding's name
				InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
				GLFW.GLFW_KEY_U, // The default keycode of the keybinding.
				"category.forgero.dev" // The translation key of the keybinding's category.
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (viewUpgradesKey.wasPressed()) {
				if (client.player == null) {
					continue;
				}

				ItemStack mainHandStack = client.player.getMainHandStack();
				if (mainHandStack.isEmpty()) {
					continue;
				}

				ForgeroApi.converter().toComponent(mainHandStack)
						.filter(CustomizableComponent.class::isInstance)
						.map(CustomizableComponent.class::cast)
						.ifPresent(component -> {
							if (!component.getUpgradeSlots().isEmpty()) {
								client.setScreen(new ComponentUpgradeScreen(component, ForgeroApi.converter(), Hand.MAIN_HAND));
							}
						});
			}
		});
	}
}
