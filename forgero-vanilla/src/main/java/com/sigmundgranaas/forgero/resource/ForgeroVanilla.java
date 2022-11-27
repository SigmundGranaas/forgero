package com.sigmundgranaas.forgero.resource;

import com.sigmundgranaas.forgero.resource.data.v2.FilePackageLoader;
import com.sigmundgranaas.forgero.resource.data.v2.PackageSupplier;

import java.util.List;

import static com.sigmundgranaas.forgero.resource.data.Constant.MINECRAFT_PACKAGE;
import static com.sigmundgranaas.forgero.resource.data.Constant.VANILLA_PACKAGE;

public class ForgeroVanilla {
    public static PackageSupplier VANILLA_SUPPLIER = () -> List.of(new FilePackageLoader(MINECRAFT_PACKAGE).get(), new FilePackageLoader(VANILLA_PACKAGE).get());

}
