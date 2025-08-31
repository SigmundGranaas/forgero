Here is the complete README documentation for the Forgero Property System.

# Forgero Property System Documentation

## 1. Overview

The Forgero Property System is a data-driven framework for adding custom behaviors (e.g., special mining patterns, on-hit effects) to tools and equipment. It allows developers and data pack creators to define complex logic in simple JSON files, which are then interpreted by the system at runtime.

This document outlines the architecture of the system and provides instructions for creating new properties and extending existing ones.

### Core Concepts

*   **Property:** A data object, defined in JSON, that represents a specific feature. It contains configuration for its behavior and optional conditions.
*   **Handler / Selector / Calculator:** Interfaces that define the logic of a property. A property's JSON `type` field determines which concrete implementation is used.
*   **Condition:** A set of rules that determine if a property is active. Conditions can be **static** (checked once when the item is created, e.g., `is_root`) or **dynamic** (checked at runtime, e.g., `target_has_tag`).
*   **Manager:** A static class that acts as the bridge between game events (via Mixins) and the Forgero property resolution engine.
*   **Engine:** A core class that handles the two-phase resolution process for a specific property type:
    1.  **Bake:** A one-time, cached process that gathers all potential properties from a component tree and filters them by static conditions.
    2.  **Apply:** A fast, runtime process that takes the "baked" result and filters it by dynamic conditions to get the final active properties.
*   **Plugin:** A class that registers the property's codec with Forgero's data loading system and initializes its Manager.

---

## 2. System Architecture

The system is designed to be modular and extensible. A single "property" is composed of several collaborating classes:

1.  **The Property Record (`OnHitProperty.java`)**:
    *   An immutable data class representing the property as defined in JSON.
    *   Implements `ConditionalProperty`.
    *   Contains fields for its logic handlers (e.g., `OnHitHandler`) and an optional `Condition`.
    *   Defines a `Codec` for serialization and key identifiers (`KEY`, `PROPERTY_KEY`).

2.  **The Handler Interface (`OnHitHandler.java`)**:
    *   Defines the contract for the property's logic (e.g., `void onHit(Entity source, Entity target)`).
    *   Uses a dispatch `Codec` that selects the correct implementation based on a `type` field in the JSON.

3.  **Concrete Handler Implementations (`FireHandler.java`, `ExplosionHandler.java`)**:
    *   Implement the Handler interface.
    *   Contain the actual game logic.
    *   Provide a `Codec` for their specific configuration fields.
    *   Are statically registered in the Property's Plugin class.

4.  **The Property Engine (`OnHitProperty.Engine.java`)**:
    *   Extends `AbstractConditionalPropertyEngine`.
    *   Connects the `PropertyKey` to the resolution logic, handling the bake/apply phases automatically.

5.  **The Manager (`OnHitManager.java`)**:
    *   Provides the static entry point for game code (Mixins).
    *   Uses the Forgero `Resolver` and the Property's `Engine` to get the active properties for a given context.
    *   Invokes the logic from the property's handlers.

6.  **The Plugin (`OnHitPropertiesPlugin.java`)**:
    *   Implements `DataPlugin` and `PostLoadPlugin`.
    *   Registers the property's master `Codec` with Forgero.
    *   Initializes the `Manager` with necessary services after data has been loaded.
    *   Contains the static registry for its associated handler implementations.

---

## 3. How to Create a New Property Type

This guide uses the `on_hit` property as an example.

### Step 1: Define the Property Record

Create a record that implements `ConditionalProperty`. This class mirrors the structure of your JSON object.

==== FILE: `/.../properties/minecraft/onhit/OnHitProperty.java` ====
```java
package com.sigmundgranaas.forgero.properties.minecraft.onhit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.condition.api.Condition;
import com.sigmundgranaas.forgero.core.property.api.PropertyKey;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.ConditionalProperty;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.OnHitHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record OnHitProperty(
		OnHitHandler handler,
		@Nullable Condition condition
) implements ConditionalProperty {
	public static final OpenIdentifier KEY_ID = new OpenIdentifier("minecraft", "on_hit");
	public static final ResolutionKey<List<OnHitProperty>> KEY = new ResolutionKey<>(KEY_ID);
	public static final PropertyKey<OnHitProperty> PROPERTY_KEY = new PropertyKey<>(OnHitProperty.class, KEY_ID.toString());

	public static Codec<OnHitProperty> codec(Codec<Condition> conditionCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				OnHitHandler.CODEC.fieldOf("handler").forGetter(OnHitProperty::handler),
				conditionCodec.optionalFieldOf("condition").forGetter(p -> Optional.ofNullable(p.condition()))
		).apply(instance, (handler, condition) -> new OnHitProperty(handler, condition.orElse(null))));
	}

	@Override
	public @Nullable Condition condition() {
		return condition;
	}

	public static class Engine extends AbstractConditionalPropertyEngine<OnHitProperty, List<OnHitProperty>> {
		public Engine() {
			super(KEY, PROPERTY_KEY);
		}

		@Override
		public List<OnHitProperty> apply(OptimizedBakedResult<OnHitProperty> baked, DynamicContext context) {
			return baked.stream(context).collect(Collectors.toList());
		}
	}
}
```

