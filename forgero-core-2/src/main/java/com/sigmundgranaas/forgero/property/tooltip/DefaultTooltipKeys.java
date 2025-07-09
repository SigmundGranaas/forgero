package com.sigmundgranaas.forgero.property.tooltip;

import com.sigmundgranaas.forgero.common.identifier.api.OpenIdentifier;
import com.sigmundgranaas.forgero.core.property.api.ResolutionKey;

import java.util.List;

/**
 * Central registry for keys related to Tooltip properties.
 * This class provides the type-safe {@link ResolutionKey} for requesting Tooltip data
 * from the {@link com.sigmundgranaas.forgero.core.property.api.Resolver}.
 */
public class DefaultTooltipKeys {
	public static final OpenIdentifier TOOLTIP_SECTION_IDENTIFIER = new OpenIdentifier("forgero", "tooltip_sections");
	public static final ResolutionKey<List<TooltipProperty>> TOOLTIPS = new ResolutionKey<>(TOOLTIP_SECTION_IDENTIFIER);
}
