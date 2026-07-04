package com.fdm.group.Etrm_Project_Prototype;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

// ─────────────────────────────────────────────────────────────────────────────
// TRADE TESTS: CrudeOilOptionTrade and CrudeOilFutureTrade
// ─────────────────────────────────────────────────────────────────────────────
@DisplayName("CrudeOilOptionTrade and CrudeOilFutureTrade Tests")
public class CrudeOilTradeTests {

    // ── Shared fixtures ───────────────────────────────────────────────────────
    private TradeInfo standardInfo;
    private CrudeOilOption callOption;
    private CrudeOilOption putOption;
    private CrudeOilFuture future;

    private static final LocalDate TRADE_DATE  = LocalDate.of(2026, 6, 11);
    private static final LocalDate EXPIRY      = LocalDate.of(2026, 9, 19);
    private static final LocalDate DELIVERY    = LocalDate.of(2026, 9, 19);
    private static final double    STRIKE      = 80.0;
    private static final double    CONTRACT    = 1_000.0;
    private static final double    PREMIUM     = 3.50;
    private static final double    ENTRY_PRICE = 78.50;

    @BeforeEach
    void setup() {
        standardInfo = TradeInfo.builder()
            .standardId("TRD-001")
            .tradeDate(TRADE_DATE)
            .settlementDate(TRADE_DATE.plusDays(2))
            .counterparty("Shell Trading")
            .book("CRUDE_DERIVATIVES")
            .build();

        callOption = CrudeOilOption.builder()
            .call()
            .strike(STRIKE)
            .expiryDate(EXPIRY)
            .contractSize(CONTRACT)
            .build();

        putOption = CrudeOilOption.builder()
            .put()
            .strike(STRIKE)
            .expiryDate(EXPIRY)
            .contractSize(CONTRACT)
            .build();

        future = CrudeOilFuture.builder()
            .deliveryDate(DELIVERY)
            .contractSize(CONTRACT)
            .cashSettled()
            .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OPTION TRADE TESTS
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilOptionTrade — TradeInfo")
    class OptionTradeInfo {

        @Test
        @DisplayName("getInfo() returns TradeInfo, not null — regression for null bug")
        void getInfo_returnsTradeInfo_notNull() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo)
                .product(callOption)
                .longPosition(10)
                .premiumPerUnit(PREMIUM)
                .build();

            assertNotNull(trade.getInfo());
            assertEquals("TRD-001", trade.getInfo().getStandardId().orElseThrow());
        }

        @Test
        @DisplayName("getInfo() returns the SAME TradeInfo passed to builder")
        void getInfo_sameInstance() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(5).premiumPerUnit(PREMIUM).build();
            
            System.out.println(trade.getInfo());
            System.out.println(standardInfo);
            