### Step 2: Define Handler Interfaces

Create an interface for the logic handlers. This interface must include a dispatch `Codec` that can deserialize different handler types.

==== FILE: `/.../properties/minecraft/onhit/handler/OnHitHandler.java` ====
```java
package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin;
import com.sigmundgranaas.forgero.utility.codec.DispatchCodecUtils;

import net.minecraft.entity.Entity;

public interface OnHitHandler {
	void onHit(Entity source, Entity target);

	String type();

	static Codec<? extends OnHitHandler> getCodec(String type) {
		Codec<? extends OnHitHandler> codec = OnHitPropertiesPlugin.getHandlerCodec(type);
		if (codec == null) {
			throw new IllegalArgumentException("Unknown OnHitHandler type: " + type);
		}
		return codec;
	}

	Codec<OnHitHandler> CODEC = DispatchCodecUtils.create(
			OnHitHandler::getCodec,
			OnHitHandler::type
	);
}
```

### Step 3: Implement Concrete Handlers

Create one or more classes that implement the handler interface. Each class must define its own `TYPE` string and `Codec`.

==== FILE: `/.../properties/minecraft/onhit/handler/FireHandler.java` ====
```java
package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.entity.Entity;

public record FireHandler(int duration) implements OnHitHandler {
	public static final String TYPE = "forgero:fire";
	public static final Codec<FireHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("duration").forGetter(FireHandler::duration)
	).apply(instance, FireHandler::new));

	@Override
	public void onHit(Entity source, Entity target) {
		target.setFireTicks(duration * 20); // duration in seconds
	}

	@Override
	public String type() {
		return TYPE;
	}
}
```

### Step 4: Create the Property Manager

This class is the public API for your property. It is called by Mixins to trigger the property resolution and execution.

==== FILE: `/.../properties/minecraft/onhit/OnHitManager.java` ====
```java
package com.sigmundgranaas.forgero.properties.minecraft.onhit;

import com.sigmundgranaas.forgero.common.convert.ComponentConverter;
import com.sigmundgranaas.forgero.core.component.api.Component;
import com.sigmundgranaas.forgero.core.property.api.Resolver;
import com.sigmundgranaas.forgero.core.property.context.ContextKeys;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
// ... other imports

public class OnHitManager {
	// ... static fields for converter, resolver, etc.

	public static void initialize(ComponentConverter converter, Resolver resolver) {
		// Initialization logic
	}

	public static void handleOnHit(ItemStack stack, Entity source, Entity target) {
		if (!initialized || stack.isEmpty()) {
			return;
		}

		converter.toComponent(stack).ifPresent(component -> {
			// Resolve active properties using the engine and a dynamic context
			List<OnHitProperty> properties = getActiveProperties(component, target);
			// Execute the handler logic for each active property
			properties.forEach(prop -> prop.handler().onHit(source, target));
		});
	}
	
	private static List<OnHitProperty> getActiveProperties(Component component, Entity target) {
        var engine = new OnHitProperty.Engine();
        DynamicContext.Builder contextBuilder = new DynamicContext.Builder();
        // Populate context for dynamic conditions
        // ...
        return resolver.resolve(component, engine, contextBuilder.build());
    }
}
```

### Step 5: Create and Register the Plugin

The plugin registers your property's codec, initializes the manager, and statically registers your concrete handlers.

==== FILE: `/.../properties/minecraft/onhit/OnHitPropertiesPlugin.java` ====
```java
package com.sigmundgranaas.forgero.properties.minecraft.onhit;

import com.mojang.serialization.Codec;
import com.sigmundgranaas.forgero.core.property.api.codec.ListCodecWrapper;
import com.sigmundgranaas.forgero.loader.api.DataPlugin;
import com.sigmundgranaas.forgero.loader.api.PluginRegistrationContext;
import com.sigmundgranaas.forgero.loader.api.PostLoadPlugin;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.FireHandler;
import com.sigmundgranaas.forgero.properties.minecraft.onhit.handler.OnHitHandler;
// ... other handler imports

public class OnHitPropertiesPlugin implements DataPlugin, PostLoadPlugin {

	private static final Map<String, Codec<? extends OnHitHandler>> HANDLERS = new ConcurrentHashMap<>();

	// Statically register all concrete handlers
	static {
		register(FireHandler.TYPE, FireHandler.CODEC);
		// register(ExplosionHandler.TYPE, ExplosionHandler.CODEC);
	}

	public static void register(String type, Codec<? extends OnHitHandler> codec) {
		HANDLERS.put(type, codec);
	}

	public static Codec<? extends OnHitHandler> getHandlerCodec(String type) {
		return HANDLERS.get(type);
	}

	@Override
	public void register(PluginRegistrationContext context) {
		// Register the main property codec
		context.registerPropertyCodec(
				OnHitProperty.PROPERTY_KEY,
				conditionCodecSupplier -> ListCodecWrapper.of(OnHitProperty.codec(conditionCodecSupplier.get()))
		);
	}

	@Override
	public void onDataLoaded(DataLoadingContext context) {
		// Initialize the manager
		OnHitManager.initialize(context.getConverter(), context.getResolver());
	}

    // ... getId method
}
```

