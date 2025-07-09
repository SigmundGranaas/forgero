package com.sigmundgranaas.forgero.property;

import com.sigmundgranaas.forgero.core.property.api.PropertyRegistry;
import com.sigmundgranaas.forgero.data.mapper.api.PropertyCodec;
import com.sigmundgranaas.forgero.data.mapper.impl.AttributeCodec;
import com.sigmundgranaas.forgero.data.mapper.impl.FeatureCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class PropertyRegistryTest {

	@BeforeEach
	void setUp() {
		// Reset the registry to a clean state before each test
		PropertyRegistry.getInstance().reset();
	}

	@Test
	void getInstanceReturnsSingleton() {
		PropertyRegistry instance1 = PropertyRegistry.getInstance();
		PropertyRegistry instance2 = PropertyRegistry.getInstance();
		Assertions.assertSame(instance1, instance2, "getInstance should always return the same instance.");
	}

	@Test
	void resetPopulatesWithCoreCodecs() {
		List<PropertyCodec<?>> codecs = PropertyRegistry.getInstance().getPropertyCodecs();
		Assertions.assertFalse(codecs.isEmpty(), "Registry should not be empty after reset.");

		boolean hasAttributeCodec = codecs.stream().anyMatch(codec -> codec instanceof AttributeCodec);
		boolean hasFeatureCodec = codecs.stream().anyMatch(codec -> codec instanceof FeatureCodec);

		Assertions.assertTrue(hasAttributeCodec, "Registry should contain an AttributeCodec after reset.");
		Assertions.assertTrue(hasFeatureCodec, "Registry should contain a FeatureCodec after reset.");
	}

	@Test
	void canRegisterAndRetrieveCustomCodec() {
		// A simple mock codec for testing
		PropertyCodec<?> mockCodec = new MockPropertyCodec();
		PropertyRegistry.getInstance().registerPropertyCodec(mockCodec);

		List<PropertyCodec<?>> codecs = PropertyRegistry.getInstance().getPropertyCodecs();
		boolean hasMockCodec = codecs.stream().anyMatch(codec -> codec instanceof MockPropertyCodec);

		Assertions.assertTrue(hasMockCodec, "Registry should contain the newly registered mock codec.");
	}

	@Test
	void doesNotRegisterDuplicateCodecTypes() {
		PropertyRegistry registry = PropertyRegistry.getInstance();
		long initialCount = registry.getPropertyCodecs().stream()
				.filter(codec -> codec.getPropertyType().equals("forgero:attributes"))
				.count();

		Assertions.assertEquals(1, initialCount);

		// Attempt to register another codec for the same type
		registry.registerPropertyCodec(new AttributeCodec(null, null)); // Dependencies are not used for this check

		long finalCount = registry.getPropertyCodecs().stream()
				.filter(codec -> codec.getPropertyType().equals("forgero:attributes"))
				.count();

		Assertions.assertEquals(1, finalCount, "Registry should not allow registering a duplicate property type.");
	}

	// Mock codec class for testing registration
	static class MockPropertyCodec implements PropertyCodec<com.sigmundgranaas.forgero.data.loading.api.data.PropertyData> {
		@Override
		public String getPropertyType() {
			return "forgero:mock";
		}

		@Override
		public Class<com.sigmundgranaas.forgero.data.loading.api.data.PropertyData> getPropertyDataType() {
			return com.sigmundgranaas.forgero.data.loading.api.data.PropertyData.class;
		}

		@Override
		public java.util.List<com.sigmundgranaas.forgero.core.property.api.Property> build(java.util.List<com.sigmundgranaas.forgero.data.loading.api.data.PropertyData> dataList) {
			return java.util.Collections.emptyList();
		}

		@Override
		public com.sigmundgranaas.forgero.data.loading.api.data.PropertyData toData(com.sigmundgranaas.forgero.core.property.api.Property property) {
			return null;
		}

		@Override
		public com.mojang.serialization.Codec<java.util.List<com.sigmundgranaas.forgero.data.loading.api.data.PropertyData>> getCodec() {
			return null;
		}
	}
}
