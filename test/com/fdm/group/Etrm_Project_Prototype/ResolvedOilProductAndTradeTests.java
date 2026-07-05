package com.fdm.group.Etrm_Project_Prototype;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

// ─────────────────────────────────────────────────────────────────────────────
// RESOLVED PRODUCT AND TRADE TESTS
// ─────────────────────────────────────────────────────────────────────────────
@DisplayName("ResolvedCrudeOilOption and ResolvedCrudeOilOptionTrade Tests")
public class ResolvedOilProductAndTradeTests {

    // ── Shared fixtures ───────────────────────────────────────────────────────
    private LocalDate valuationDate;
    private LocalDate expiryDate;
    private LocalDate settlementDate;
    private VolatilitySurface surface;
    private MarketDataProvider marketData;
    private TradeInfo tradeInfo;
    private CrudeOilOption callOption;
    private CrudeOilOption putOption;
    private CrudeOilOptionTrade longCallTrade;
    private CrudeOilOptionTrade shortCallTrade;
    private CrudeOilOptionTrade longPutTrade;

    private static final double SPOT      = 92.50;
    private static final double STRIKE    = 95.00;
    private static final double VOL_AT_95 = 0.28;
    private static final double RATE      = 0.053;
    private static final double LOTS      = 10.0;
    private static final double CONTRACT  = 1_000.0;
    private static final double PREMIUM   = 3.50;

    @BeforeEach
    void setup() {
        valuationDate  = LocalDate.of(2026, 6, 11);
        expiryDate     = LocalDate.of(2026, 9, 19);
        settlementDate = valuationDate.plusDays(2);

        surface = VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addVolatility(80.0,  expiryDate, 0.30)
            .addVolatility(90.0,  expiryDate, 0.26)
            .addVolatility(95.0,  expiryDate, VOL_AT_95)
            .addVolatility(100.0, expiryDate, 0.25)
            .addVolatility(110.0, expiryDate, 0.27)
            .build();

        marketData = MarketDataProvider.builder()
            .valuationDate(valuationDate)
            .addSpotPrice("CRUDE_OIL", SPOT)
            .addVolatilitySurface("CRUDE_OIL", surface)
            .riskFreeRate(RATE)          // new method added in this session
            .build();

        tradeInfo = TradeInfo.builder()
            .standardId("TRD-001")
            .tradeDate(valuationDate)
            .settlementDate(settlementDate)
            .counterparty("Shell Trading")
            .book("CRUDE_DERIVATIVES")
            .build();

        callOption = CrudeOilOption.builder()
            .call().strike(STRIKE).expiryDate(expiryDate).contractSize(CONTRACT).build();

        putOption = CrudeOilOption.builder()
            .put().strike(STRIKE).expiryDate(expiryDate).contractSize(CONTRACT).build();

        longCallTrade = CrudeOilOptionTrade.builder()
            .info(tradeInfo).product(callOption).longPosition(LOTS).premiumPerUnit(PREMIUM).build();

        shortCallTrade = CrudeOilOptionTrade.builder()
            .info(tradeInfo).product(callOption).shortPosition(LOTS).premiumPerUnit(PREMIUM).build();

        longPutTrade = CrudeOilOptionTrade.builder()
            .info(tradeInfo).product(putOption).longPosition(LOTS).premiumPerUnit(PREMIUM).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ResolvedCrudeOilOption — Field Population")
    class FieldPopulation {

        @Test
        @DisplayName("Strike is pulled from the product")
        void strike_fromProduct() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertEquals(STRIKE, resolved.getStrike(), 1e-9);
        }

        @Test
        @DisplayName("Spot price is pulled from MarketDataProvider")
        void spot_fromMarketData() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertEquals(SPOT, resolved.getSpot(), 1e-9);
        }

