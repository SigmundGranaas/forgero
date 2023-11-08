package com.sigmundgranaas.forgeroforge;

import static com.sigmundgranaas.forgeroforge.testutil.ToolParts.HANDLE;
import static com.sigmundgranaas.forgeroforge.testutil.Upgrades.REDSTONE_GEM;

import com.sigmundgranaas.forgero.core.property.v2.attribute.attributes.AttackDamage;
import com.sigmundgranaas.forgeroforge.testutil.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HandleStateTest {

	@Test
	void testCreateHandleState() {
		var handle = HANDLE;
		Assertions.assertEquals("oak-handle", handle.name());
	}

	@Test
	void testHandleProperties() {
		var handle = HANDLE;
		Assertions.assertTrue(handle.getProperties().size() > 0);
	}

	@Test
	void testHandleType() {
		var handle = HANDLE;
		Assertions.assertTrue(handle.test(Types.HANDLE));
	}

	@Test
	void testHandleTypeMaterial() {
		var handle = HANDLE;
		Assertions.assertTrue(handle.test(Types.MATERIAL));
	}

	@Test
	void testHandleUpgradeIsNotMutated() {
		var handle = HANDLE;
		Assertions.assertNotEquals(handle, handle.upgrade(REDSTONE_GEM));
	}

	@Test
	void testHandleUpgradeAppliesProperty() {
		var handle = HANDLE.upgrade(REDSTONE_GEM);
		Assertions.assertTrue(handle.stream().applyAttribute(AttackDamage.KEY) > HANDLE.stream().applyAttribute(AttackDamage.KEY));
	}
}
