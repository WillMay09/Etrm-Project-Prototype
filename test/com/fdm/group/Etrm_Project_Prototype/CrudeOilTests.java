package com.fdm.group.Etrm_Project_Prototype;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fdm.group.Etrm_Project_Prototype.CrudeOilFuture.SettlementType;

// ─────────────────────────────────────────────────────────────────────────────
// PRODUCT TESTS: CrudeOilOption
// ─────────────────────────────────────────────────────────────────────────────
@DisplayName("CrudeOilOption and CrudeOilFuture Product Tests")
public class CrudeOilTests {

	// ── Shared fixtures ───────────────────────────────────────────────────────
	private static final LocalDate EXPIRY_3M = LocalDate.now().plusMonths(3);
	private static final LocalDate EXPIRY_6M = LocalDate.now().plusMonths(6);
	private static final LocalDate DELIVERY1M = LocalDate.now().plusMonths(1);
	private static final LocalDate DELIVERY2M = LocalDate.now().plusMonths(2);

	// ─────────────────────────────────────────────────────────────────────────
	@Nested
	@DisplayName("Builder Validation")
	class BuilderValidation {

		@Test
		@DisplayName("Zero strike throws IllegalArgumentException")
		void strike_zero_throws() {
			assertThrows(IllegalArgumentException.class,
					() -> CrudeOilOption.builder().call().expiryDate(EXPIRY_3M).strike(0.0).build());
		}

		@Test
		@DisplayName("Negative strike throws IllegalArgumentException")
		void strike_negative_throws() {
			assertThrows(IllegalArgumentException.class,
					() -> CrudeOilOption.builder().call().expiryDate(EXPIRY_3M).strike(-5.0).build());
		}

		@Test
		@DisplayName("Zero contract size throws IllegalArgumentException")
		void contractSize_zero_throws() {
			assertThrows(IllegalArgumentException.class,
					() -> CrudeOilOption.builder().call().strike(80.0).expiryDate(EXPIRY_3M).contractSize(0.0).build());
		}

		@Test
		@DisplayName("Missing putCall throws NullPointerException")
		void putCall_missing_throws() {
			assertThrows(NullPointerException.class, () -> CrudeOilOption.builder().strike(80.0).expiryDate(EXPIRY_3M)
					// no .call() or .put()
					.build());
		}

