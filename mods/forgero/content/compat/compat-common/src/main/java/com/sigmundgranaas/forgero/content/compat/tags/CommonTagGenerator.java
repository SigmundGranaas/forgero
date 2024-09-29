package com.sigmundgranaas.forgero.content.compat.tags;

import com.sigmundgranaas.forgero.core.Forgero;
import com.sigmundgranaas.forgero.abstractions.utils.ModLoaderUtils;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;

import net.minecraft.util.Identifier;

public abstract class CommonTagGenerator {
	private final String mod;
	private final String namespace;
	private RuntimeResourcePack resourcePack;

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
		this.resourcePack = RuntimeResourcePack.create("%s:%s_common_tags".formatted(Forgero.NAMESPACE, mod));
		addTags();
		RRPCallback.BEFORE_VANILLA.register(a -> a.add(resourcePack));
	}

	public boolean isModPresent() {
		return ModLoaderUtils.isModPresent(mod);
	}

	protected void registerCommonItemTag(String item) {
		resourcePack.addTag(new Identifier("c", "items/" + item), JTag.tag().add(new Identifier(namespace, item)));
	}
}
