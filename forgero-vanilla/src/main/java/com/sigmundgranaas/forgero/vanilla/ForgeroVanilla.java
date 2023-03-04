package com.sigmundgranaas.forgero.vanilla;

import com.sigmundgranaas.forgero.core.resource.data.v2.PackageSupplier;
import com.sigmundgranaas.forgero.core.resource.data.v2.packages.FilePackageLoader;

import java.util.List;

import static com.sigmundgranaas.forgero.core.resource.data.Constant.MINECRAFT_PACKAGE;
import static com.sigmundgranaas.forgero.core.resource.data.Constant.VANILLA_PACKAGE;

public class ForgeroVanilla {
	public static PackageSupplier VANILLA_SUPPLIER = () -> List.of(new FilePackageLoader(MINECRAFT_PACKAGE).get(), new FilePackageLoader(VANILLA_PACKAGE).get());

}