		@Test
		@DisplayName("Missing expiryDate throws NullPointerException")
		void expiryDate_missing_throws() {
			assertThrows(NullPointerException.class, () -> CrudeOilOption.builder().call().strike(80.0)
					// no expiryDate
					.build());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	@Nested
	@DisplayName("Default Values")
	class Defaults {

		@Test
		@DisplayName("Default contract size is 1000 barrels per lot")
		void default_contractSize_1000() {
			CrudeOilOption opt = CrudeOilOption.builder().call().strike(80.0).expiryDate(EXPIRY_3M).build();
			assertEquals(1_000.0, opt.getContractSize());
		}

		@Test
		@DisplayName("Default underlying is CRUDE_OIL")
		void default_underlying_crudeOil() {
			CrudeOilOption opt = CrudeOilOption.builder().call().strike(80.0).expiryDate(EXPIRY_3M).build();
			assertEquals("CRUDE_OIL", opt.getUnderlying());
		}

		@Test
		@DisplayName("Default currency is USD")
		void default_currency_usd() {
			CrudeOilOption opt = CrudeOilOption.builder().call().strike(80.0).expiryDate(EXPIRY_3M).build();
			assertEquals("USD", opt.getCurrency());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	@Nested
	@DisplayName("PutCall Logic")
	class PutCallLogic {

		@Test
		@DisplayName("isCall true and isPut false for a call option")
		void call_flags_correct() {
			CrudeOilOption opt = CrudeOilOption.builder().call().strike(80.0).expiryDate(EXPIRY_3M).build();
			assertTrue(opt.isCall());
			assertFalse(opt.isPut());
		}

		@Test
		@DisplayName("isPut true and isCall false for a put option")
		void put_flags_correct() {
			CrudeOilOption opt = CrudeOilOption.builder().put().strike(80.0).expiryDate(EXPIRY_3M).build();
			assertTrue(opt.isPut());
			assertFalse(opt.isCall());
		}

		@Test
		@DisplayName("isCall and isPut are mutually exclusive")
		void call_put_mutually_exclusive() {
			CrudeOilOption call = CrudeOilOption.builder().call().strike(80).expiryDate(EXPIRY_3M).build();
			CrudeOilOption put = CrudeOilOption.builder().put().strike(80).expiryDate(EXPIRY_3M).build();
			assertNotEquals(call.isCall(), put.isCall());
			assertNotEquals(call.isPut(), put.isPut());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	@Nested
	@DisplayName("Custom Values")
	class CustomValues {

		@Test
		@DisplayName("Custom contract size overrides default")
		void custom_contractSize() {
			CrudeOilOption opt = CrudeOilOption.builder().call().strike(80.0).expiryDate(EXPIRY_3M).contractSize(500.0)
					.build();
			assertEquals(500.0, opt.getContractSize());
		}

		@Test
		@DisplayName("Custom currency is stored")
		void custom_currency() {
			CrudeOilOption opt = CrudeOilOption.builder().call().strike(80.0).expiryDate(EXPIRY_6M).currency("EUR")
					.build();
			assertEquals("EUR", opt.getCurrency());
		}

		@Test
		@DisplayName("Strike is stored accurately")
		void strike_stored() {
			CrudeOilOption opt = CrudeOilOption.builder().call().strike(92.75).expiryDate(EXPIRY_3M).build();
			assertEquals(92.75, opt.getStrike(), 1e-9);
		}

		@Test
		@DisplayName("Expiry date is stored correctly")
		void expiry_stored() {
			CrudeOilOption opt = CrudeOilOption.builder().call().strike(80.0).expiryDate(EXPIRY_6M).build();
			assertEquals(EXPIRY_6M, opt.getExpiryDate());
		}
	}

	// ─────────────────────────────────────────────────────────────────────────
	@Nested
	@DisplayName("toString")
	class ToStringTests {

		@Test
		@DisplayName("toString contains key fields")
		void toString_containsKeyFields() {
			CrudeOilOption opt = CrudeOilOption.builder().call().strike(80.0).expiryDate(EXPIRY_3M).build();
			String s = opt.toString();
			assertTrue(s.contains("CRUDE_OIL"));
			assertTrue(s.contains("80"));
			assertTrue(s.contains("CALL"));
		}
	}




	// ─────────────────────────────────────────────────────────────────────────────
	// PRODUCT TESTS: CrudeOilFuture
	//
	// Tests the product class only — no TradeInfo, no entry price, no quantity.
	// Those concerns belong to CrudeOilFutureTrade and are tested separately.
	//
	// Mirrors the structure of CrudeOilTests.java (the option product tests)
	// so the two product test files are easy to compare side by side.
	// ─────────────────────────────────────────────────────────────────────────────
	@DisplayName("CrudeOilFuture Product Tests")
	public class CrudeOilFutureTests {
	 
	    private static final LocalDate DELIVERY_3M = LocalDate.now().plusMonths(3);
	    private static final LocalDate DELIVERY_6M = LocalDate.now().plusMonths(6);
	    private static final LocalDate DELIVERY_1Y = LocalDate.now().plusYears(1);
	 
	    // ─────────────────────────────────────────────────────────────────────────
	    @Nested
	    @DisplayName("Builder Validation")
	    class BuilderValidation {
	 
	        @Test
	        @DisplayName("Missing deliveryDate throws NullPointerException")
	        void deliveryDate_missing_throws() {
	            assertThrows(NullPointerException.class, () ->
	                CrudeOilFuture.builder()
	                    // no deliveryDate
	                    .build()
	            );
	        }
	 
	        @Test
	        @DisplayName("Zero contract size throws IllegalArgumentException")
	        void contractSize_zero_throws() {
	            assertThrows(IllegalArgumentException.class, () ->
	                CrudeOilFuture.builder()
	                    .deliveryDate(DELIVERY_3M)
	                    .contractSize(0.0)
	                    .build()
	            );
	        }
	 
	        @Test
	        @DisplayName("Negative contract size throws IllegalArgumentException")
	        void contractSize_negative_throws() {
	            assertThrows(IllegalArgumentException.class, () ->
	                CrudeOilFuture.builder()
	                    .deliveryDate(DELIVERY_3M)
	                    .contractSize(-500.0)
	                    .build()
	            );
	        }
	 
	        @Test
	        @DisplayName("Null underlying throws NullPointerException")
	        void underlying_null_throws() {
	            assertThrows(NullPointerException.class, () ->
	                CrudeOilFuture.builder()
	                    .underlying(null)
	                    .deliveryDate(DELIVERY_3M)
	                    .build()
	            );
	        }
	 
	        @Test
	        @DisplayName("Null currency throws NullPointerException")
	        void currency_null_throws() {
	            assertThrows(NullPointerException.class, () ->
	                CrudeOilFuture.builder()
	                    .deliveryDate(DELIVERY_3M)
	                    .currency(null)
	                    .build()
	            );
	        }
	 
	        @Test
	        @DisplayName("build() method is lowercase — regression for capital-B Build() bug")
	        void build_methodIsLowercase() {
	            // If Build() was still capital-B, calling .build() here would
	            // cause a compile error in the Builder. This test compiles only
	            // if the method is named build().
	            assertDoesNotThrow(() ->
	                CrudeOilFuture.builder()
	                    .deliveryDate(DELIVERY_3M)
	                    .build()
	            );
	        }
	    }
	 
	    // ─────────────────────────────────────────────────────────────────────────
	    @Nested
	    @DisplayName("Default Values")
	    class Defaults {
	 
	        @Test
	        @DisplayName("Default underlying is CRUDE_OIL")
	        void default_underlying_crudeOil() {
	            CrudeOilFuture f = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).build();
	            assertEquals("CRUDE_OIL", f.getUnderlying());
	        }
	 
	        @Test
	        @DisplayName("Default contract size is 1000 barrels per lot")
	        void default_contractSize_1000() {
	            CrudeOilFuture f = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).build();
	            assertEquals(1_000.0, f.getContractSize());
	        }
	 
	        @Test
	        @DisplayName("Default currency is USD")
	        void default_currency_usd() {
	            CrudeOilFuture f = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).build();
	            assertEquals("USD", f.getCurrency());
	        }
	 
	        @Test
	        @DisplayName("Default settlement type is CASH")
	        void default_settlementType_cash() {
	            CrudeOilFuture f = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).build();
	            assertEquals(CrudeOilFuture.SettlementType.CASH, f.getSettlementType());
	        }
	 
	        @Test
	        @DisplayName("Default is cash-settled: isCashSettled() returns true")
	        void default_isCashSettled_true() {
	            CrudeOilFuture f = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).build();
	            assertTrue(f.isCashSettled());
	        }
	    }
	 
	    // ─────────────────────────────────────────────────────────────────────────
	    @Nested
	    @DisplayName("Settlement Type Logic")
	    class SettlementTypeLogic {
	 
	        @Test
	        @DisplayName("cashSettled(): isCashSettled() is true")
	        void cashSettled_flag() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M)
	                .cashSettled()
	                .build();
	            assertTrue(f.isCashSettled());
	            assertEquals(CrudeOilFuture.SettlementType.CASH, f.getSettlementType());
	        }
	 
