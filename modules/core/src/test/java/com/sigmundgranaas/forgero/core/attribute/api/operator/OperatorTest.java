package com.sigmundgranaas.forgero.core.attribute.api.operator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Operator Tests")
class OperatorTest {

	@Nested
	@DisplayName("AdditionOperator")
	class AdditionOperatorTest {

		private final AdditionOperator operator = AdditionOperator.getInstance();

		@Test
		@DisplayName("should add positive values")
		void testAddPositiveValues() {
			assertEquals(15.0f, operator.apply(10.0f, 5.0f), 0.001f);
			assertEquals(100.5f, operator.apply(50.25f, 50.25f), 0.001f);
		}

		@Test
		@DisplayName("should add zero")
		void testAddZero() {
			assertEquals(10.0f, operator.apply(10.0f, 0.0f), 0.001f);
			assertEquals(0.0f, operator.apply(0.0f, 0.0f), 0.001f);
		}

		@Test
		@DisplayName("should add negative values")
		void testAddNegativeValues() {
			assertEquals(5.0f, operator.apply(10.0f, -5.0f), 0.001f);
			assertEquals(-15.0f, operator.apply(-10.0f, -5.0f), 0.001f);
			assertEquals(0.0f, operator.apply(10.0f, -10.0f), 0.001f);
		}

		@Test
		@DisplayName("should have order 1")
		void testOrder() {
			assertEquals(1, operator.order());
		}

		@Test
		@DisplayName("should return same instance (singleton)")
		void testSingleton() {
			assertSame(operator, AdditionOperator.getInstance());
		}
	}

	@Nested
	@DisplayName("SubtractionOperator")
	class SubtractionOperatorTest {

		private final SubtractionOperator operator = SubtractionOperator.getInstance();

		@Test
		@DisplayName("should subtract positive values")
		void testSubtractPositiveValues() {
			assertEquals(5.0f, operator.apply(10.0f, 5.0f), 0.001f);
			assertEquals(0.0f, operator.apply(50.25f, 50.25f), 0.001f);
		}

		@Test
		@DisplayName("should subtract zero")
		void testSubtractZero() {
			assertEquals(10.0f, operator.apply(10.0f, 0.0f), 0.001f);
			assertEquals(0.0f, operator.apply(0.0f, 0.0f), 0.001f);
		}

		@Test
		@DisplayName("should subtract negative values")
		void testSubtractNegativeValues() {
			assertEquals(15.0f, operator.apply(10.0f, -5.0f), 0.001f);
			assertEquals(-5.0f, operator.apply(-10.0f, -5.0f), 0.001f);
			assertEquals(20.0f, operator.apply(10.0f, -10.0f), 0.001f);
		}

		@Test
		@DisplayName("should have order 1")
		void testOrder() {
			assertEquals(1, operator.order());
		}

		@Test
		@DisplayName("should return same instance (singleton)")
		void testSingleton() {
			assertSame(operator, SubtractionOperator.getInstance());
		}
	}

	@Nested
	@DisplayName("MultiplicationOperator")
	class MultiplicationOperatorTest {

		private final MultiplicationOperator operator = MultiplicationOperator.getInstance();

		@Test
		@DisplayName("should multiply positive values")
		void testMultiplyPositiveValues() {
			assertEquals(50.0f, operator.apply(10.0f, 5.0f), 0.001f);
			assertEquals(100.0f, operator.apply(10.0f, 10.0f), 0.001f);
			assertEquals(12.5f, operator.apply(5.0f, 2.5f), 0.001f);
		}

		@Test
		@DisplayName("should multiply by zero")
		void testMultiplyByZero() {
			assertEquals(0.0f, operator.apply(10.0f, 0.0f), 0.001f);
			assertEquals(0.0f, operator.apply(0.0f, 10.0f), 0.001f);
			assertEquals(0.0f, operator.apply(0.0f, 0.0f), 0.001f);
		}

		@Test
		@DisplayName("should multiply negative values")
		void testMultiplyNegativeValues() {
			assertEquals(-50.0f, operator.apply(10.0f, -5.0f), 0.001f);
			assertEquals(50.0f, operator.apply(-10.0f, -5.0f), 0.001f);
			assertEquals(-100.0f, operator.apply(-10.0f, 10.0f), 0.001f);
		}

		@Test
		@DisplayName("should have order 2")
		void testOrder() {
			assertEquals(2, operator.order());
		}

		@Test
		@DisplayName("should return same instance (singleton)")
		void testSingleton() {
			assertSame(operator, MultiplicationOperator.getInstance());
		}
	}

	@Nested
	@DisplayName("DivisionOperator")
	class DivisionOperatorTest {

		private final DivisionOperator operator = DivisionOperator.getInstance();

		@Test
		@DisplayName("should divide positive values")
		void testDividePositiveValues() {
			assertEquals(2.0f, operator.apply(10.0f, 5.0f), 0.001f);
			assertEquals(1.0f, operator.apply(10.0f, 10.0f), 0.001f);
			assertEquals(5.0f, operator.apply(10.0f, 2.0f), 0.001f);
		}

		@Test
		@DisplayName("should throw exception when dividing by zero")
		void testDivisionByZero() {
			ArithmeticException exception = assertThrows(
					ArithmeticException.class,
					() -> operator.apply(10.0f, 0.0f)
			);
			assertEquals("Division by zero", exception.getMessage());
		}

		@Test
		@DisplayName("should divide by negative values")
		void testDivideByNegativeValues() {
			assertEquals(-2.0f, operator.apply(10.0f, -5.0f), 0.001f);
			assertEquals(2.0f, operator.apply(-10.0f, -5.0f), 0.001f);
			assertEquals(-1.0f, operator.apply(-10.0f, 10.0f), 0.001f);
		}

