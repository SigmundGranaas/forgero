package com.sigmundgranaas.forgero.block.assemblystation;

import com.sigmundgranaas.forgero.ForgeroStateRegistry;
import com.sigmundgranaas.forgero.conversion.StateConverter;
import com.sigmundgranaas.forgero.state.Composite;
import com.sigmundgranaas.forgero.state.State;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

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

        this.addSlot(new Slot(inventory, 0, 34, 34));

        //Our inventory
        for (m = 0; m < 3; ++m) {
            this.addSlot(new Slot(inventory, m + 1, 92 + m * 18, 17));
        }


        for (m = 0; m < 3; ++m) {
            this.addSlot(new Slot(inventory, m + 4, 92 + m * 18, 53));
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
        boolean isEmpty = IntStream.range(1, 7).allMatch(index -> inventory.getStack(index).isEmpty());
        if (!inventory.getStack(0).isEmpty() && inventory.getStack(0).getDamage() == 0 && isEmpty) {
            ItemStack empty = ItemStack.EMPTY;
            var dissasemblyStack = inventory.getStack(0);
            var state = StateConverter.of(inventory.getStack(0));
            this.context.run((world, pos) -> {
                if (!world.isClient && false) {
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
                    } else if (dissasemblyStack.getItem() == Items.DIAMOND_PICKAXE) {
                        inventory.setStack(0, empty);
                        setPreviousTrackedSlot(0, empty);
                        inventory.setStack(2, new ItemStack(Registry.ITEM.get(new Identifier("forgero:oak-handle"))));
                        inventory.setStack(1, new ItemStack(Registry.ITEM.get(new Identifier("forgero:diamond-pickaxe_head"))));
                        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), 0, empty));
                    }
                }
            });

        } else {
            this.context.run((world, pos) -> {
                if (!world.isClient) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                    List<State> states = IntStream.range(1, 7).mapToObj(index -> StateConverter.of(inventory.getStack(index))).flatMap(Optional::stream).toList();
                    var output = ForgeroStateRegistry.STATES.all().stream()
                            .filter(Composite.class::isInstance)
                            .map(Composite.class::cast)
                            .filter(composite -> composite.ingredients().stream().allMatch(ingredient -> states.stream().anyMatch(state -> state.identifier().equals(ingredient.identifier()))))
                            .map(StateConverter::of)
                            .findAny();
                    if (output.isPresent() && inventory.getStack(0).isEmpty()) {
                        inventory.setStack(0, output.get());
                        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), 0, output.get()));
                    } else if (!inventory.getStack(0).isEmpty() && output.isEmpty()) {
                        inventory.setStack(0, ItemStack.EMPTY);
                        serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.syncId, this.nextRevision(), 0, ItemStack.EMPTY));
                    }
                }
            });
        }

        super.onContentChanged(inventory);
    }


}
