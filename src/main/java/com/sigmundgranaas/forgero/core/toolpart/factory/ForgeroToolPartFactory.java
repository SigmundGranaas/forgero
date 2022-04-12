package com.sigmundgranaas.forgero.core.toolpart.factory;

import com.sigmundgranaas.forgero.core.identifier.tool.ForgeroToolPartIdentifier;
import com.sigmundgranaas.forgero.core.material.MaterialCollection;
import com.sigmundgranaas.forgero.core.material.material.PrimaryMaterial;
import com.sigmundgranaas.forgero.core.pattern.HeadPattern;
import com.sigmundgranaas.forgero.core.pattern.Pattern;
import com.sigmundgranaas.forgero.core.pattern.PatternCollection;
import com.sigmundgranaas.forgero.core.toolpart.ForgeroToolPart;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ForgeroToolPartFactory {
    ForgeroToolPartFactory INSTANCE = ForgeroToolPartFactoryImpl.getInstance();

    @NotNull
    ForgeroToolPart createToolPart(@NotNull ForgeroToolPartIdentifier identifier);

    @NotNull
    ToolPartHeadBuilder createToolPartHeadBuilder(@NotNull PrimaryMaterial material, HeadPattern pattern);

    @NotNull
    ToolPartHandleBuilder createToolPartHandleBuilder(@NotNull PrimaryMaterial material, @NotNull Pattern pattern);

    @NotNull
    ToolPartBindingBuilder createToolPartBindingBuilder(@NotNull PrimaryMaterial material, @NotNull Pattern pattern);

    @NotNull
    ToolPartBuilder createToolPartBuilderFromToolPart(@NotNull ForgeroToolPart toolPart);

    @NotNull
    List<ForgeroToolPart> createBaseToolParts(@NotNull MaterialCollection collection, PatternCollection patternCollection);
}
