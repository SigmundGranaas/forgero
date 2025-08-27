package com.sigmundgranaas.forgero.smithing.block.entity.custom;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameLogic;
import com.sigmundgranaas.forgero.smithing.minigame.MinigamePositioning;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.RuntimeModelUtil;
import com.sigmundgranaas.forgero.smithing.util.SchematicResultUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

@Getter
public class SmithingAnvilBlockEntity extends BlockEntity implements MinigameLogic.MinigameCallback {
	private static final @NotNull String INVENTORY_NBT_KEY = "inventory";

	private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
	private final SimpleInventory simpleInventory = new SimpleInventory(inventory.size()) {
		@Override
		public ItemStack getStack(int slot) {
			return inventory.get(slot);
		}

		@Override
		public void setStack(int slot, ItemStack stack) {
			inventory.set(slot, stack);
			super.setStack(slot, stack);
		}
	};

	// Minigame logic instance
	private final MinigameLogic minigameLogic = new MinigameLogic();

	private final Random random = new Random();

	private static final int ANVIL_INVENTORY_COOL_TICK_INTERVAL = 4;
	public int anvilInventoryCoolAmountPerTick = 4; // Changeable cooling amount per tick
	private int anvilInventoryCoolTickCounter = 0;

	private static final float ANVIL_TOP_Y = 0.9375f;
	private static final float Y_FIGHTING_OFFSET = 0.001f;
	private static final float MARKER_VISUAL_Y_OFFSET = 0.01f;

	// Ingot-crafting mode
	private boolean isSmithing = false;
	@Nullable
	private Identifier plannedProductId = null;

	// Tag for any mod-provided ingots (c:ingots). Fallback heuristics are used if tags are missing.
	private static final TagKey<Item> INGOTS_TAG = TagKey.of(RegistryKeys.ITEM, new Identifier("c", "ingots"));

	private long guiBlockCooldownUntil = 0;

	// Cached images for morphing/minigame
	private transient BufferedImage startingItemImage = null;
	private transient BufferedImage plannedProductImage = null;

	private transient boolean showFinalMorphOnce = false;
	private boolean pendingFinalMorphNotify = false;