        @Test
        @DisplayName("Implied vol is looked up from VolatilitySurface at correct strike and expiry")
        void vol_fromSurface() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertEquals(VOL_AT_95, resolved.getImpliedVol(), 1e-9);
        }

        @Test
        @DisplayName("Risk-free rate is pulled from MarketDataProvider")
        void rate_fromMarketData() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertEquals(RATE, resolved.getRiskFreeRate(), 1e-9);
        }

        @Test
        @DisplayName("ScaledQuantity = lots × contractSize, signed")
        void scaledQuantity_lotsTimesSize() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertEquals(LOTS * CONTRACT, resolved.getScaledQuantity(), 1e-9);
        }

        @Test
        @DisplayName("ScaledQuantity is negative for a short position")
        void scaledQuantity_negativeForShort() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(shortCallTrade, marketData, valuationDate);
            assertTrue(resolved.getScaledQuantity() < 0,
                "Short position should have negative scaledQuantity");
        }

        @Test
        @DisplayName("TimeToExpiryYears returns time to expiry in years, not raw days")
        void timeToExpiry_inYears() {
            // valuationDate = 2026-06-11, expiryDate = 2026-09-19 = ~100 days
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            double tYears = resolved.getTimeToExpiryYears();
            assertTrue(tYears > 0.0 && tYears < 1.0,
                "~100 days should be a fraction of a year between 0 and 1, got: " + tYears);
        }

        @Test
        @DisplayName("SettlementDate is pulled from TradeInfo")
        void settlementDate_fromTradeInfo() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertEquals(settlementDate, resolved.getSettlementDate());
        }

        @Test
        @DisplayName("ExpiryDate is pulled from product")
        void expiryDate_fromProduct() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertEquals(expiryDate, resolved.getExpiryDate());
        }

        @Test
        @DisplayName("Currency is pulled from product")
        void currency_fromProduct() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertEquals("USD", resolved.getCurrency());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ResolvedCrudeOilOption — Discount Factor Integrity")
    class DiscountFactorIntegrity {

        @Test
        @DisplayName("discountFactor equals DiscountFactor.of(rate, time)")
        void discountFactor_matchesCentralizedFormula() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);

            double expected = DiscountFactor.of(resolved.getRiskFreeRate(), resolved.getTimeToExpiry());
            assertEquals(expected, resolved.getDiscountFactor(), 1e-10,
                "discountFactor must be computed by DiscountFactor.of(), not inline Math.exp()");
        }

        @Test
        @DisplayName("discountFactor is between 0 and 1 for positive rate")
        void discountFactor_inRange() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertTrue(resolved.getDiscountFactor() > 0.0 && resolved.getDiscountFactor() < 1.0,
                "discountFactor should be in (0,1) for positive rate and positive time");
        }

        @Test
        @DisplayName("discountFactor approaches 1.0 as time to expiry approaches 0")
        void discountFactor_nearExpiry_approachesOne() {
            // value at the expiry date itself
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, expiryDate);
            assertEquals(1.0, resolved.getDiscountFactor(), 0.001,
                "At expiry, discount factor should be ~1.0");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ResolvedCrudeOilOption — Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("Throws when TradeInfo has no settlementDate")
        void throws_noSettlementDate() {
            TradeInfo noSettlement = TradeInfo.builder()
                .standardId("TRD-X").tradeDate(valuationDate).build();  // no settlementDate
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(noSettlement).product(callOption).longPosition(1).premiumPerUnit(PREMIUM).build();

            assertThrows(IllegalStateException.class, () ->
                ResolvedCrudeOilOption.of(trade, marketData, valuationDate)
            );
        }

        @Test
        @DisplayName("Throws when MarketDataProvider has no risk-free rate")
        void throws_noRiskFreeRate() {
            MarketDataProvider noRate = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", SPOT)
                .addVolatilitySurface("CRUDE_OIL", surface)
                // no riskFreeRate
                .build();

            assertThrows(IllegalArgumentException.class, () ->
                ResolvedCrudeOilOption.of(longCallTrade, noRate, valuationDate)
            );
        }

        @Test
        @DisplayName("Throws when commodity has no spot price")
        void throws_noSpotPrice() {
            MarketDataProvider noSpot = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addVolatilitySurface("CRUDE_OIL", surface)
                .riskFreeRate(RATE)
                .build();

            assertThrows(IllegalArgumentException.class, () ->
                ResolvedCrudeOilOption.of(longCallTrade, noSpot, valuationDate)
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ResolvedCrudeOilOption — PutCall Type Preserved")
    class PutCallPreserved {

        @Test
        @DisplayName("Resolved call has PutCall.CALL")
        void call_putCallPreserved() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            assertEquals(CrudeOilOption.PutCall.CALL, resolved.getPutCall());
        }

        @Test
        @DisplayName("Resolved put has PutCall.PUT")
        void put_putCallPreserved() {
            ResolvedCrudeOilOption resolved = ResolvedCrudeOilOption.of(longPutTrade, marketData, valuationDate);
            assertEquals(CrudeOilOption.PutCall.PUT, resolved.getPutCall());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ResolvedCrudeOilOptionTrade — Carrier Tests")
    class ResolvedTradeCarrier {

        @Test
        @DisplayName("getInfo() returns same TradeInfo instance — carrier does no copying")
        void getInfo_sameInstance() {
            ResolvedCrudeOilOptionTrade resolved = ResolvedCrudeOilOptionTrade.of(
                longCallTrade, marketData, valuationDate
            );
            assertSame(tradeInfo, resolved.getInfo());
        }

        @Test
        @DisplayName("getProduct() returns the resolved option, not the unresolved one")
        void getProduct_returnsResolved() {
            ResolvedCrudeOilOptionTrade resolved = ResolvedCrudeOilOptionTrade.of(
                longCallTrade, marketData, valuationDate
            );
            assertNotNull(resolved.getProduct());
            assertInstanceOf(ResolvedCrudeOilOption.class, resolved.getProduct());
        }

        @Test
        @DisplayName("Resolved trade and directly-resolved product are consistent")
        void resolvedTrade_consistentWithDirectResolution() {
            ResolvedCrudeOilOptionTrade resolvedTrade = ResolvedCrudeOilOptionTrade.of(
                longCallTrade, marketData, valuationDate
            );
            ResolvedCrudeOilOption directProduct = ResolvedCrudeOilOption.of(
                longCallTrade, marketData, valuationDate
            );

            // They should produce the same field values since the same inputs are used
            assertEquals(directProduct.getStrike(),         resolvedTrade.getProduct().getStrike(), 1e-9);
            assertEquals(directProduct.getSpot(),           resolvedTrade.getProduct().getSpot(), 1e-9);
            assertEquals(directProduct.getImpliedVol(),     resolvedTrade.getProduct().getImpliedVol(), 1e-9);
            assertEquals(directProduct.getScaledQuantity(), resolvedTrade.getProduct().getScaledQuantity(), 1e-9);
            assertEquals(directProduct.getDiscountFactor(), resolvedTrade.getProduct().getDiscountFactor(), 1e-12);
        }

        @Test
        @DisplayName("Resolving same trade twice produces equivalent results")
        void resolve_twiceIsConsistent() {
            ResolvedCrudeOilOption r1 = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            ResolvedCrudeOilOption r2 = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);

            assertEquals(r1.getStrike(),         r2.getStrike(), 1e-9);
            assertEquals(r1.getSpot(),           r2.getSpot(), 1e-9);
            assertEquals(r1.getDiscountFactor(), r2.getDiscountFactor(), 1e-12);
        }

        @Test
        @DisplayName("Different valuation dates produce different timeToExpiry")
        void differentValuationDates_differentTimeToExpiry() {
            ResolvedCrudeOilOption early = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            ResolvedCrudeOilOption later = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate.plusMonths(1));

            assertTrue(early.getTimeToExpiry() > later.getTimeToExpiry(),
                "Earlier valuation date should have longer time to expiry");
        }

        @Test
        @DisplayName("Different valuation dates produce different discount factors")
        void differentValuationDates_differentDiscountFactors() {
            ResolvedCrudeOilOption early = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate);
            ResolvedCrudeOilOption later = ResolvedCrudeOilOption.of(longCallTrade, marketData, valuationDate.plusMonths(1));

            // Later valuation date = less time to discount = higher discount factor
            assertTrue(later.getDiscountFactor() > early.getDiscountFactor(),
                "Closer to expiry means larger discount factor (less discounting needed)");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("MarketDataProvider — Risk-Free Rate")
    class RiskFreeRateTests {

        @Test
        @DisplayName("getRiskFreeRate() returns the added rate")
        void getRiskFreeRate_returnsValue() {
            assertEquals(RATE, marketData.getRiskFreeRate(), 1e-9);
        }

        @Test
        @DisplayName("getRiskFreeRate(currency) returns rate for that currency")
        void getRiskFreeRate_byCurrency() {
            MarketDataProvider provider = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", SPOT)
                .addRiskFreeRate("USD", 0.05)
                .addRiskFreeRate("EUR", 0.03)
                .build();

            assertEquals(0.05, provider.getRiskFreeRate("USD"), 1e-9);
            assertEquals(0.03, provider.getRiskFreeRate("EUR"), 1e-9);
        }

        @Test
        @DisplayName("getRiskFreeRate() throws when no USD rate is present")
        void getRiskFreeRate_throws_whenMissing() {
            MarketDataProvider noRate = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", SPOT)
                .build();

            assertThrows(IllegalArgumentException.class, noRate::getRiskFreeRate);
        }

        @Test
        @DisplayName("toBuilder preserves risk-free rates")
        void toBuilder_preservesRates() {
            MarketDataProvider copy = marketData.toBuilder().build();
            assertEquals(RATE, copy.getRiskFreeRate(), 1e-9);
        }

        @Test
        @DisplayName("Stress scenario: shock spot price without changing rate")
        void stressScenario_spotShock() {
            MarketDataProvider shocked = marketData.toBuilder()
                .addSpotPrice("CRUDE_OIL", SPOT * 1.10)
                .build();

            assertEquals(SPOT * 1.10, shocked.getSpotPrice("CRUDE_OIL"), 0.01);
            // Rate should be unchanged
            assertEquals(RATE, shocked.getRiskFreeRate(), 1e-9);
        }
    }
}
