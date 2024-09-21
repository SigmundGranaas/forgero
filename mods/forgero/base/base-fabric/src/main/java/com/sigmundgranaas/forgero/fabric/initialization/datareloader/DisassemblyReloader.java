package com.sigmundgranaas.forgero.fabric.initialization.datareloader;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.resources.DisassemblyRecipeLoader;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class DisassemblyReloader implements SimpleSynchronousResourceReloadListener {
	@Override
	public void reload(ResourceManager manager) {
		DisassemblyRecipeLoader.reload(manager);
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier(Forgero.NAMESPACE, "disassembly");
	}
}
