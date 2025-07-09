package com.sigmundgranaas.forgero.property.tooltip;

import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;
import com.sigmundgranaas.forgero.core.property.api.custom.AbstractConditionalPropertyEngine;
import com.sigmundgranaas.forgero.core.property.api.custom.OptimizedBakedResult;
import com.sigmundgranaas.forgero.core.property.context.DynamicContext;

import java.util.List;

import static com.sigmundgranaas.forgero.property.tooltip.DefaultTooltipKeys.TOOLTIPS;


/**
 * The expert engine for resolving Tooltip properties.
 * <p>
 * Intermediate Baked Type {@code <B>}: {@link OptimizedBakedResult}
 * Final Result Type {@code <R>}: {@code List<TooltipProperty>}
 */
public class TooltipEngine extends AbstractConditionalPropertyEngine<TooltipProperty, List<TooltipProperty>> {
	public static final ResolutionKey<List<TooltipProperty>> KEY = TOOLTIPS;

	public TooltipEngine() {
		super(KEY, TooltipProperty.class);
	}

	@Override
	public List<TooltipProperty> apply(OptimizedBakedResult<TooltipProperty> baked, DynamicContext context) {
		return baked.stream(context).toList();
	}
}