		@Test
		@DisplayName("should handle fractional results")
		void testFractionalResults() {
			assertEquals(2.5f, operator.apply(10.0f, 4.0f), 0.001f);
			assertEquals(0.5f, operator.apply(1.0f, 2.0f), 0.001f);
			assertEquals(3.333333f, operator.apply(10.0f, 3.0f), 0.001f);
		}

		@Test
		@DisplayName("should divide zero by non-zero")
		void testDivideZero() {
			assertEquals(0.0f, operator.apply(0.0f, 10.0f), 0.001f);
			assertEquals(0.0f, operator.apply(0.0f, -5.0f), 0.001f);
		}

		@Test
		@DisplayName("should have order 3")
		void testOrder() {
			assertEquals(3, operator.order());
		}

		@Test
		@DisplayName("should return same instance (singleton)")
		void testSingleton() {
			assertSame(operator, DivisionOperator.getInstance());
		}
	}

	@Nested
	@DisplayName("MinOperator")
	class MinOperatorTest {

		private final MinOperator operator = MinOperator.getInstance();

		@Test
		@DisplayName("should return minimum of positive values")
		void testMinPositiveValues() {
			assertEquals(5.0f, operator.apply(10.0f, 5.0f), 0.001f);
			assertEquals(5.0f, operator.apply(5.0f, 10.0f), 0.001f);
			assertEquals(10.0f, operator.apply(10.0f, 10.0f), 0.001f);
		}

		@Test
		@DisplayName("should handle zero")
		void testMinWithZero() {
			assertEquals(0.0f, operator.apply(10.0f, 0.0f), 0.001f);
			assertEquals(0.0f, operator.apply(0.0f, 10.0f), 0.001f);
			assertEquals(0.0f, operator.apply(0.0f, 0.0f), 0.001f);
		}

		@Test
		@DisplayName("should return minimum with negative values")
		void testMinNegativeValues() {
			assertEquals(-10.0f, operator.apply(10.0f, -10.0f), 0.001f);
			assertEquals(-15.0f, operator.apply(-10.0f, -15.0f), 0.001f);
			assertEquals(-5.0f, operator.apply(-5.0f, 5.0f), 0.001f);
		}

		@Test
		@DisplayName("should have order 4")
		void testOrder() {
			assertEquals(4, operator.order());
		}

		@Test
		@DisplayName("should return same instance (singleton)")
		void testSingleton() {
			assertSame(operator, MinOperator.getInstance());
		}
	}

	@Nested
	@DisplayName("MaxOperator")
	class MaxOperatorTest {

		private final MaxOperator operator = MaxOperator.getInstance();

		@Test
		@DisplayName("should return maximum of positive values")
		void testMaxPositiveValues() {
			assertEquals(10.0f, operator.apply(10.0f, 5.0f), 0.001f);
			assertEquals(10.0f, operator.apply(5.0f, 10.0f), 0.001f);
			assertEquals(10.0f, operator.apply(10.0f, 10.0f), 0.001f);
		}

		@Test
		@DisplayName("should handle zero")
		void testMaxWithZero() {
			assertEquals(10.0f, operator.apply(10.0f, 0.0f), 0.001f);
			assertEquals(10.0f, operator.apply(0.0f, 10.0f), 0.001f);
			assertEquals(0.0f, operator.apply(0.0f, 0.0f), 0.001f);
		}

		@Test
		@DisplayName("should return maximum with negative values")
		void testMaxNegativeValues() {
			assertEquals(10.0f, operator.apply(10.0f, -10.0f), 0.001f);
			assertEquals(-10.0f, operator.apply(-10.0f, -15.0f), 0.001f);
			assertEquals(5.0f, operator.apply(-5.0f, 5.0f), 0.001f);
		}

		@Test
		@DisplayName("should have order 4")
		void testOrder() {
			assertEquals(4, operator.order());
		}

		@Test
		@DisplayName("should return same instance (singleton)")
		void testSingleton() {
			assertSame(operator, MaxOperator.getInstance());
		}
	}

	@Nested
	@DisplayName("Operator Ordering")
	class OperatorOrderingTest {

		@Test
		@DisplayName("should have correct precedence order")
		void testOperatorPrecedence() {
			// Addition and Subtraction have order 1
			assertEquals(1, AdditionOperator.getInstance().order());
			assertEquals(1, SubtractionOperator.getInstance().order());

			// Multiplication has order 2
			assertEquals(2, MultiplicationOperator.getInstance().order());

			// Division has order 3
			assertEquals(3, DivisionOperator.getInstance().order());

			// Min and Max have order 4
			assertEquals(4, MinOperator.getInstance().order());
			assertEquals(4, MaxOperator.getInstance().order());
		}

		@Test
		@DisplayName("should verify relative precedence")
		void testRelativePrecedence() {
			// Addition/Subtraction should execute before Multiplication
			assertTrue(AdditionOperator.getInstance().order() < MultiplicationOperator.getInstance().order());
			assertTrue(SubtractionOperator.getInstance().order() < MultiplicationOperator.getInstance().order());

			// Multiplication should execute before Division
			assertTrue(MultiplicationOperator.getInstance().order() < DivisionOperator.getInstance().order());

			// Division should execute before Min/Max
			assertTrue(DivisionOperator.getInstance().order() < MinOperator.getInstance().order());
			assertTrue(DivisionOperator.getInstance().order() < MaxOperator.getInstance().order());
		}
	}
}
