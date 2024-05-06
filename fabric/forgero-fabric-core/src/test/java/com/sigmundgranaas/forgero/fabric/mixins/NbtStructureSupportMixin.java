package com.sigmundgranaas.forgero.fabric.mixins;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import com.sigmundgranaas.forgero.core.Forgero;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.Block;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.FixedBufferInputStream;
import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelStorage;


/**
 * A very temporary mixin to make it possible to load NBT structure files.
 * <p>
 * I don't know how to save structures as SNBT. Maybe this should be added to the gametest Fabric api...
 */
@Mixin(StructureTemplateManager.class)
public abstract class NbtStructureSupportMixin {
	@Shadow
	private ResourceManager resourceManager;

	@Shadow
	public abstract StructureTemplate createTemplate(NbtCompound nbt);

	@Unique
	private static final String GAMETEST_STRUCTURE_PATH = "gametest/structures";
	@Unique
	private static final ResourceFinder GAMETEST_STRUCTURE_FINDER = new ResourceFinder(GAMETEST_STRUCTURE_PATH, ".nbt");

	@Unique
	private Optional<StructureTemplate> forgero_loadnbtFromResource(Identifier id) {
		Identifier path = GAMETEST_STRUCTURE_FINDER.toResourcePath(id);
		Optional<Resource> resource = this.resourceManager.getResource(path);

		if (resource.isPresent()) {
			try {
				NbtCompound nbtCompound = NbtIo.readCompound(new DataInputStream(new FixedBufferInputStream(new GZIPInputStream(resource.get().getInputStream()))));
				StructureTemplate struc = this.createTemplate(nbtCompound);
				return Optional.of(struc);
			} catch (IOException e) {
				Forgero.LOGGER.error("Failed to load GameTest structure " + id, e);
			}
		}

		return Optional.empty();
	}

	@Unique
	private Stream<Identifier> forgero_streamNbtTemplatesFromResource() {
		ResourceFinder finder = GAMETEST_STRUCTURE_FINDER;
		return finder.findResources(this.resourceManager).keySet().stream().map(finder::toResourceId);
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList$Builder;add(Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList$Builder;", ordinal = 2, shift = At.Shift.AFTER, remap = false), locals = LocalCapture.CAPTURE_FAILHARD)
	private void forgero$addFabricTemplateProvider(ResourceManager resourceManager, LevelStorage.Session session, DataFixer dataFixer, RegistryEntryLookup<Block> blockLookup, CallbackInfo ci, ImmutableList.Builder<StructureTemplateManager.Provider> builder) {
		builder.add(new StructureTemplateManager.Provider(this::forgero_loadnbtFromResource, this::forgero_streamNbtTemplatesFromResource));
	}
}
