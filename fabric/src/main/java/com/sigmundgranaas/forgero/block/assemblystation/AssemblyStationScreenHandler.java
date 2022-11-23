package com.sigmundgranaas.forgero.block.assemblystation;

import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.state.Composite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;

import static com.sigmundgranaas.forgero.block.assemblystation.AssemblyStationBlock.ASSEMBLY_STATION;

public class AssemblyStationScreenHandler extends ScreenHandler {

    public static ScreenHandlerType<AssemblyStationScreenHandler> ASSEMBLY_STATION_SCREEN_HANDLER;

    static {

        //We use registerSimple here because our Entity is not an ExtendedScreenHandlerFactory
        //but a NamedScreenHandlerFactory.
        //In a later Tutorial you will see what ExtendedScreenHandlerFactory can do!
        ASSEMBLY_STATION_SCREEN_HANDLER = Registry.register(Registry.SCREEN_HANDLER, ASSEMBLY_STATION, new ScreenHandlerType<>(AssemblyStationScreenHandler::new));
    }

    private final SimpleInventory inventory;
    private final ScreenHandlerContext context;
    private final PlayerEntity player;

    //This constructor gets called on the client when the server wants it to open the screenHandler,
    //The client will call the other constructor with an empty Inventory and the screenHandler will automatically
    //sync this empty inventory with the inventory on the server.
    public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    //This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
    //and can therefore directly provide it as an argument. This inventory will then be synced to the client.
    public AssemblyStationScreenHandler(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(AssemblyStationScreenHandler.ASSEMBLY_STATION_SCREEN_HANDLER, syncId);
        this.player = playerInventory.player;
        this.context = context;
        this.inventory = new SimpleInventory(7);
        inventory.addListener(this::onContentChanged);
        //some inventories do custom logic when a player opens it.
        inventory.onOpen(playerInventory.player);

        //This will place the slot in the correct locations for a 3x3 Grid. The slots exist on both server and client!
        //This will not render the background of the slots however, this is the Screens job
        int m;
        int l;

        this.addSlot(new Slot(inventory, 0, 62 + 18, 17 + 18));

        //Our inventory
        for (m = 0; m < 3; ++m) {
            this.addSlot(new Slot(inventory, m + 1, 62 + m * 18, 17));
        }


        for (m = 0; m < 3; ++m) {
            this.addSlot(new Slot(inventory, m + 4, 62 + m * 18, 17 + 2 * 18));
        }


        //The player inventory
        for (m = 0; m < 3; ++m) {
            for (l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 8 + l * 18, 84 + m * 18));
            }
        }
        //The player Hotbar
        for (m = 0; m < 9; ++m) {
            this.addSlot(new Slot(playerInventory, m, 8 + m * 18, 142));
        }

    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    // Shift + Player Inv Slot
    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
        return true;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        if (!inventory.getStack(0).isEmpty() && inventory.getStack(0).getDamage() == 0) {
            ItemStack empty = ItemStack.EMPTY;
            var state = StateConverter.of(inventory.getStack(0));
            this.context.run((world, pos) -> {
                if (!world.isClient) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                    if (state.isPresent() && state.get() instanceof Composite composite) {
                        var elements = composite.disassemble();
                        inventory.setStack(0, empty);
                        setPreviousTrackedSlot(0, empty);
                        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), 0, empty));

                        for (int i = 1; i < elements.size() + 1; i++) {
                            var element = elements.get(i - 1);
                            var newStack = StateConverter.of(element);
                            inventory.setStack(i, newStack);
                            setPreviousTrackedSlot(i, newStack);
                            serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), i, newStack));
                        }
                    }


                }
            });
        }
        super.onContentChanged(inventory);
    }


}
