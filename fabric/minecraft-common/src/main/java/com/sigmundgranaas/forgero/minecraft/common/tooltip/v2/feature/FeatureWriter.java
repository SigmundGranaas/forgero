package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import static com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper.number;

import java.util.Collections;
import java.util.List;

import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.BaseWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TagWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import com.sigmundgranaas.forgero.minecraft.common.utils.Text;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class FeatureWriter extends BaseWriter {
	protected final PropertyData data;
	protected TooltipConfiguration config = TooltipConfiguration.builder().build();

	public FeatureWriter(PropertyData data) {
		super();
		this.data = data;
	}

	public FeatureWriter setConfig(TooltipConfiguration configuration) {
		this.config = configuration;
		return this;
	}


	@Override
	public void write(List<net.minecraft.text.Text> tooltip, TooltipContext context) {
		var extendedData = writeExtendedData().stream().map(entry -> indented(config.baseIndent() + 1).append(entry)).toList();
		MutableText header = writeDataHeader();
		if (extendedData.size() > 0) {
			header.append(separator());
		}
		tooltip.add(header);
		tooltip.addAll(extendedData);
	}

	protected List<net.minecraft.text.Text> writeExtendedData() {
		if (data.getTags().size() > 0) {
			MutableText against = Text.translatable("tooltip.forgero.against").formatted(Formatting.GRAY);
			return List.of(against.append(TagWriter.writeTagList(data.getTags())));
		}
		return Collections.emptyList();
	}

	protected MutableText writeDataHeader() {
		return indented(config.baseIndent()).append(Text.translatable(String.format("tooltip.forgero.feature.%s", data.type().toLowerCase()))).formatted(neutral());
	}

	protected MutableText separator() {
		return Text.translatable("tooltip.forgero.separator");
	}

	public net.minecraft.text.Text writeTextWithValue(String text, float value) {
		return writeTextWithValue(text, number(value));
	}

	public net.minecraft.text.Text writeTextWithValue(String text, String value) {
		return Text.translatable(String.format("tooltip.forgero.%s", text)).formatted(neutral())
				.append(separator()).formatted(neutral())
				.append(indented(1).formatted())
				.append(Text.literal(value).formatted(highlighted()));
	}

	public net.minecraft.text.Text writeTextWithInfo(String text, String info) {
		return Text.translatable(String.format("tooltip.forgero.%s", text)).formatted(neutral())
				.append(Text.translatable("tooltip.forgero.separator")).formatted(neutral())
				.append(indented(1))
				.append(Text.translatable(String.format("tooltip.forgero.%s", info)).formatted(highlighted()));
	}
}
