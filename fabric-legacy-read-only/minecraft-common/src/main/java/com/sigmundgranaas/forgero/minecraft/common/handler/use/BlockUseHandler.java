package com.sigmundgranaas.forgero.minecraft.common.handler.use;

import com.sigmundgranaas.forgero.core.property.v2.feature.ClassKey;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;

public interface BlockUseHandler extends BaseHandler {
	ClassKey<BlockUseHandler> KEY = new ClassKey<>("minecraft:block_use_handler", BlockUseHandler.class);

	ActionResult useOnBlock(ItemUsageContext context);
}