	public SmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SMITHING_ANVIL, pos, state);
	}

	// =================================
	// MinigameCallback Implementation
	// =================================

	@Override
	public void playHitEffect(Vec2f markerLocalPos) {
		if (world instanceof ServerWorld serverWorld) {
			float particleY = serverParticleY();
			Vec2f offsetVec = resolveOffsetVec(currentStack());
			net.minecraft.util.math.Vec3d worldParticlePos = MinigamePositioning.itemLocalToWorld(
					markerLocalPos, getPos(), getCachedState(), offsetVec, particleY
			);
			serverWorld.spawnParticles(ParticleTypes.FLAME, worldParticlePos.x, worldParticlePos.y, worldParticlePos.z, 4, 0.001, 0.001, 0.001, 0.05);
			serverWorld.spawnParticles(ParticleTypes.LAVA, worldParticlePos.x, worldParticlePos.y, worldParticlePos.z, 2, 0.01, 0.01, 0.01, 0.02);
			serverWorld.playSound(null, getPos(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 1f, 1f);
		}
	}

	@Override
	public void playMissEffect() {
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.playSound(null, getPos(), SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1f, 1.0f);
			serverWorld.spawnParticles(ParticleTypes.SMOKE, getPos().getX() + 0.5, getPos().getY() + 1.0, getPos().getZ() + 0.5, 10, 0.3, 0.1, 0.3, 0.05);
		}
	}

	@Override
	public void spawnMarkerAppearanceEffect(Vec2f markerLocalPos, ItemStack itemStack) {
		if (world instanceof ServerWorld serverWorld) {
			float particleY = serverParticleY();
			Vec2f offsetVec = resolveOffsetVec(itemStack);
			net.minecraft.util.math.Vec3d worldParticlePos = MinigamePositioning.itemLocalToWorld(
					markerLocalPos, getPos(), getCachedState(), offsetVec, particleY
			);
			serverWorld.playSound(null, getPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 0.5f, 1.0f);
		}
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public ItemStack getCurrentStack() {
		return currentStack();
	}

	@Override
	public void replaceWithResult(ItemStack resultStack) {
		getInventory().setStack(0, resultStack);
	}

	// =================================
	// Utilities
	// =================================

	private boolean isServer() {
		return world != null && !world.isClient;
	}

	private ItemStack currentStack() {
		return simpleInventory.getStack(0);
	}

	private Vec2f resolveOffsetVec(ItemStack stack) {
		boolean morphedOrPlanned = (isSmithing && plannedProductId != null) || stack.getItem() instanceof MorphedItem;
		return morphedOrPlanned
				? MinigamePositioning.getMorphedTextureOffsetVec2f(this)
				: MinigamePositioning.getItemTextureOffsetVec2f(stack);
	}

	private float serverParticleY() {
		return ANVIL_TOP_Y + Y_FIGHTING_OFFSET + MARKER_VISUAL_Y_OFFSET;
	}

	private void resetCraftingState() {
		isSmithing = false;
		plannedProductId = null;
	}

	// =================================
	// Main Interaction Logic
	// =================================

	public ActionResult onHammerHit(PlayerEntity player, BlockHitResult hitResult) {
		if (!isServer()) {
			return ActionResult.SUCCESS;
		}
		ItemStack anvilItem = currentStack();
		if (anvilItem.isEmpty()) {
			playMissEffect();
			return ActionResult.FAIL;
		}

		// Temperature gating for morphed items as well
		if (anvilItem.getItem() instanceof MorphedItem) {
			int temperature = TemperatureUtils.getTemperature(anvilItem);
			int maxTemp = TemperatureUtils.getMaxTemp(anvilItem);
			if (!com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isHotEnoughForWork(temperature, maxTemp)) {
				player.sendMessage(net.minecraft.text.Text.of("That needs to be heaten up first!"), true);
				return ActionResult.FAIL;
			}
		}

		if (isSmithing && plannedProductId == null && !(anvilItem.getItem() instanceof MorphedItem)) {
			int temperature = TemperatureUtils.getTemperature(anvilItem);
			int maxTemp = TemperatureUtils.getMaxTemp(anvilItem);
			if (!com.sigmundgranaas.forgero.smithing.temperature.TemperatureColorProvider.isHotEnoughForWork(temperature, maxTemp)) {
				player.sendMessage(net.minecraft.text.Text.of("That needs to be heaten up first!"), true);
				return ActionResult.FAIL;
			}
			openSchematicSelection(player);
			return ActionResult.FAIL;
		}

		Vec2f offsetVec = resolveOffsetVec(anvilItem);
		Vec2f itemLocalHit = MinigamePositioning.worldHitToItemLocal(hitResult, getCachedState(), offsetVec);

		boolean hit = minigameLogic.processHit(itemLocalHit, this);

		if (!hit) {
			playMissEffect();
		}
		minigameLogic.processMarkerAttempt(hit, this);

		if (minigameLogic.isComplete()) {
			if (anvilItem.getItem() instanceof MorphedItem) {
				minigameLogic.setMorphProgress(1.0, this, plannedProductId);
				minigameLogic.resetMarkerProgress(this);
			} else {
				minigameLogic.resetMarkerProgress(this);
			}
		}
		return ActionResult.SUCCESS;
	}

	public boolean isGuiBlocked(World world) {
		return world != null && world.getTime() < guiBlockCooldownUntil;
	}

	public ActionResult tryPickupItem(PlayerEntity player) {
		if (!isServer()) {
			return ActionResult.SUCCESS;
		}
		ItemStack anvilItem = currentStack();
		if (!anvilItem.isEmpty()) {
			minigameLogic.saveProgressToItem(anvilItem);
			player.getInventory().offerOrDrop(anvilItem.copy());
			getInventory().setStack(0, ItemStack.EMPTY);
			resetCraftingState();
			markDirty();
			minigameLogic.resetMarkerProgress(this);
			guiBlockCooldownUntil = world.getTime() + 20; // 1s
		}
		return ActionResult.SUCCESS;
	}

	public ActionResult tryPlaceItem(PlayerEntity player, Hand hand) {
		if (!isServer()) {
			return ActionResult.SUCCESS;
		}
		ItemStack stackInHand = player.getStackInHand(hand);
		ItemStack anvilItem = getInventory().getStack(0);

		if (anvilItem.isEmpty()) {
			// Accept any item with hasMaxTemperature OR MorphedItem
			if (TemperatureUtils.hasMaxTemperature(stackInHand)){
				ItemStack toPlace = stackInHand.copy();
				toPlace.setCount(1);
				getInventory().setStack(0, toPlace);

				isSmithing = !(stackInHand.getItem() instanceof MorphedItem);

				plannedProductId = null;
				stackInHand.decrement(1);

				if (stackInHand.getItem() instanceof MorphedItem) {
					// Restore progress from NBT for MorphedItem
					minigameLogic.restoreFromItemNbt(toPlace);
					// Do NOT reset marker progress, just clear active marker
					minigameLogic.getMarkerPositions().clear();
					minigameLogic.getMarkerHits().clear();
				} else {
					// Reset progress for new items
					minigameLogic.resetMarkerProgress(this);
				}
				markDirty();
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.SUCCESS;
	}

	// =================================
	// NBT and Syncing
	// =================================

	@Override
	public void markDirty() {
		super.markDirty();
		if (isServer()) {
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
			syncCustomDataToClients();
		}
	}

	@Override
	public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Override
	public void writeNbt(@NotNull NbtCompound nbt) {
		super.writeNbt(nbt);
		Inventories.writeNbt(nbt, this.inventory);

		// Write minigame data
		minigameLogic.writeNbt(nbt);

		ItemStack stack = simpleInventory.getStack(0);
		if (!stack.isEmpty()) {
			NbtCompound itemNbt = stack.getOrCreateNbt();
			itemNbt.putInt("forgero_markerHitsCount", minigameLogic.getMarkerHitsCount());
			itemNbt.putInt("forgero_markerAttempts", minigameLogic.getMarkerAttempts());
			itemNbt.putIntArray("hitTemperatures", minigameLogic.getHitTemperatures().stream().mapToInt(Integer::intValue).toArray());
			itemNbt.putInt("coldStageHits", minigameLogic.getColdStageHits());
			itemNbt.putInt("warmStageHits", minigameLogic.getWarmStageHits());
			itemNbt.putInt("hotStageHits", minigameLogic.getHotStageHits());
			itemNbt.putInt("veryHotStageHits", minigameLogic.getVeryHotStageHits());
			itemNbt.putInt("nearMeltStageHits", minigameLogic.getNearMeltStageHits());
			itemNbt.putInt("moltenStageHits", minigameLogic.getMoltenStageHits());
		}

		// Ingot crafting state
		nbt.putBoolean("ingotCrafting", isSmithing);
		if (plannedProductId != null) {
			nbt.putString("plannedProductId", plannedProductId.toString());
		}
	}

	@Override
	public void readNbt(@NotNull NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, this.inventory);

		// Read minigame data
		minigameLogic.readNbt(nbt);

		ItemStack stack = simpleInventory.getStack(0);
		if (!stack.isEmpty()) {
			minigameLogic.restoreFromItemNbt(stack);
		} else {
			minigameLogic.setMarkerHitsCount(0);
			minigameLogic.setMarkerAttempts(0);
		}

		// Ingot crafting state
		this.isSmithing = nbt.getBoolean("ingotCrafting");
		if (nbt.contains("plannedProductId")) {
			try {
				this.plannedProductId = new Identifier(nbt.getString("plannedProductId"));
			} catch (Exception e) {
				this.plannedProductId = null;
			}
		} else {
			this.plannedProductId = null;
		}
	}

	/**
	 * Sends custom sync data to all tracking clients.
	 * This is necessary for data that isn't handled by BlockEntityUpdateS2CPacket's default NBT sync.
	 */
	private void syncCustomDataToClients() {
		if (world == null || world.isClient) return;

		PacketByteBuf data = PacketByteBufs.create();
		data.writeBlockPos(getPos());

		// Inventory
		data.writeInt(simpleInventory.size());
		for (int i = 0; i < simpleInventory.size(); i++) {
			data.writeItemStack(simpleInventory.getStack(i));
		}

		// Marker positions and hits
		data.writeInt(minigameLogic.getMarkerPositions().size());
		for (int i = 0; i < minigameLogic.getMarkerPositions().size(); i++) {
			Vec2f pos = minigameLogic.getMarkerPositions().get(i);
			data.writeFloat(pos.x);
			data.writeFloat(pos.y);
			boolean hit = minigameLogic.getMarkerHits().size() > i && minigameLogic.getMarkerHits().get(i);
			data.writeBoolean(hit);
		}

		// Fast marker indices
		data.writeInt(minigameLogic.getFastMarkerIndices().size());
		for (int idx : minigameLogic.getFastMarkerIndices()) {
			data.writeInt(idx);
		}

		// Progress counters
		data.writeInt(minigameLogic.getMarkerAttempts());
		data.writeInt(minigameLogic.getMarkerHitsCount());

		// Ingot crafting state
		data.writeBoolean(isSmithing);
		data.writeBoolean(plannedProductId != null);
		if (plannedProductId != null) {
			data.writeIdentifier(plannedProductId);
		}

		// One-shot final morph overlay notification
		data.writeBoolean(pendingFinalMorphNotify);

		for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
			ServerPlayNetworking.send(player, ModMessages.ITEM_SYNC, data);
		}
		// Reset the pending flag after sending to all players
		pendingFinalMorphNotify = false;
	}

	// =================================
	// Delegated Methods to MinigameLogic
	// =================================

	public void resetMarkerProgress() {
		minigameLogic.resetMarkerProgress(this);
	}

	public void clearMarkerProgress() {
		minigameLogic.clearMarkerProgress();
	}

	public void resetMarkers() {
		minigameLogic.resetMarkerProgress(this);
	}

	public void tick() {
		if (!isServer()) {
			return;
		}

		// GUI cooldown
		if (guiBlockCooldownUntil > 0 && world.getTime() >= guiBlockCooldownUntil) {
			guiBlockCooldownUntil = 0;
		}

		// Periodic dirty to ensure cooling/etc. visuals
		anvilInventoryCoolTickCounter++;
		if (anvilInventoryCoolTickCounter >= ANVIL_INVENTORY_COOL_TICK_INTERVAL) {
			anvilInventoryCoolTickCounter = 0;
			ItemStack stack = currentStack();
			if (!stack.isEmpty() && (stack.getItem() instanceof com.sigmundgranaas.forgero.minecraft.common.item.StateItem || stack.getItem() instanceof MorphedItem)) {
				if (TemperatureUtils.hasMaxTemperature(stack)) {
					int temp = TemperatureUtils.getTemperature(stack);
					int maxTemp = TemperatureUtils.getMaxTemp(stack);
					if (temp > 20) {
						int newTemp = Math.max(20, temp - anvilInventoryCoolAmountPerTick); // Use configurable cooling amount
						TemperatureUtils.setTemperature(stack, newTemp);
					}
					// --- Fire particle logic for veryHot stage ---
					if (stack.getItem() instanceof MorphedItem && MorphedItem.getMorphProgress(stack) < 1.0) {
						if (TemperatureColorProvider.isInVeryHot(temp, maxTemp)) { // Use proper stage detection
							if (world instanceof ServerWorld serverWorld) {
								// Spawn a few critical hit particles with wider spread around the item
								for (int i = 0; i < 2; i++) {
									double xOffset = 0.4 * (random.nextDouble() - 0.5); // wider spread
									double zOffset = 0.4 * (random.nextDouble() - 0.5);
									double yOffset = 0.1 + 0.1 * random.nextDouble();
									double x = getPos().getX() + 0.5 + xOffset;
									double y = getPos().getY() + 0.95 + yOffset;
									double z = getPos().getZ() + 0.5 + zOffset;
									serverWorld.spawnParticles(ParticleTypes.CRIT, x, y, z, 1, 0, 0, 0, 0.01);
								}
							}
						}
					}
				}
			}
			markDirty();
		}

		// Delegate minigame tick to MinigameLogic
		minigameLogic.tick(this);
	}

	// =================================
	// Data Persistence on Item
	// =================================

	public void saveProgressToItem() {
		ItemStack stack = simpleInventory.getStack(0);
		minigameLogic.saveProgressToItem(stack);
	}

	public SimpleInventory getInventory() {
		return simpleInventory;
	}

	// =================================
	// Getter methods for compatibility
	// =================================

	public List<Vec2f> getMarkerPositions() {
		return minigameLogic.getMarkerPositions();
	}

	public List<Boolean> getMarkerHits() {
		return minigameLogic.getMarkerHits();
	}

	public int getMarkerAttempts() {
		return minigameLogic.getMarkerAttempts();
	}

	public int getMarkerHitsCount() {
		return minigameLogic.getMarkerHitsCount();
	}

	public void setMarkerAttempts(int attempts) {
		minigameLogic.setMarkerAttempts(attempts);
	}

	public void setMarkerHitsCount(int count) {
		minigameLogic.setMarkerHitsCount(count);
	}

	public double getMorphProgress() {
		return minigameLogic.getMorphProgress();
	}

	public void setMorphProgress(double progress) {
		minigameLogic.setMorphProgress(progress, this, plannedProductId);
	}

	public List<Integer> getFastMarkerIndices() {
		return minigameLogic.getFastMarkerIndices();
	}

	public MinigameLogic getMinigameLogic() {
		return minigameLogic;
	}

	// ---------------------------------
	// Schematic selection helpers / API
	// ---------------------------------

	public void openSchematicSelection(PlayerEntity player) {
		if (world == null || world.isClient) return;
		List<Identifier> options = SchematicResultUtil.findAvailableSchematicProductsForPlayer(player)
				.stream()
				.filter(id -> !createProductFromPlanned(id).isEmpty())
				.collect(Collectors.toList());
		SchematicResultUtil.openSchematicSelection(player, getPos(), options, world);
	}

	public void setPlannedProduct(Identifier productId) {
		this.plannedProductId = productId;
		markDirty();
		// Replace the ingot with the MorphedItem now that a result has been chosen
		replaceIngotWithMorphed();
		// Reset the mini-game so it starts fresh after selection
		minigameLogic.resetMarkerProgress(this);
		// --- Fetch planned product image ---
		if (world != null && world.isClient) {
			ItemStack plannedStack = createProductFromPlanned(productId);
			plannedProductImage = RuntimeModelUtil.getFirstQuadTextureImage(plannedStack, MinecraftClient.getInstance());
		}
	}

	// Client-only: refresh morph images based on current inventory and planned product
	public void clientRefreshMorphImages() {
		if (world == null || !world.isClient) return;
		ItemStack stack = getInventory().getStack(0);
		this.startingItemImage = RuntimeModelUtil.getFirstQuadTextureImage(stack, MinecraftClient.getInstance());
		if (plannedProductId != null) {
			ItemStack plannedStack = createProductFromPlanned(plannedProductId);
			this.plannedProductImage = RuntimeModelUtil.getFirstQuadTextureImage(plannedStack, MinecraftClient.getInstance());
		} else {
			this.plannedProductImage = null;
		}
	}

	// Make product creation accessible to positioning util
	public ItemStack createProductFromPlanned(Identifier productId) {
		// Prefer resolving via StateService if your tool heads are states
		try {
			var maybeState = StateService.INSTANCE.find(productId.toString());
			if (maybeState.isPresent()) {
				var state = maybeState.get();
				var stackOpt = StateService.INSTANCE.convert(state);
				if (stackOpt.isPresent()) {
					return stackOpt.get();
				}
			}
		} catch (Throwable ignored) {
			// Fall through to try more specific materialized IDs
		}

		// Materialize the product for the current workpiece material (e.g., iron, copper, gold, etc.)
		String material = detectMaterialForStack(getInventory().getStack(0));
		List<Identifier> candidates = new ArrayList<>();
		if (material != null && !material.isEmpty()) {
			candidates.add(new Identifier(productId.getNamespace(), material + "_" + productId.getPath()));
			candidates.add(new Identifier(productId.getNamespace(), material + "-" + productId.getPath()));
		}
		candidates.add(productId);

		// Try states first
		for (Identifier id : candidates) {
			try {
				var maybeState = StateService.INSTANCE.find(id.toString());
				if (maybeState.isPresent()) {
					var stackOpt = StateService.INSTANCE.convert(maybeState.get());
					if (stackOpt.isPresent() && !stackOpt.get().isEmpty()) {
						return stackOpt.get();
					}
				}
			} catch (Throwable ignored) {
			}
		}

		// Fallback: registry items
		for (Identifier id : candidates) {
			try {
				var itemOpt = Registries.ITEM.getOrEmpty(id);
				if (itemOpt.isPresent()) {
					return new ItemStack(itemOpt.get());
				}
			} catch (Throwable ignored) {
			}
		}
		return ItemStack.EMPTY;
	}

	private String detectMaterialForStack(ItemStack stack) {
		if (stack.isEmpty()) return "";
		Identifier id = Registries.ITEM.getId(stack.getItem());
		String path = id.getPath();
		if (com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils.hasMaxTemperature(stack)) {
			if (path.endsWith("_ingot")) {
				return path.substring(0, path.length() - "_ingot".length());
			}
			return path;
		} else {
			// For non-ingots, use the full item name
			return path;
		}
	}

	// Client-only setter used by S2C sync to reflect ingot crafting state without resetting markers/minigame.
	public void clientSyncIngotState(boolean ingotCrafting, @Nullable Identifier plannedProductId) {
		this.isSmithing = ingotCrafting;
		this.plannedProductId = plannedProductId;
	}

	// Client-only: trigger a one-frame overlay of the fully-morphed texture
	public void clientTriggerFinalMorphOnce() {
		this.showFinalMorphOnce = true;
	}

	private void replaceIngotWithMorphed() {
		if (world == null || world.isClient) return;
		if (plannedProductId == null) return;

		ItemStack current = getInventory().getStack(0);
		if (current.isEmpty()) return;

		Item morphedItem = findMorphedItem();
		if (morphedItem == null) {
			return;
		}

		ItemStack morphed = new ItemStack(morphedItem, 1);

		// NBT no longer needed - predicate system handles nether detection automatically

		// Initialize morph NBT (start -> ingot id, result -> selected product item id)
		ItemStack resultStack = createProductFromPlanned(plannedProductId);
		if (!resultStack.isEmpty()) {
			MorphedItem.setResultItem(morphed, resultStack.getItem());
		} else {
			// Fallback: store the planned id string if we couldn't materialize an item stack
			morphed.getOrCreateNbt().putString(MorphedItem.RESULT_KEY, plannedProductId.toString());
		}
		MorphedItem.setStartItem(morphed, current.getItem());
		MorphedItem.setMorphProgress(morphed, 0.0);

		TemperatureUtils.setMaxTemperature(morphed, TemperatureUtils.getMaxTemp(current));
		TemperatureUtils.setTemperature(morphed, TemperatureUtils.getTemperature(current));

		getInventory().setStack(0, morphed);
		// Keep ingotCrafting true so minigame continues to work with morphed item
		markDirty();
	}

	// Scan registry to find the MorphedItem instance registered by the mod
	@Nullable
	private Item findMorphedItem() {
		for (Item item : Registries.ITEM) {
			if (item instanceof MorphedItem) {
				return item;
			}
		}
		return null;
	}
}