	        @Test
	        @DisplayName("physicalDelivery(): isCashSettled() is false")
	        void physicalDelivery_flag() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M)
	                .physicalDelivery()
	                .build();
	            assertFalse(f.isCashSettled());
	            assertEquals(CrudeOilFuture.SettlementType.PHYSICAL, f.getSettlementType());
	        }
	 
	        @Test
	        @DisplayName("CASH and PHYSICAL are mutually exclusive")
	        void cash_physical_mutuallyExclusive() {
	            CrudeOilFuture cash     = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).cashSettled().build();
	            CrudeOilFuture physical = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).physicalDelivery().build();
	 
	            assertNotEquals(cash.getSettlementType(), physical.getSettlementType());
	            assertTrue(cash.isCashSettled());
	            assertFalse(physical.isCashSettled());
	        }
	 
	        @Test
	        @DisplayName("physicalDelivery() overrides prior cashSettled() call in builder chain")
	        void physicalDelivery_overridesCash() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M)
	                .cashSettled()        // set first
	                .physicalDelivery()   // then override
	                .build();
	            assertFalse(f.isCashSettled(), "physicalDelivery() should override cashSettled()");
	        }
	 
	        @Test
	        @DisplayName("cashSettled() overrides prior physicalDelivery() call in builder chain")
	        void cashSettled_overridesPhysical() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M)
	                .physicalDelivery()   // set first
	                .cashSettled()        // then override
	                .build();
	            assertTrue(f.isCashSettled(), "cashSettled() should override physicalDelivery()");
	        }
	    }
	 
	    // ─────────────────────────────────────────────────────────────────────────
	    @Nested
	    @DisplayName("Custom Values")
	    class CustomValues {
	 
	        @Test
	        @DisplayName("Custom underlying is stored")
	        void custom_underlying() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .underlying("BRENT_CRUDE")
	                .deliveryDate(DELIVERY_3M)
	                .build();
	            assertEquals("BRENT_CRUDE", f.getUnderlying());
	        }
	 
	        @Test
	        @DisplayName("Custom contract size overrides default")
	        void custom_contractSize() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M)
	                .contractSize(500.0)
	                .build();
	            assertEquals(500.0, f.getContractSize());
	        }
	 
	        @Test
	        @DisplayName("Custom currency is stored")
	        void custom_currency() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_6M)
	                .currency("EUR")
	                .build();
	            assertEquals("EUR", f.getCurrency());
	        }
	 
	        @Test
	        @DisplayName("Delivery date is stored exactly")
	        void deliveryDate_stored() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_6M)
	                .build();
	            assertEquals(DELIVERY_6M, f.getDeliveryDate());
	        }
	 
	        @Test
	        @DisplayName("Delivery date 1 year out is stored correctly")
	        void deliveryDate_oneYear() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_1Y)
	                .build();
	            assertEquals(DELIVERY_1Y, f.getDeliveryDate());
	        }
	 
	        @Test
	        @DisplayName("Past delivery date is accepted — product makes no assertion about when")
	        void deliveryDate_pastIsAccepted() {
	            LocalDate pastDelivery = LocalDate.now().minusDays(30);
	            assertDoesNotThrow(() ->
	                CrudeOilFuture.builder()
	                    .deliveryDate(pastDelivery)
	                    .build()
	            );
	        }
	 
	        @Test
	        @DisplayName("Very large contract size is stored correctly")
	        void largeContractSize_stored() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M)
	                .contractSize(10_000.0)
	                .build();
	            assertEquals(10_000.0, f.getContractSize(), 1e-9);
	        }
	 
	        @Test
	        @DisplayName("Fractional contract size is stored correctly")
	        void fractionalContractSize_stored() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M)
	                .contractSize(0.5)
	                .build();
	            assertEquals(0.5, f.getContractSize(), 1e-9);
	        }
	    }
	 
	    // ─────────────────────────────────────────────────────────────────────────
	    @Nested
	    @DisplayName("Immutability — Product Cannot Be Changed After Construction")
	    class Immutability {
	 
	        @Test
	        @DisplayName("Building two futures from the same builder produces independent objects")
	        void twoBuildsFromSameBuilder_independent() {
	            CrudeOilFuture.Builder builder = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M)
	                .contractSize(1_000.0);
	 
	            CrudeOilFuture f1 = builder.build();
	            CrudeOilFuture f2 = builder.deliveryDate(DELIVERY_6M).build();
	 
	            // f1 should not be affected by the builder change after it was built
	            assertEquals(DELIVERY_3M, f1.getDeliveryDate(),
	                "f1's delivery date should be unchanged after the builder was modified");
	            assertEquals(DELIVERY_6M, f2.getDeliveryDate());
	        }
	 
	        @Test
	        @DisplayName("Different builders produce independent instances")
	        void differentBuilders_independentInstances() {
	            CrudeOilFuture cash     = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).cashSettled().build();
	            CrudeOilFuture physical = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).physicalDelivery().build();
	 
	            assertNotSame(cash, physical);
	            assertNotEquals(cash.getSettlementType(), physical.getSettlementType());
	        }
	    }
	 
	    // ─────────────────────────────────────────────────────────────────────────
	    @Nested
	    @DisplayName("Design Contract — Entry Price Does NOT Belong on the Product")
	    class DesignContract {
	 
	        @Test
	        @DisplayName("Product has no entry price field — it belongs on CrudeOilFutureTrade")
	        void noEntryPrice_onProduct() {
	            // This test documents the design decision explicitly.
	            // A WTI September 2026 future is the same product for all holders.
	            // Shell may have bought at $78.50, BP at $79.00 — entry price
	            // is a property of the TRADE, not the instrument.
	            //
	            // The product only defines: what commodity, when delivered, how settled.
	            CrudeOilFuture f = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).build();
	 
	            // These are the only things the product knows about:
	            assertNotNull(f.getUnderlying());
	            assertNotNull(f.getDeliveryDate());
	            assertNotNull(f.getCurrency());
	            assertNotNull(f.getSettlementType());
	            assertTrue(f.getContractSize() > 0);
	 
	            // There is intentionally no f.getEntryPrice() method.
	            // If you find yourself wanting to call f.getEntryPrice(), look at
	            // CrudeOilFutureTrade.getEntryPrice() instead, which delegates to TradedPrice.
	        }
	 
	        @Test
	        @DisplayName("Same product can be referenced by multiple trades at different entry prices")
	        void sameProduct_multipleTrades() {
	            // One product
	            CrudeOilFuture wtiSep2026 = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_6M)
	                .contractSize(1_000.0)
	                .cashSettled()
	                .build();
	 
	            TradeInfo shellInfo = TradeInfo.builder()
	                .standardId("TRD-SHELL-001")
	                .tradeDate(LocalDate.now())
	                .settlementDate(LocalDate.now().plusDays(2))
	                .build();
	 
	            TradeInfo bpInfo = TradeInfo.builder()
	                .standardId("TRD-BP-001")
	                .tradeDate(LocalDate.now())
	                .settlementDate(LocalDate.now().plusDays(2))
	                .build();
	 
	            // Same product, two trades at different entry prices
	            CrudeOilFutureTrade shellTrade = CrudeOilFutureTrade.builder()
	                .info(shellInfo)
	                .product(wtiSep2026)
	                .longPosition(10)
	                .entryPrice(78.50)    // Shell bought at 78.50
	                .build();
	 
	            CrudeOilFutureTrade bpTrade = CrudeOilFutureTrade.builder()
	                .info(bpInfo)
	                .product(wtiSep2026)
	                .longPosition(5)
	                .entryPrice(79.00)    // BP bought the same product at 79.00
	                .build();
	 
	            // Same product object referenced by both trades
	            assertSame(wtiSep2026, shellTrade.getProduct());
	            assertSame(wtiSep2026, bpTrade.getProduct());
	 
	            // Different entry prices on the trades — not on the product
	            assertEquals(78.50, shellTrade.getEntryPrice(), 1e-9);
	            assertEquals(79.00, bpTrade.getEntryPrice(), 1e-9);
	 
	            // Mark-to-market P&L differs because entry prices differ
	            double currentPrice = 80.00;
	            assertNotEquals(
	                shellTrade.markToMarketPnl(currentPrice),
	                bpTrade.markToMarketPnl(currentPrice)
	            );
	        }
	    }
	 
	    // ─────────────────────────────────────────────────────────────────────────
	    @Nested
	    @DisplayName("toString")
	    class ToStringTests {
	 
	        @Test
	        @DisplayName("toString contains underlying")
	        void toString_containsUnderlying() {
	            CrudeOilFuture f = CrudeOilFuture.builder().deliveryDate(DELIVERY_3M).build();
	            assertTrue(f.toString().contains("CRUDE_OIL"));
	        }
	 
	        @Test
	        @DisplayName("toString contains settlement type")
	        void toString_containsSettlementType() {
	            CrudeOilFuture cash = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M).cashSettled().build();
	            CrudeOilFuture physical = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M).physicalDelivery().build();
	 
	            assertTrue(cash.toString().contains("CASH"));
	            assertTrue(physical.toString().contains("PHYSICAL"));
	        }
	 
	        @Test
	        @DisplayName("toString contains contract size")
	        void toString_containsContractSize() {
	            CrudeOilFuture f = CrudeOilFuture.builder()
	                .deliveryDate(DELIVERY_3M).contractSize(2_000.0).build();
	            assertTrue(f.toString().contains("2000"));
	        }
	    }
	}
	 
}