            assertSame(standardInfo, trade.getInfo());
        }

        @Test
        @DisplayName("counterparty and book are accessible via TradeInfo")
        void getInfo_counterpartyAndBook() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(5).premiumPerUnit(PREMIUM).build();
            assertEquals("Shell Trading",     trade.getInfo().getCounterparty().orElseThrow());
            assertEquals("CRUDE_DERIVATIVES", trade.getInfo().getBook().orElseThrow());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilOptionTrade — Builder Validation")
    class OptionBuilderValidation {

        @Test
        @DisplayName("Missing TradeInfo throws NullPointerException")
        void missingInfo_throws() {
            assertThrows(IllegalArgumentException.class, () ->
                CrudeOilOptionTrade.builder()
                    .product(callOption).longPosition(10).premiumPerUnit(PREMIUM)
                    .build()
            );
        }

        @Test
        @DisplayName("Missing product throws NullPointerException")
        void missingProduct_throws() {
            assertThrows(NullPointerException.class, () ->
                CrudeOilOptionTrade.builder()
                    .info(standardInfo).longPosition(10).premiumPerUnit(PREMIUM)
                    .build()
            );
        }

        @Test
        @DisplayName("Missing quantity throws NullPointerException")
        void missingQuantity_throws() {
            assertThrows(NullPointerException.class, () ->
                CrudeOilOptionTrade.builder()
                    .info(standardInfo).product(callOption).premiumPerUnit(PREMIUM)
                    .build()
            );
        }

        @Test
        @DisplayName("premiumPerUnit requires tradeDate in TradeInfo")
        void premiumPerUnit_requiresTradeDate() {
            TradeInfo infoWithoutDate = TradeInfo.builder().standardId("TRD-X").build();
            assertThrows(IllegalArgumentException.class, () ->
                CrudeOilOptionTrade.builder()
                    .info(infoWithoutDate).product(callOption).longPosition(1)
                    .premiumPerUnit(PREMIUM)
                    .build()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilOptionTrade — Premium Sign Convention")
    class OptionPremiumSign {

        @Test
        @DisplayName("Long position: getTotalPremium() is NEGATIVE (you pay)")
        void long_premium_isNegative() {
            // Regression test for the inverted sign bug found in code review
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(10).premiumPerUnit(PREMIUM).build();

            assertTrue(trade.getTotalPremium() > 0,
                "Long position should be positive, got: " + trade.getTotalPremium());
        }

        @Test
        @DisplayName("Short position: getTotalPremium() is POSITIVE (you receive)")
        void short_premium_isPositive() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).shortPosition(10).premiumPerUnit(PREMIUM).build();
            
            assertTrue(trade.getTotalPremium() < 0,
                "Short position should be negative, got: " + trade.getTotalPremium());
        }
        
        @Test
        @DisplayName("Premium magnitude: |lots| × contractSize × premiumPerUnit")
        void premium_magnitude() {
            // 10 lots × 1000 barrels × $3.50 = $35,000
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(10).premiumPerUnit(PREMIUM).build();

            assertEquals(35_000.0, Math.abs(trade.getTotalPremium()), 0.01);
        }

        @Test
        @DisplayName("Long and short of same size produce equal magnitude, opposite sign")
        void long_and_short_symmetry() {
            CrudeOilOptionTrade longTrade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(5).premiumPerUnit(PREMIUM).build();
            CrudeOilOptionTrade shortTrade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).shortPosition(5).premiumPerUnit(PREMIUM).build();
            System.out.println("Long Trade premium" +longTrade.getTotalPremium());
            System.out.println("Short Trade premium" + shortTrade.getTotalPremium());
            assertEquals(Math.abs(longTrade.getTotalPremium()), Math.abs(shortTrade.getTotalPremium()), 0.01);
            assertTrue(longTrade.getTotalPremium() > 0);
            assertTrue(shortTrade.getTotalPremium() < 0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilOptionTrade — Time to Expiry")
    class OptionTimeToExpiry {

        @Test
        @DisplayName("365 days to expiry = ~1.0 years, not 365.0 — regression for /365 bug")
        void timeToExpiry_inYears_not_rawDays() {
            CrudeOilOption option = CrudeOilOption.builder()
                .call().strike(80).expiryDate(LocalDate.now().plusDays(365)).build();
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(option).longPosition(1).premiumPerUnit(PREMIUM).build();

            assertEquals(1.0, trade.getTimetoExpiry(), 0.01,
                "365 days should return ~1.0 years, not 365.0");
        }

        @Test
        @DisplayName("90 days to expiry = ~0.25 years")
        void timeToExpiry_90days_quarterYear() {
            CrudeOilOption option = CrudeOilOption.builder()
                .call().strike(80).expiryDate(LocalDate.now().plusDays(90)).build();
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(option).longPosition(1).premiumPerUnit(PREMIUM).build();

            assertEquals(0.25, trade.getTimetoExpiry(), 0.01);
        }

        @Test
        @DisplayName("Already expired option returns 0.0, not negative")
        void timeToExpiry_expired_returnsZero() {
            CrudeOilOption option = CrudeOilOption.builder()
                .call().strike(80).expiryDate(LocalDate.now().minusDays(10)).build();
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(option).longPosition(1).premiumPerUnit(PREMIUM).build();

            assertEquals(0.0, trade.getTimetoExpiry(), 0.0,
                "Expired option should return 0.0, not negative");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilOptionTrade — Payoff Calculations")
    class OptionPayoff {

        @Test
        @DisplayName("Call ITM: payoff = (spot - strike) × size × lots")
        void call_payoff_inTheMoney() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(10).premiumPerUnit(PREMIUM).build();

            // spot=85, strike=80, 10 lots × 1000 barrels = (85-80)×1000×10 = 50,000
            double payoff = trade.payoffAt(85.0);
            assertEquals(50_000.0, payoff, 0.01);
        }

        @Test
        @DisplayName("Call OTM: payoff is zero")
        void call_payoff_outOfMoney() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(10).premiumPerUnit(PREMIUM).build();

            assertEquals(0.0, trade.payoffAt(75.0), 0.0);
        }

        @Test
        @DisplayName("Call ATM: payoff is zero")
        void call_payoff_atMoney() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(10).premiumPerUnit(PREMIUM).build();

            assertEquals(0.0, trade.payoffAt(STRIKE), 0.0);
        }

        @Test
        @DisplayName("Put ITM: payoff = (strike - spot) × size × lots")
        void put_payoff_inTheMoney() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(putOption).longPosition(10).premiumPerUnit(PREMIUM).build();

            // spot=70, strike=80: (80-70)×1000×10 = 100,000
            double payoff = trade.payoffAt(70.0);
            assertEquals(100_000.0, payoff, 0.01);
        }

        @Test
        @DisplayName("Put OTM: payoff is zero")
        void put_payoff_outOfMoney() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(putOption).longPosition(10).premiumPerUnit(PREMIUM).build();

            assertEquals(0.0, trade.payoffAt(90.0), 0.0);
        }

        @Test
        @DisplayName("Short call ITM: payoff is negative (you owe money)")
        void short_call_payoff_isNegative() {
            CrudeOilOptionTrade trade = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).shortPosition(10).premiumPerUnit(PREMIUM).build();

            assertTrue(trade.payoffAt(85.0) < 0,
                "Short call ITM should produce negative payoff (obligation to deliver)");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilOptionTrade — Amendment (toBuilder)")
    class OptionAmendment {

        @Test
        @DisplayName("toBuilder produces trade equal in all fields to original")
        void toBuilder_roundtrip() {
            CrudeOilOptionTrade original = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(10).premiumPerUnit(PREMIUM).build();

            CrudeOilOptionTrade copy = original.toBuilder().build();

            assertSame(original.getInfo(),    copy.getInfo());
            assertSame(original.getProduct(), copy.getProduct());
            assertEquals(original.getLots(),  copy.getLots(), 0.0);
        }

        @Test
        @DisplayName("toBuilder allows TradeInfo to be replaced for amendment")
        void toBuilder_infoReplacement() {
            CrudeOilOptionTrade original = CrudeOilOptionTrade.builder()
                .info(standardInfo).product(callOption).longPosition(10).premiumPerUnit(PREMIUM).build();

            TradeInfo amendedInfo = standardInfo.toBuilder()
                .settlementDate(TRADE_DATE.plusDays(4))
                .build();

            CrudeOilOptionTrade amended = original.toBuilder().info(amendedInfo).build();

            assertNotSame(original.getInfo(), amended.getInfo());
            assertEquals(TRADE_DATE.plusDays(4), amended.getInfo().getSettlementDate().orElseThrow());
            // product and quantity unchanged
            assertSame(original.getProduct(), amended.getProduct());
            assertEquals(original.getLots(),  amended.getLots(), 0.0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FUTURES TRADE TESTS
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilFutureTrade — TradeInfo and Basic Fields")
    class FutureTradeBasics {

        @Test
        @DisplayName("getInfo() returns TradeInfo, not null")
        void getInfo_notNull() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future)
                .longPosition(5).entryPrice(ENTRY_PRICE).build();

            assertNotNull(trade.getInfo());
            assertEquals("TRD-001", trade.getInfo().getStandardId().orElseThrow());
        }

        @Test
        @DisplayName("Entry price is stored from TradedPrice")
        void entryPrice_stored() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future)
                .longPosition(5).entryPrice(ENTRY_PRICE).build();

            assertEquals(ENTRY_PRICE, trade.getEntryPrice(), 1e-9);
        }

        @Test
        @DisplayName("Delivery date delegates to product")
        void deliveryDate_delegatesToProduct() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future)
                .longPosition(5).entryPrice(ENTRY_PRICE).build();

            assertEquals(DELIVERY, trade.getDeliveryDate());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilFutureTrade — Builder Validation")
    class FutureBuilderValidation {

        @Test
        @DisplayName("Missing TradeInfo throws IllegalStateException")
        void missingInfo_throws() {
            assertThrows(IllegalStateException.class, () ->
                CrudeOilFutureTrade.builder()
                    .product(future).longPosition(5).entryPrice(ENTRY_PRICE).build()
            );
        }

        @Test
        @DisplayName("entryPrice requires tradeDate in TradeInfo")
        void entryPrice_requiresTradeDate() {
            TradeInfo noDate = TradeInfo.builder().standardId("TRD-X").build();
            assertThrows(IllegalStateException.class, () ->
                CrudeOilFutureTrade.builder()
                    .info(noDate).product(future).longPosition(5).entryPrice(ENTRY_PRICE).build()
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilFutureTrade — Time to Delivery")
    class FutureTimeToDelivery {

        @Test
        @DisplayName("Time to delivery is in years, not raw days")
        void timeToDelivery_inYears() {
            CrudeOilFuture f = CrudeOilFuture.builder()
                .deliveryDate(LocalDate.now().plusDays(365)).build();
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(f).longPosition(5).entryPrice(ENTRY_PRICE).build();

            assertEquals(1.0, trade.getTimeToDelivery(), 0.01,
                "365 days should be ~1.0 years");
        }

        @Test
        @DisplayName("Already-delivered future returns 0.0, not negative")
        void timeToDelivery_past_returnsZero() {
            CrudeOilFuture f = CrudeOilFuture.builder()
                .deliveryDate(LocalDate.now().minusDays(5)).build();
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(f).longPosition(5).entryPrice(ENTRY_PRICE).build();

            assertEquals(0.0, trade.getTimeToDelivery(), 0.0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilFutureTrade — Mark-to-Market P&L")
    class FutureMtm {

        @Test
        @DisplayName("Long position profits when price rises")
        void long_profits_on_priceRise() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).longPosition(5).entryPrice(ENTRY_PRICE).build();

            // (80.00 - 78.50) × 1000 × 5 = +7500
            double pnl = trade.markToMarketPnl(80.00);
            assertEquals(7_500.0, pnl, 0.01);
        }

        @Test
        @DisplayName("Long position loses when price falls")
        void long_loses_on_priceFall() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).longPosition(5).entryPrice(ENTRY_PRICE).build();

            double pnl = trade.markToMarketPnl(75.00);
            assertTrue(pnl < 0, "Long position should lose when price falls below entry");
        }

        @Test
        @DisplayName("Short position profits when price falls")
        void short_profits_on_priceFall() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).shortPosition(5).entryPrice(ENTRY_PRICE).build();

            // (75.00 - 78.50) × 1000 × -5 = +17500
            double pnl = trade.markToMarketPnl(75.00);
            assertTrue(pnl > 0, "Short position profits when price falls below entry");
        }

        @Test
        @DisplayName("Zero P&L when current price equals entry price")
        void zeroPnl_atEntry() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).longPosition(5).entryPrice(ENTRY_PRICE).build();

            assertEquals(0.0, trade.markToMarketPnl(ENTRY_PRICE), 1e-9);
        }

        @Test
        @DisplayName("Long and short produce equal magnitude, opposite sign P&L")
        void long_short_pnl_symmetry() {
            CrudeOilFutureTrade longTrade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).longPosition(5).entryPrice(ENTRY_PRICE).build();
            CrudeOilFutureTrade shortTrade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).shortPosition(5).entryPrice(ENTRY_PRICE).build();

            double longPnl  = longTrade.markToMarketPnl(80.00);
            double shortPnl = shortTrade.markToMarketPnl(80.00);

            assertEquals(Math.abs(longPnl), Math.abs(shortPnl), 0.01);
            assertNotEquals(Math.signum(longPnl), Math.signum(shortPnl));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilFutureTrade — Variation Margin")
    class FutureVariationMargin {

        @Test
        @DisplayName("Long position: margin = (todayClose - prevClose) × size × lots")
        void long_variationMargin_priceRise() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).longPosition(5).entryPrice(ENTRY_PRICE).build();

            // (79.20 - 78.50) × 1000 × 5 = +3500
            double margin = trade.variationMargin(78.50, 79.20);
            assertEquals(3_500.0, margin, 0.01);
        }

        @Test
        @DisplayName("Short position pays margin when price rises")
        void short_variationMargin_priceRise() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).shortPosition(5).entryPrice(ENTRY_PRICE).build();

            double margin = trade.variationMargin(78.50, 79.20);
            assertTrue(margin < 0, "Short position should pay margin when price rises");
        }

        @Test
        @DisplayName("Zero margin when price is unchanged")
        void zeroMargin_unchanged() {
            CrudeOilFutureTrade trade = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).longPosition(5).entryPrice(ENTRY_PRICE).build();

            assertEquals(0.0, trade.variationMargin(78.50, 78.50), 1e-9);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CrudeOilFutureTrade — Amendment (toBuilder)")
    class FutureAmendment {

        @Test
        @DisplayName("toBuilder preserves all fields")
        void toBuilder_roundtrip() {
            CrudeOilFutureTrade original = CrudeOilFutureTrade.builder()
                .info(standardInfo).product(future).longPosition(5).entryPrice(ENTRY_PRICE).build();
            CrudeOilFutureTrade copy = original.toBuilder().build();

            assertSame(original.getInfo(),    copy.getInfo());
            assertSame(original.getProduct(), copy.getProduct());
            assertEquals(original.getLots(),  copy.getLots(), 0.0);
        }
    }
}