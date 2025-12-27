package com.sigmundgranaas.forgero.fabric.tags;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.drp.api.DRPApi;
import com.sigmundgranaas.forgero.drp.api.DynamicResourcePack;
import com.sigmundgranaas.forgero.drp.api.lifecycle.ResourcePackPhase;
import com.sigmundgranaas.forgero.drp.api.tag.TagBuilder;
import com.sigmundgranaas.forgero.fabric.ForgeroCompatInitializer;

import net.minecraft.util.Identifier;

public abstract class CommonTagGenerator {
	private final String mod;
	private final String namespace;
	private DynamicResourcePack resourcePack;

	protected CommonTagGenerator(String mod, String namespace) {
		this.mod = mod;
		this.namespace = namespace;
	}

	protected CommonTagGenerator(String mod) {
		this.mod = mod;
		this.namespace = mod;
	}

	public abstract void addTags();

	public void register() {
		this.resourcePack = DRPApi.getInstance()
				.createPack("%s:%s_common_tags".formatted(Forgero.NAMESPACE, mod))
				.description("Forgero common tags for " + mod)
				.build();
		addTags();
		DRPApi.getInstance().register(resourcePack, ResourcePackPhase.BEFORE_VANILLA);
	}

	public boolean isModLoaded() {
		return ForgeroCompatInitializer.isModLoaded(mod);
	}

	protected void registerCommonItemTag(String item) {
		var tagBuilder = TagBuilder.items("c:items/" + item)
				.add(new Identifier(namespace, item).toString());
		resourcePack.addTag(tagBuilder);
	}
}
