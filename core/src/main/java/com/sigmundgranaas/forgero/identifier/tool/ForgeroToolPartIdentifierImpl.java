package com.sigmundgranaas.forgero.identifier.tool;

import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierFactory;
import com.sigmundgranaas.forgero.identifier.ForgeroIdentifierType;
import com.sigmundgranaas.forgero.identifier.ForgeroSchematicIdentifier;
import com.sigmundgranaas.forgero.toolpart.ForgeroToolPartTypes;

import java.util.Locale;

import static com.sigmundgranaas.forgero.identifier.Common.ELEMENT_SEPARATOR;

public class ForgeroToolPartIdentifierImpl extends AbstractForgeroIdentifier implements ForgeroToolPartIdentifier {
    protected final String toolPartName;

    public ForgeroToolPartIdentifierImpl(String forgeroName) {
        super(ForgeroIdentifierType.TOOL_PART);
        this.toolPartName = forgeroName;
    }

    @Override
    public ForgeroMaterialIdentifier getMaterial() {
        return (ForgeroMaterialIdentifier) ForgeroIdentifierFactory.INSTANCE.createForgeroIdentifier(toolPartName.split(ELEMENT_SEPARATOR)[0]);
    }

    @Override
    public ForgeroSchematicIdentifier getSchematic() {
        return new ForgeroSchematicIdentifier(String.format("%s%sschematic", toolPartName.split(ELEMENT_SEPARATOR)[1].toLowerCase(Locale.ROOT), ELEMENT_SEPARATOR).replace("head", ""));
    }

    @Override
    public ForgeroToolPartTypes getToolPartType() {
        if (toolPartName.contains("head")) {
            return ForgeroToolPartTypes.valueOf("HEAD");
        } else {
            return ForgeroToolPartTypes.valueOf(toolPartName.split(ELEMENT_SEPARATOR)[1].toUpperCase(Locale.ROOT));
        }

    }

    @Override
    public String getIdentifier() {
        return toolPartName;
    }
}
