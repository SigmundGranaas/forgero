package com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.feature;

import com.sigmundgranaas.forgero.core.property.v2.feature.PropertyData;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.BaseWriter;
import com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.TooltipConfiguration;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

import static com.sigmundgranaas.forgero.minecraft.common.tooltip.v2.AttributeWriterHelper.number;

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
    public void write(List<Text> tooltip, TooltipContext context) {
        var extendedData = writeExtendedData().stream().map(entry -> indented(config.baseIndent() + 1).append(entry)).toList();
        MutableText header = writeDataHeader();
        if (extendedData.size() > 0) {
            header.append(separator());
        }
        tooltip.add(header);
        tooltip.addAll(extendedData);
    }

    protected List<Text> writeExtendedData() {
        return Collections.emptyList();
    }

    protected MutableText writeDataHeader() {
        return indented(config.baseIndent()).append(Text.translatable(String.format("tooltip.forgero.feature.%s", data.type().toLowerCase()))).formatted(neutral());
    }

    protected MutableText separator() {
        return Text.translatable("tooltip.forgero.separator");
    }

    public Text writeTextWithValue(String text, float value) {
        return Text.translatable(String.format("tooltip.forgero.%s", text)).formatted(neutral())
                .append(separator()).formatted(neutral())
                .append(indented(1).formatted())
                .append(Text.literal(number(value)).formatted(highlighted()));
    }

    public Text writeTextWithInfo(String text, String info) {
        return Text.translatable(String.format("tooltip.forgero.%s", text)).formatted(neutral())
                .append(Text.translatable("tooltip.forgero.separator")).formatted(neutral())
                .append(indented(1))
                .append(Text.translatable(String.format("tooltip.forgero.%s", info)).formatted(highlighted()));
    }
}
