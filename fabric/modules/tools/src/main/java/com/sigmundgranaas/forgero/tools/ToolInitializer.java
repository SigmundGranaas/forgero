package com.sigmundgranaas.forgero.tools;

import com.sigmundgranaas.forgero.common.item.ItemToComponentMapper;
import com.sigmundgranaas.forgero.common.nbt.ComponentNbtConverter;
import com.sigmundgranaas.forgero.common.registrar.ForgeroDataRegistrar;
import com.sigmundgranaas.forgero.common.registrar.ItemCreator;
import com.sigmundgranaas.forgero.common.service.ComponentService;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.component.api.CustomizableComponent;
import com.sigmundgranaas.forgero.core.component.api.slot.UpgradeSlot;
import com.sigmundgranaas.forgero.core.component.mutation.api.ComponentMutater;
import com.sigmundgranaas.forgero.core.component.mutation.impl.ComponentMutaterImpl;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.registry.ComponentRegistry;
import com.sigmundgranaas.forgero.core.registry.impl.MapBackedComponentRegistry;
import com.sigmundgranaas.forgero.data.loading.api.data.host.CreateData;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataBundle;
import com.sigmundgranaas.forgero.data.pipeline.api.ForgeroDataInitializer;
import com.sigmundgranaas.forgero.tools.item.ForgeroPartItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroPickaxeItem;
import com.sigmundgranaas.forgero.tools.item.ForgeroToolMaterial;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ToolInitializer implements ModInitializer {
	public static final String MOD_NAMESPACE = "forgero";
	public static final Logger LOGGER = LoggerFactory.getLogger(ToolInitializer.class);
	public static final String PICKAXE_ITEM_CLASS = "forgero:pickaxe_item";
	public static final String PART_ITEM_CLASS = "forgero:part_item";

	@Override
	public void onInitialize() {
		long totalStartTime = System.currentTimeMillis();
		LOGGER.info("Starting Forgero Tools module initialization.");

		ForgeroDataInitializer dataInitializer = new ForgeroDataInitializer(MOD_NAMESPACE);
		ForgeroDataBundle bundle = dataInitializer.getDataBundle();
		LOGGER.info("Data bundle loaded with {} component entries.", bundle.componentRegistry().all().size());

		ComponentRegistry components = new MapBackedComponentRegistry(bundle.componentRegistry().all().stream().collect(Collectors.toMap(Component::id, Function.identity())));
		// Initialize services in the correct order
		ComponentNbtConverter.initialize(components);
		ItemToComponentMapper.initialize(bundle);
		ComponentService.INSTANCE.initialize(components);
		LOGGER.info("Forgero services initialized.");

		ForgeroDataRegistrar registrar = new ForgeroDataRegistrar(LOGGER);

		Map<String, ItemCreator> creators = new HashMap<>();
		creators.put(PICKAXE_ITEM_CLASS, this::createPickaxeItem);
		creators.put(PART_ITEM_CLASS, this::createPartItem);

		registrar.process(bundle, creators);

		UseItemCallback.EVENT.register(this::onUseItem);
		LOGGER.info("Registered item use callback for slotting.");

		long totalEndTime = System.currentTimeMillis();
		LOGGER.info("Forgero Tools module initialization complete. Took {}ms.", totalEndTime - totalStartTime);
	}

	private TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
		if (world.isClient() || hand != Hand.MAIN_HAND) {
			return TypedActionResult.pass(player.getStackInHand(hand));
		}

		ItemStack mainHandStack = player.getStackInHand(Hand.MAIN_HAND);
		ItemStack offHandStack = player.getStackInHand(Hand.OFF_HAND);

		ComponentService service = ComponentService.INSTANCE;
		Optional<Component> mainComponentOpt = service.getComponent(mainHandStack);
		Optional<Component> offHandComponentOpt = service.getComponent(offHandStack);

		// We only care if the main hand item is a Forgero component.
		// The offhand item can be anything, as long as it maps to a component.
		if (mainComponentOpt.isEmpty() || offHandComponentOpt.isEmpty()) {
			return TypedActionResult.pass(mainHandStack);
		}

		Component mainComponent = mainComponentOpt.get();
		Component offHandComponent = offHandComponentOpt.get();

		if (!(mainComponent instanceof CustomizableComponent customizable)) {
			// This check is now more useful, as it will trigger for things that are components but don't have slots.
			return TypedActionResult.pass(mainHandStack);
		}

		// Find the first empty, compatible slot.
		for (UpgradeSlot slot : customizable.getUpgradeSlots()) {
			if (slot.content().isEmpty() && slot.validator().test(offHandComponent)) {
				ComponentMutater mutater = new ComponentMutaterImpl();
					Component newMainComponent = mutater.setSlot(mainComponent, slot.id(), offHandComponent);
					Identifier newId = new Identifier(newMainComponent.id().namespace(), newMainComponent.id().path());
					Item newItem = Registries.ITEM.get(newId);

					if (newItem == Items.AIR) {
						LOGGER.warn("Failed to create upgraded item. No item registered for ID: {}", newId);
						player.sendMessage(Text.translatable("forgero.upgrade.error"), true);
						return TypedActionResult.fail(mainHandStack);
					}

					ItemStack newStack = new ItemStack(newItem);
					NbtCompound newNbt = ComponentNbtConverter.getInstance().toNbt(newMainComponent);
					newStack.setNbt(newNbt);

					player.setStackInHand(Hand.MAIN_HAND, newStack);
					if (!player.isCreative()) {
						offHandStack.decrement(1);
					}

					player.sendMessage(Text.translatable("forgero.upgrade.success", offHandComponent.id().path(), mainComponent.id().path()), true);
					return TypedActionResult.success(newStack, true);
			}
		}

		// Fail silently if no slotting action was taken. This prevents the "eating" animation on right-click.
		return TypedActionResult.pass(mainHandStack);
	}


	private Item createPickaxeItem(Component component, CreateData data, Resolver resolver) {
		ForgeroToolMaterial material = new ForgeroToolMaterial(component, resolver);
		return new ForgeroPickaxeItem(material, new Item.Settings(), component, resolver);
	}

	private Item createPartItem(Component component, CreateData data, Resolver resolver) {
		return new ForgeroPartItem(new Item.Settings(), component, resolver);
	}
}