### Step 6: Add Plugin to `fabric.mod.json`

Register your plugin class in the `forgero:data_plugin` and `forgero:post_load_plugin` entrypoints.

==== FILE: `fabric.mod.json` ====
```json
{
  "entrypoints": {
    "forgero:data_plugin": [
      "com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingPropertiesPlugin",
      "com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin"
    ],
    "forgero:post_load_plugin": [
      "com.sigmundgranaas.forgero.properties.minecraft.blockbreaking.BlockBreakingPropertiesPlugin",
      "com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitPropertiesPlugin"
    ]
  }
}
```

### Step 7: Add Mixins

Finally, create Mixins to inject calls to your `Manager` into the appropriate game events.

==== FILE: `/.../properties/minecraft/mixin/onhit/PlayerEntityMixin.java` ====
```java
package com.sigmundgranaas.forgero.properties.minecraft.mixin.onhit;

import com.sigmundgranaas.forgero.properties.minecraft.onhit.OnHitManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

	@Shadow
	public abstract ItemStack getMainHandStack();

	@Inject(method = "attack", at = @At("HEAD"))
	public void forgero$onHitMixin(Entity target, CallbackInfo ci) {
		PlayerEntity self = (PlayerEntity) (Object) this;
		ItemStack stack = this.getMainHandStack();
		if (!stack.isEmpty()) {
			OnHitManager.handleOnHit(stack, self, target);
		}
	}
}
```

---

## 4. How to Extend an Existing Property (Add a New Handler)

If a property type like `on_hit` already exists, adding new behavior is much simpler.

### Step 1: Create the New Handler Class

Implement the existing handler interface (e.g., `OnHitHandler`) and provide its logic, `TYPE`, and `Codec`.

==== FILE: `/.../properties/minecraft/onhit/handler/SoundHandler.java` ====
```java
package com.sigmundgranaas.forgero.properties.minecraft.onhit.handler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record SoundHandler(Identifier sound, float volume, float pitch) implements OnHitHandler {
    public static final String TYPE = "forgero:play_sound";
    public static final Codec<SoundHandler> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("sound").forGetter(SoundHandler::sound),
            Codec.FLOAT.optionalFieldOf("volume", 1.0f).forGetter(SoundHandler::volume),
            Codec.FLOAT.optionalFieldOf("pitch", 1.0f).forGetter(SoundHandler::pitch)
    ).apply(instance, SoundHandler::new));

    @Override
    public void onHit(Entity source, Entity target) {
        SoundEvent soundEvent = Registries.SOUND_EVENT.get(sound);
        if (soundEvent != null) {
            target.getWorld().playSound(null, target.getBlockPos(), soundEvent, target.getSoundCategory(), volume, pitch);
        }
    }

    @Override
    public String type() {
        return TYPE;
    }
}
```

### Step 2: Register the Handler in the Plugin

Add a single line to the static registration block in the corresponding `PropertiesPlugin` class.

==== FILE: `/.../properties/minecraft/onhit/OnHitPropertiesPlugin.java` ====
```java
public class OnHitPropertiesPlugin implements DataPlugin, PostLoadPlugin {

	private static final Map<String, Codec<? extends OnHitHandler>> HANDLERS = new ConcurrentHashMap<>();

	static {
		register(FireHandler.TYPE, FireHandler.CODEC);
		register(ExplosionHandler.TYPE, ExplosionHandler.CODEC);
		register(StatusEffectHandler.TYPE, StatusEffectHandler.CODEC);
		// Add the new handler here
		register(SoundHandler.TYPE, SoundHandler.CODEC); 
	}
    
    // ... rest of the class
}
```
No other changes are needed. The dispatch codec system will now recognize `"type": "forgero:play_sound"` in JSON files.

---

## 5. JSON Schema and Examples

Below is an example of how to use the `on_hit` property in a component's JSON definition.

### Example JSON for `minecraft:on_hit`

```json
{
  "properties": {
    "minecraft:on_hit": [
      {
        "handler": {
          "type": "forgero:explosion",
          "power": 1.5
        },
        "condition": {
          "type": "forgero:target_has_tag",
          "tag": "minecraft:undead"
        }
      },
      {
        "handler": {
          "type": "forgero:fire",
          "duration": 5
        }
      },
      {
        "handler": {
          "type": "forgero:play_sound",
          "sound": "minecraft:entity.wither.shoot",
          "volume": 0.8,
          "pitch": 1.2
        }
      }
    ]
  }
}
```

*   The property key (e.g., `"minecraft:on_hit"`) maps to an **array** of property objects.
*   Each object has a `"handler"` which defines the logic. The `"type"` field within the handler selects the implementation.
*   An optional `"condition"` can be added to make the effect trigger selectively. If omitted, the effect is always applied.
