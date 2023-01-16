package com.sigmundgranaas.forgero.minecraft.common.property;

import com.sigmundgranaas.forgero.core.property.passive.LeveledPassiveType;
import com.sigmundgranaas.forgero.minecraft.common.conversion.StateConverter;
import com.sigmundgranaas.forgero.minecraft.common.property.handler.TeleportHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemUseHandler implements Runnable {
   private final World world;
   private final PlayerEntity user;
   private final Hand hand;
   private final ItemStack stack;

    public ItemUseHandler(World world, PlayerEntity user, Hand hand, ItemStack stack) {
        this.world = world;
        this.user = user;
        this.hand = hand;
        this.stack = stack;
    }

    @Override
    public void run() {
       var optState = StateConverter.of(stack);
        if(optState.isPresent()){
            var state = optState.get();
            var properties = state.stream().getLeveledPassiveProperties().toList();
            if(properties.size() > 0){
                var teleport = properties.stream().filter(prop -> prop.type() == (LeveledPassiveType.ENDER_TELEPORT)).findAny();
                if(teleport.isPresent()){
                    TeleportHandler.execute(5, world, user);
                }
            }
        }
    }
}
