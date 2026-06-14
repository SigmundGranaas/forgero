package com.sigmundgranaas.forgero.smithing.block.entity.custom;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.sigmundgranaas.forgero.minecraft.common.service.StateService;
import com.sigmundgranaas.forgero.smithing.block.entity.ModBlockEntities;
import com.sigmundgranaas.forgero.smithing.item.custom.MorphedItem;
import com.sigmundgranaas.forgero.smithing.minigame.MinigameLogic;
import com.sigmundgranaas.forgero.smithing.minigame.MinigamePositioning;
import com.sigmundgranaas.forgero.smithing.networking.ModMessages;
import com.sigmundgranaas.forgero.smithing.temperature.TemperatureUtils;
import com.sigmundgranaas.forgero.smithing.util.RuntimeModelUtil;
import com.sigmundgranaas.forgero.smithing.util.SchematicMaterialCost;
import com.sigmundgranaas.forgero.smithing.util.SchematicResultUtil;

import lombok.Getter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
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

	private static final int ANVIL_INVENTORY_COOL_TICK_INTERVAL = 10;
	private static final int GUI_COOLDOWN_TICKS = 20;
	private static final int DEFAULT_COOLING_AMOUNT = 4;
	private static final int MAX_MATERIAL_STACK_ON_ANVIL = SchematicMaterialCost.MAX_MATERIAL_COST;

	private static final float ANVIL_TOP_Y = 0.9375f;
	private static final float Y_FIGHTING_OFFSET = 0.001f;
	private static final float MARKER_VISUAL_Y_OFFSET = 0.01f;

	private static final TagKey<Item> INGOTS_TAG = TagKey.of(
			RegistryKeys.ITEM,
			new Identifier("c", "ingots")
	);

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

	private final MinigameLogic minigameLogic = new MinigameLogic();
	private final Random random = new Random();

	public int anvilInventoryCoolAmountPerTick = DEFAULT_COOLING_AMOUNT;
	private int anvilInventoryCoolTickCounter = 0;

	private boolean isSmithing = false;

	@Nullable
	private Identifier plannedProductId = null;

	private long guiBlockCooldownUntil = 0;

	private transient BufferedImage startingItemImage = null;
	private transient BufferedImage plannedProductImage = null;

	private transient boolean showFinalMorphOnce = false;
	private boolean pendingFinalMorphNotify = false;

	public SmithingAnvilBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.SMITHING_ANVIL, pos, state);
	}

	@Override
	public void playHitEffect(Vec2f markerLocalPos) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		float particleY = serverParticleY();
		Vec2f offsetVec = resolveOffsetVec(currentStack());

		net.minecraft.util.math.Vec3d worldPos = MinigamePositioning.itemLocalToWorld(
				markerLocalPos,
				getPos(),
				getCachedState(),
				offsetVec,
				particleY
		);

		serverWorld.spawnParticles(
				ParticleTypes.FLAME,
				worldPos.x,
				worldPos.y,
				worldPos.z,
				4,
				0.001,
				0.001,
				0.001,
				0.05
		);

		serverWorld.spawnParticles(
				ParticleTypes.LAVA,
				worldPos.x,
				worldPos.y,
				worldPos.z,
				2,
				0.01,
				0.01,
				0.01,
				0.02
		);

		serverWorld.playSound(
				null,
				getPos(),
				SoundEvents.BLOCK_ANVIL_PLACE,
				SoundCategory.BLOCKS,
				1f,
				1f
		);
	}

	@Override
	public void playMissEffect() {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		serverWorld.playSound(
				null,
				getPos(),
				SoundEvents.ITEM_AXE_SCRAPE,
				SoundCategory.BLOCKS,
				1f,
				1.0f
		);

		serverWorld.spawnParticles(
				ParticleTypes.SMOKE,
				getPos().getX() + 0.5,
				getPos().getY() + 1.0,
				getPos().getZ() + 0.5,
				10,
				0.3,
				0.1,
				0.3,
				0.05
		);
	}

	@Override
	public void spawnMarkerAppearanceEffect(Vec2f markerLocalPos, ItemStack itemStack) {
		if (!(world instanceof ServerWorld serverWorld)) {
			return;
		}

		float particleY = serverParticleY();
		Vec2f offsetVec = resolveOffsetVec(itemStack);

		MinigamePositioning.itemLocalToWorld(
				markerLocalPos,
				getPos(),
				getCachedState(),
				offsetVec,
				particleY
		);

		serverWorld.playSound(
				null,
				getPos(),
				SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
				SoundCategory.BLOCKS,
				0.5f,
				1.0f
		);
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

	private boolean isServer() {
		return world != null && !world.isClient;
	}

	private ItemStack currentStack() {
		return simpleInventory.getStack(0);
	}

	private Vec2f resolveOffsetVec(ItemStack stack) {
		boolean isMorphedOrPlanned = (isSmithing && plannedProductId != null)
				|| stack.getItem() instanceof MorphedItem;

		return isMorphedOrPlanned
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

	private void informPlayerHeatRequired(PlayerEntity player) {
		player.sendMessage(net.minecraft.text.Text.of("That needs to be heaten up first!"), true);
	}

	public ActionResult onHammerHit(PlayerEntity player, BlockHitResult hitResult) {
		if (!isServer()) {
			return ActionResult.SUCCESS;
		}

		ItemStack anvilItem = currentStack();

		if (anvilItem.isEmpty()) {
			playMissEffect();
			return ActionResult.FAIL;
		}

		if (anvilItem.getItem() instanceof MorphedItem) {
			// if (!isHotEnoughForWork(anvilItem)) {
			//     informPlayerHeatRequired(player);
			//     return ActionResult.FAIL;
			// }
		}

		if (shouldOpenSchematicSelection(anvilItem)) {
			// if (!isHotEnoughForWork(anvilItem)) {
			//     informPlayerHeatRequired(player);
			//     return ActionResult.FAIL;
			// }

			openSchematicSelection(player);
			return ActionResult.FAIL;
		}

		Vec2f offsetVec = resolveOffsetVec(anvilItem);

		Vec2f itemLocalHit = MinigamePositioning.worldHitToItemLocal(
				hitResult,
				getCachedState(),
				offsetVec
		);

		boolean hit = minigameLogic.processHit(itemLocalHit, this);

		if (!hit) {
			playMissEffect();
		}

		minigameLogic.processMarkerAttempt(hit, this);

		if (minigameLogic.isComplete()) {
			minigameLogic.resetMarkerProgress(this);
		}

		return ActionResult.SUCCESS;
	}

	private boolean shouldOpenSchematicSelection(ItemStack stack) {
		return isSmithing
				&& plannedProductId == null
				&& !(stack.getItem() instanceof MorphedItem);
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
			ItemStack itemToReturn = anvilItem.copy();

			if (itemToReturn.getItem() instanceof MorphedItem) {
				minigameLogic.saveProgressToItem(itemToReturn);
			} else {
				cleanPlainMaterialStack(itemToReturn);
			}

			player.getInventory().offerOrDrop(itemToReturn);

			getInventory().setStack(0, ItemStack.EMPTY);

			resetCraftingState();

			markDirty();

			minigameLogic.resetMarkerProgress(this);

			guiBlockCooldownUntil = world.getTime() + GUI_COOLDOWN_TICKS;
		}

		return ActionResult.SUCCESS;
	}

	private void cleanPlainMaterialStack(ItemStack stack) {
		if (stack.isEmpty() || stack.getItem() instanceof MorphedItem) {
			return;
		}

		if (!stack.hasNbt()) {
			return;
		}

		NbtCompound nbt = stack.getNbt();

		if (nbt == null) {
			return;
		}

		nbt.remove("forgero_markerHitsCount");
		nbt.remove("forgero_markerAttempts");
		nbt.remove("forgero_fastMarkerHits");
		nbt.remove("forgero_missMarkerHits");
		nbt.remove("fastMarkerIndices");
		nbt.remove("hitStageIndices");
		nbt.remove("morphProgress");
		nbt.remove(MorphedItem.PROGRESS_KEY);
		nbt.remove(TemperatureUtils.TEMPERATURE_KEY);
		nbt.remove(TemperatureUtils.MAX_TEMPERATURE_KEY);
		nbt.remove(TemperatureUtils.WORKABLE_TEMPERATURE_START_KEY);
		nbt.remove(TemperatureUtils.WORKABLE_TEMPERATURE_END_KEY);

		if (nbt.isEmpty()) {
			stack.setNbt(null);
		}
	}

	public ActionResult tryPlaceItem(PlayerEntity player, Hand hand) {
		if (!isServer()) {
			return ActionResult.SUCCESS;
		}

		ItemStack stackInHand = player.getStackInHand(hand);

		if (stackInHand.isEmpty()) {
			return ActionResult.SUCCESS;
		}

		ItemStack anvilItem = getInventory().getStack(0);

		if (stackInHand.getItem() instanceof MorphedItem) {
			return tryPlaceMorphedItem(stackInHand);
		}

		if (!isValidSmithingMaterial(stackInHand)) {
			return ActionResult.SUCCESS;
		}

		if (anvilItem.isEmpty()) {
			placeFirstMaterialIngot(stackInHand);
			return ActionResult.SUCCESS;
		}

		if (canAddMaterialIngot(anvilItem, stackInHand)) {
			addMaterialIngot(anvilItem, stackInHand);
			return ActionResult.SUCCESS;
		}

		return ActionResult.SUCCESS;
	}

	private ActionResult tryPlaceMorphedItem(ItemStack stackInHand) {
		ItemStack anvilItem = getInventory().getStack(0);

		if (!anvilItem.isEmpty()) {
			return ActionResult.SUCCESS;
		}

		ItemStack toPlace = stackInHand.copy();
		toPlace.setCount(1);

		getInventory().setStack(0, toPlace);

		isSmithing = false;
		plannedProductId = null;

		stackInHand.decrement(1);

		minigameLogic.restoreFromItemNbt(toPlace);
		minigameLogic.getMarkerPositions().clear();
		minigameLogic.getMarkerHits().clear();

		markDirty();

		return ActionResult.SUCCESS;
	}

	private boolean isValidSmithingMaterial(ItemStack stack) {
		return !stack.isEmpty()
				&& stack.isIn(INGOTS_TAG)
				&& TemperatureUtils.hasMaxTemperature(stack);
	}

	private void placeFirstMaterialIngot(ItemStack stackInHand) {
		ItemStack toPlace = stackInHand.copy();
		toPlace.setCount(1);

		getInventory().setStack(0, toPlace);

		isSmithing = true;
		plannedProductId = null;

		stackInHand.decrement(1);

		minigameLogic.resetMarkerProgress(this);

		markDirty();
	}

	private boolean canAddMaterialIngot(ItemStack anvilItem, ItemStack stackInHand) {
		if (plannedProductId != null) {
			return false;
		}

		if (!isSmithing) {
			return false;
		}

		if (anvilItem.getItem() instanceof MorphedItem) {
			return false;
		}

		if (!isValidSmithingMaterial(anvilItem) || !isValidSmithingMaterial(stackInHand)) {
			return false;
		}

		if (!anvilItem.isOf(stackInHand.getItem())) {
			return false;
		}

		return anvilItem.getCount() < MAX_MATERIAL_STACK_ON_ANVIL;
	}

	private void addMaterialIngot(ItemStack anvilItem, ItemStack stackInHand) {
		int existingTemp = TemperatureUtils.getTemperature(anvilItem);
		int addedTemp = TemperatureUtils.getTemperature(stackInHand);
		int combinedTemp = Math.min(existingTemp, addedTemp);

		anvilItem.increment(1);
		TemperatureUtils.setTemperature(anvilItem, combinedTemp);

		stackInHand.decrement(1);

		isSmithing = true;
		plannedProductId = null;

		minigameLogic.resetMarkerProgress(this);

		markDirty();
	}

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
		minigameLogic.writeNbt(nbt);

		ItemStack stack = simpleInventory.getStack(0);

		if (!stack.isEmpty()) {
			writeItemNbtData(stack, minigameLogic);
		}

		nbt.putBoolean("ingotCrafting", isSmithing);

		if (plannedProductId != null) {
			nbt.putString("plannedProductId", plannedProductId.toString());
		}
	}

	private void writeItemNbtData(ItemStack stack, MinigameLogic logic) {
		if (stack.isEmpty()) {
			return;
		}

		if (!(stack.getItem() instanceof MorphedItem)) {
			return;
		}

		NbtCompound itemNbt = stack.getOrCreateNbt();

		double progress = logic.getMorphProgress();

		itemNbt.putInt("forgero_markerHitsCount", logic.getMarkerHitsCount());
		itemNbt.putInt("forgero_markerAttempts", logic.getMarkerAttempts());
		itemNbt.putDouble(MorphedItem.PROGRESS_KEY, progress);
		itemNbt.putDouble("morphProgress", progress);
	}

	@Override
	public void readNbt(@NotNull NbtCompound nbt) {
		super.readNbt(nbt);

		Inventories.readNbt(nbt, this.inventory);
		minigameLogic.readNbt(nbt);

		ItemStack stack = simpleInventory.getStack(0);

		if (!stack.isEmpty() && stack.getItem() instanceof MorphedItem) {
			minigameLogic.restoreFromItemNbt(stack);
		} else {
			minigameLogic.setMarkerHitsCount(0);
			minigameLogic.setMarkerAttempts(0);
		}

		this.isSmithing = nbt.getBoolean("ingotCrafting");
		this.plannedProductId = tryParseIdentifier(nbt.getString("plannedProductId"));
	}

	private @Nullable Identifier tryParseIdentifier(String idString) {
		if (idString == null || idString.isEmpty()) {
			return null;
		}

		try {
			return new Identifier(idString);
		} catch (Exception e) {
			return null;
		}
	}

	private void syncCustomDataToClients() {
		if (world == null || world.isClient) {
			return;
		}

		ItemStack stack = simpleInventory.getStack(0);

		if (!stack.isEmpty()) {
			minigameLogic.saveProgressToItem(stack);
			writeItemNbtData(stack, minigameLogic);
		}

		for (ServerPlayerEntity player : PlayerLookup.tracking((ServerWorld) world, getPos())) {
			PacketByteBuf data = PacketByteBufs.create();

			data.writeBlockPos(getPos());
			data.writeInt(simpleInventory.size());

			for (int i = 0; i < simpleInventory.size(); i++) {
				data.writeItemStack(simpleInventory.getStack(i));
			}

			data.writeInt(minigameLogic.getMarkerPositions().size());

			for (int i = 0; i < minigameLogic.getMarkerPositions().size(); i++) {
				Vec2f markerPos = minigameLogic.getMarkerPositions().get(i);

				data.writeFloat(markerPos.x);
				data.writeFloat(markerPos.y);

				boolean hit = i < minigameLogic.getMarkerHits().size()
						&& minigameLogic.getMarkerHits().get(i);

				data.writeBoolean(hit);
			}

			data.writeInt(minigameLogic.getFastMarkerIndices().size());

			for (int idx : minigameLogic.getFastMarkerIndices()) {
				data.writeInt(idx);
			}

			data.writeInt(minigameLogic.getMarkerAttempts());
			data.writeInt(minigameLogic.getMarkerHitsCount());

			data.writeBoolean(isSmithing);
			data.writeBoolean(plannedProductId != null);

			if (plannedProductId != null) {
				data.writeIdentifier(plannedProductId);
			}

			data.writeBoolean(pendingFinalMorphNotify);

			ServerPlayNetworking.send(player, ModMessages.ITEM_SYNC, data);
		}

		pendingFinalMorphNotify = false;
	}

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

		updateGuiCooldown();
		updateTemperatureCooling();

		minigameLogic.tick(this);
	}

	private void updateGuiCooldown() {
		if (guiBlockCooldownUntil > 0 && world.getTime() >= guiBlockCooldownUntil) {
			guiBlockCooldownUntil = 0;
		}
	}

	private void updateTemperatureCooling() {
		anvilInventoryCoolTickCounter++;

		if (anvilInventoryCoolTickCounter < ANVIL_INVENTORY_COOL_TICK_INTERVAL) {
			return;
		}

		anvilInventoryCoolTickCounter = 0;

		ItemStack stack = currentStack();

		if (!shouldCoolStack(stack)) {
			return;
		}

		boolean temperatureChanged = coolStack(stack);

		if (temperatureChanged) {
			markDirty();
		}
	}

	private boolean shouldCoolStack(ItemStack stack) {
		if (stack.isEmpty() || !TemperatureUtils.hasMaxTemperature(stack)) {
			return false;
		}

		boolean schematicSelected = (isSmithing && plannedProductId != null)
				|| stack.getItem() instanceof MorphedItem;

		return schematicSelected;
	}

	private boolean coolStack(ItemStack stack) {
		int temp = TemperatureUtils.getTemperature(stack);

		if (temp <= 20) {
			return false;
		}

		int newTemp = Math.max(20, temp - anvilInventoryCoolAmountPerTick);

		if (newTemp == temp) {
			return false;
		}

		TemperatureUtils.setTemperature(stack, newTemp);

		return true;
	}

	public void saveProgressToItem() {
		ItemStack stack = simpleInventory.getStack(0);
		minigameLogic.saveProgressToItem(stack);
	}

	public SimpleInventory getInventory() {
		return simpleInventory;
	}

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
		return minigameLogic.getMorphProgress(getInventory().getStack(0));
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

	public void openSchematicSelection(PlayerEntity player) {
		if (world == null || world.isClient) {
			return;
		}

		ItemStack materialStack = currentStack();

		if (materialStack.isEmpty() || materialStack.getItem() instanceof MorphedItem) {
			return;
		}

		int materialCount = materialStack.getCount();

		Map<Identifier, Integer> costs = new LinkedHashMap<>();

		List<Identifier> options = SchematicResultUtil.findAvailableSchematicProductsForPlayer(player)
				.stream()
				.filter(id -> !createProductFromPlanned(id).isEmpty())
				.peek(id -> costs.put(id, SchematicMaterialCost.getCost(id)))
				.collect(Collectors.toList());

		SchematicResultUtil.openSchematicSelection(
				player,
				getPos(),
				options,
				costs,
				materialCount,
				world
		);
	}

	public void setPlannedProduct(Identifier productId) {
		setPlannedProduct(null, productId);
	}

	public void setPlannedProduct(@Nullable PlayerEntity player, Identifier productId) {
		this.plannedProductId = productId;

		boolean converted = replaceIngotWithMorphed(player);

		if (!converted) {
			this.plannedProductId = null;
			markDirty();
			return;
		}

		minigameLogic.resetMarkerProgress(this);

		if (world != null && world.isClient) {
			ItemStack plannedStack = createProductFromPlanned(productId);

			plannedProductImage = RuntimeModelUtil.getFirstQuadTextureImage(
					plannedStack,
					MinecraftClient.getInstance()
			);
		}

		markDirty();
	}

	public void clientRefreshMorphImages() {
		if (world == null || !world.isClient) {
			return;
		}

		ItemStack stack = getInventory().getStack(0);

		this.startingItemImage = RuntimeModelUtil.getFirstQuadTextureImage(
				stack,
				MinecraftClient.getInstance()
		);

		if (plannedProductId != null) {
			ItemStack plannedStack = createProductFromPlanned(plannedProductId);

			this.plannedProductImage = RuntimeModelUtil.getFirstQuadTextureImage(
					plannedStack,
					MinecraftClient.getInstance()
			);
		} else {
			this.plannedProductImage = null;
		}
	}

	public ItemStack createProductFromPlanned(Identifier productId) {
		ItemStack result = tryCreateFromStateService(productId);

		if (!result.isEmpty()) {
			return result;
		}

		String material = detectMaterialForStack(getInventory().getStack(0));
		List<Identifier> candidates = buildCandidateIds(productId, material);

		result = tryResolveFromCandidates(candidates);

		return result.isEmpty() ? ItemStack.EMPTY : result;
	}

	private ItemStack tryCreateFromStateService(Identifier productId) {
		try {
			var maybeState = StateService.INSTANCE.find(productId.toString());

			if (maybeState.isPresent()) {
				var stackOpt = StateService.INSTANCE.convert(maybeState.get());

				if (stackOpt.isPresent()) {
					return stackOpt.get();
				}
			}
		} catch (Throwable ignored) {
		}

		return ItemStack.EMPTY;
	}

	private List<Identifier> buildCandidateIds(Identifier productId, String material) {
		List<Identifier> candidates = new ArrayList<>();

		if (material != null && !material.isEmpty()) {
			candidates.add(new Identifier(
					productId.getNamespace(),
					material + "_" + productId.getPath()
			));

			candidates.add(new Identifier(
					productId.getNamespace(),
					material + "-" + productId.getPath()
			));
		}

		candidates.add(productId);

		return candidates;
	}

	private ItemStack tryResolveFromCandidates(List<Identifier> candidates) {
		for (Identifier id : candidates) {
			ItemStack result = tryResolveFromStateService(id);

			if (!result.isEmpty()) {
				return result;
			}
		}

		for (Identifier id : candidates) {
			ItemStack result = tryResolveFromRegistry(id);

			if (!result.isEmpty()) {
				return result;
			}
		}

		return ItemStack.EMPTY;
	}

	private ItemStack tryResolveFromStateService(Identifier id) {
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

		return ItemStack.EMPTY;
	}

	private ItemStack tryResolveFromRegistry(Identifier id) {
		try {
			var itemOpt = Registries.ITEM.getOrEmpty(id);

			if (itemOpt.isPresent()) {
				return new ItemStack(itemOpt.get());
			}
		} catch (Throwable ignored) {
		}

		return ItemStack.EMPTY;
	}

	private String detectMaterialForStack(ItemStack stack) {
		if (stack.isEmpty()) {
			return "";
		}

		Identifier id = Registries.ITEM.getId(stack.getItem());
		String path = id.getPath();

		if (TemperatureUtils.hasMaxTemperature(stack)) {
			if (path.endsWith("_ingot")) {
				return path.substring(0, path.length() - "_ingot".length());
			}

			return path;
		}

		return path;
	}

	public void clientSyncIngotState(
			boolean ingotCrafting,
			@Nullable Identifier plannedProductId
	) {
		this.isSmithing = ingotCrafting;
		this.plannedProductId = plannedProductId;
	}

	public void clientTriggerFinalMorphOnce() {
		this.showFinalMorphOnce = true;
	}

	private boolean replaceIngotWithMorphed(@Nullable PlayerEntity player) {
		if (world == null || world.isClient || plannedProductId == null) {
			return false;
		}

		ItemStack current = getInventory().getStack(0);

		if (current.isEmpty()) {
			return false;
		}

		int requiredCost = SchematicMaterialCost.getCost(plannedProductId);

		if (current.getCount() < requiredCost) {
			return false;
		}

		int currentTemp = TemperatureUtils.getTemperature(current);
		int maxTemp = TemperatureUtils.getMaxTemp(current);
		int workableStart = TemperatureUtils.getWorkableTemperatureStart(current);
		int workableEnd = TemperatureUtils.getWorkableTemperatureEnd(current);

		Item morphedItem = findMorphedItem();

		if (morphedItem == null) {
			return false;
		}

		ItemStack resultStack = createProductFromPlanned(plannedProductId);

		if (resultStack.isEmpty()) {
			return false;
		}

		ItemStack morphed = new ItemStack(morphedItem, 1);

		MorphedItem.setResultItem(morphed, resultStack.getItem());
		MorphedItem.setStartItem(morphed, current.getItem());
		MorphedItem.setMorphProgress(morphed, 0.0);

		morphed.getOrCreateNbt().putInt(
				SchematicMaterialCost.MATERIAL_COST_KEY,
				requiredCost
		);

		TemperatureUtils.setTemperature(morphed, currentTemp);
		TemperatureUtils.setMaxTemperature(morphed, maxTemp);
		TemperatureUtils.setWorkableTemperatureStart(morphed, workableStart);
		TemperatureUtils.setWorkableTemperatureEnd(morphed, workableEnd);

		int verifyTemp = TemperatureUtils.getTemperature(morphed);

		if (verifyTemp != currentTemp) {
			morphed.getOrCreateNbt().putInt(
					TemperatureUtils.TEMPERATURE_KEY,
					currentTemp
			);
		}

		int leftoverCount = current.getCount() - requiredCost;

		if (leftoverCount > 0) {
			ItemStack leftover = current.copy();
			leftover.setCount(leftoverCount);

			if (player != null) {
				player.getInventory().offerOrDrop(leftover);
			} else {
				Block.dropStack(world, getPos().up(), leftover);
			}
		}

		getInventory().setStack(0, morphed);

		return true;
	}

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
