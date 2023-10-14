package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.section;

import static com.sigmundgranaas.forgero.core.util.Identifiers.EMPTY_IDENTIFIER;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sigmundgranaas.forgero.core.property.PropertyContainer;
import com.sigmundgranaas.forgero.core.property.v2.feature.BasePredicateFeature;
import com.sigmundgranaas.forgero.core.property.v2.feature.Feature;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.WriterHelper;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.Text;

public class FeatureSectionWriter extends SectionWriter {
	private final PropertyContainer container;


	public FeatureSectionWriter(PropertyContainer container, TooltipConfiguration configuration) {
		super(configuration);
		this.container = container;
	}


	public static Optional<SectionWriter> of(PropertyContainer container) {
		return of(container, TooltipConfiguration.builder().build());
	}

	public static Optional<SectionWriter> of(PropertyContainer container, TooltipConfiguration configuration) {
		SectionWriter writer = new FeatureSectionWriter(container, configuration);
		if (writer.shouldWrite()) {
			return Optional.of(writer);
		}
		return Optional.empty();
	}

	@Override
	public boolean shouldWrite() {
		return !container.stream().features().toList().isEmpty();
	}

	@Override
	public void write(List<Text> tooltip, TooltipContext context) {
		if (!configuration.hideSectionTitle()) {
			tooltip.add(createSection("features"));
		}
		tooltip.addAll(entries());

		super.write(tooltip, context);
	}

	@Override
	public int getSectionOrder() {
		return 0;
	}

	@Override
	public List<Text> entries() {
		TooltipConfiguration featureConfig;
		if (!configuration.hideSectionTitle()) {
			featureConfig = configuration.baseIndent(1);

		} else {
			featureConfig = configuration;
		}
		return container.stream()
				.features()
				.map(feature -> BaseFeatureWriter.of(feature, new WriterHelper(featureConfig)))
				.flatMap(Optional::stream)
				.map(BaseFeatureWriter::write)
				.flatMap(List::stream)
				.toList();
	}

	public static class BaseFeatureWriter {
		private final BasePredicateFeature feature;
		private final WriterHelper helper;

		private BaseFeatureWriter(BasePredicateFeature feature, WriterHelper helper) {
			this.feature = feature;
			this.helper = helper;
		}

		public static Optional<BaseFeatureWriter> of(Feature feature, WriterHelper helper) {
			if (feature instanceof BasePredicateFeature base) {
				if (!base.title().equals(EMPTY_IDENTIFIER)) {
					return Optional.of(new BaseFeatureWriter(base, helper));
				}
			}
			return Optional.empty();
		}

		public List<Text> write() {
			List<Text> entries = new ArrayList<>();

			Text title = helper.writeBase()
					.append(Text.translatable(feature.title()))
					.formatted(helper.neutral());
			entries.add(title);
			if (!feature.description().equals(EMPTY_IDENTIFIER)) {
				Text description = helper.writeBase()
						.append(" ")
						.append(Text.translatable(feature.description()))
						.formatted(helper.base());
				entries.add(description);
			}
			return entries;
		}
	}
}
