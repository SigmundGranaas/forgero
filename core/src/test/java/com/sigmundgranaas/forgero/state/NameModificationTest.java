package com.sigmundgranaas.forgero.state;

import com.sigmundgranaas.forgero.core.api.identity.DefaultRules;
import com.sigmundgranaas.forgero.core.api.identity.ModificationRuleBuilder;
import com.sigmundgranaas.forgero.core.state.State;
import com.sigmundgranaas.forgero.testutil.ToolParts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NameModificationTest {
	private String apply(ModificationRuleBuilder builder, State state) {
		return builder.build().transformation().apply(state.name());
	}

	@Test
	public void testPickaxeHead() {
		Assertions.assertEquals(apply(DefaultRules.head, ToolParts.PICKAXE_HEAD), "iron-pickaxe");
	}

	@Test
	public void testSwordBlade() {
		Assertions.assertEquals(apply(DefaultRules.blade, ToolParts.SWORD_BLADE), "iron-sword");
	}

	@Test
	public void handleIgnored() {
		Assertions.assertEquals(apply(DefaultRules.handle, ToolParts.HANDLE), "");
	}
}
