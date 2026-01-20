package com.sigmundgranaas.forgero.tools;


import com.sigmundgranaas.forgero.common.api.DataLoadingContext;
import com.sigmundgranaas.forgero.common.api.PostLoadPlugin;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevPlugin implements PostLoadPlugin {
	public static final Logger LOGGER = LoggerFactory.getLogger(DevPlugin.class);

	@Override
	public String getId() {
		return "forgero:development-post-plugin";
	}

	@Override
	public void onDataLoaded(DataLoadingContext context) {
		ComponentSlottingHandler slottingHandler = new ComponentSlottingHandler(context.converter());
		UseItemCallback.EVENT.register(slottingHandler::handle);

		ServerPlayNetworking.registerGlobalReceiver(UpgradeComponentPacket.ID, (server, player, handler, buf, responseSender) -> {
			Hand hand = UpgradeComponentPacket.readHand(buf);
			NbtCompound newNbt = UpgradeComponentPacket.readNbt(buf);

			server.execute(() -> {
				ItemStack stackInHand = player.getStackInHand(hand);

				if (!stackInHand.isEmpty() && context.converter().toComponent(stackInHand).isPresent()) {
					ItemStack newStack = stackInHand.copy();
					newStack.setNbt(newNbt);
					player.setStackInHand(hand, newStack);
				}
			});
		});
		LOGGER.debug("Development plugin initialized");
	}
}
