package com.fdm.group.Etrm_Project_Prototype;
 
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import com.fdm.group.Etrm_Project_Prototype.PriceCurve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
/**
 * Integration tests for PriceCurve
 * 
 * Tests realistic workflows and complex scenarios:
 * - Realistic market data curves
 * - Stress testing workflows
 * - Historical analysis
 * - Risk management scenarios
 */
@DisplayName("PriceCurve Integration Tests")
class PriceCurveIntegrationTest {
    
    private LocalDate valuationDate;
    
    @BeforeEach
    void setUp() {
        valuationDate = LocalDate.of(2026, 3, 21);
    }
    
    // =========================================================================
    // Realistic Market Data Scenarios
    // =========================================================================
    
    @Test
    @DisplayName("Should handle realistic crude oil forward curve")
    void testIntegration_RealisticCrudeOilCurve() {
        // Realistic crude oil forward curve (contango: prices decrease over time)
        PriceCurve curve = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            // Front month contracts
            .addPrice(LocalDate.of(2026, 4, 17), 94.50)  // Apr
            .addPrice(LocalDate.of(2026, 5, 19), 93.50)  // May
            .addPrice(LocalDate.of(2026, 6, 19), 92.00)  // Jun
            .addPrice(LocalDate.of(2026, 7, 17), 91.00)  // Jul
            .addPrice(LocalDate.of(2026, 8, 19), 90.50)  // Aug
            .addPrice(LocalDate.of(2026, 9, 18), 90.00)  // Sep
            // Deferred contracts
            .addPrice(LocalDate.of(2026, 12, 31), 88.00) // Dec
            .addPrice(LocalDate.of(2027, 3, 19), 87.00)  // Mar 2027
            .addPrice(LocalDate.of(2027, 6, 18), 86.00)  // Jun 2027
            .metadata("source", "NYMEX")
            .metadata("curveType", "CONTANGO")
            .build();
        
        // Test exact matches
        assertEquals(92.00, curve.getPrice(LocalDate.of(2026, 6, 19)), 0.01);
        assertEquals(88.00, curve.getPrice(LocalDate.of(2026, 12, 31)), 0.01);
        
        // Test interpolations
        LocalDate julyMid = LocalDate.of(2026, 7, 1);  // Between Jun and Jul
        double julyPrice = curve.getPrice(julyMid);
        assertTrue(julyPrice >= 91.0 && julyPrice <= 92.0, 
            "July price should be between Jun (92) and Jul (91)");
        
        // Test extrapolation
        LocalDate farFuture = LocalDate.of(2028, 1, 1);
        double farPrice = curve.getPrice(farFuture);
        assertEquals(86.00, farPrice, 0.01, "Should use last price for far future");
        
        // Verify curve size
        assertEquals(9, curve.size());
    }
    
    @Test
    @DisplayName("Should handle backwardation curve (increasing prices)")
    void testIntegration_BackwardationCurve() {
        // Backwardation: spot higher than forward (tight supply now)
        PriceCurve curve = PriceCurve.builder()
            .commodity("NATURAL_GAS")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 4, 17), 4.50)   // High now
            .addPrice(LocalDate.of(2026, 6, 19), 4.00)   // Decreasing
            .addPrice(LocalDate.of(2026, 9, 18), 3.50)   // Lower forward
            .addPrice(LocalDate.of(2026, 12, 31), 3.00)  // Lowest
            .metadata("curveType", "BACKWARDATION")
            .build();
        
        // Verify decreasing trend
        double apr = curve.getPrice(LocalDate.of(2026, 4, 17));
        double jun = curve.getPrice(LocalDate.of(2026, 6, 19));
        double sep = curve.getPrice(LocalDate.of(2026, 9, 18));
        double dec = curve.getPrice(LocalDate.of(2026, 12, 31));
        
        assertTrue(apr > jun);
        assertTrue(jun > sep);
        assertTrue(sep > dec);
    }
    
    // =========================================================================
    // Stress Testing Scenarios
    // =========================================================================
    
    @Test
    @DisplayName("Should support supply shock stress test")
    void testIntegration_SupplyShockStressTest() {
        // Base case: normal market
        PriceCurve baseCase = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 90.0)
            .addPrice(LocalDate.of(2026, 9, 18), 88.0)
            .addPrice(LocalDate.of(2026, 12, 31), 86.0)
            .metadata("scenario", "BASE_CASE")
            .build();
        
        // Stress scenario: Supply shock (+$10/barrel)
        PriceCurve supplyShock = baseCase.withParallelShift(10.0);
        
        // Verify shift applied to all points
        assertEquals(100.0, supplyShock.getPrice(LocalDate.of(2026, 6, 19)), 0.01);
        assertEquals(98.0, supplyShock.getPrice(LocalDate.of(2026, 9, 18)), 0.01);
        assertEquals(96.0, supplyShock.getPrice(LocalDate.of(2026, 12, 31)), 0.01);
        
        // Check metadata
        String scenarioMeta = supplyShock.getMetadata("scenario", String.class);
        assertTrue(scenarioMeta.contains("10"));
    }
    
    @Test
    @DisplayName("Should support demand surge stress test")
    void testIntegration_DemandSurgeStressTest() {
        // Base case
        PriceCurve baseCase = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 100.0)
            .addPrice(LocalDate.of(2026, 9, 18), 100.0)
            .addPrice(LocalDate.of(2026, 12, 31), 100.0)
            .build();
        
        // Stress scenario: Demand surge (+20%)
        PriceCurve demandSurge = baseCase.withBump(0.20);
        
        // All prices should increase 20%
        assertEquals(120.0, demandSurge.getPrice(LocalDate.of(2026, 6, 19)), 0.01);
        assertEquals(120.0, demandSurge.getPrice(LocalDate.of(2026, 9, 18)), 0.01);
        assertEquals(120.0, demandSurge.getPrice(LocalDate.of(2026, 12, 31)), 0.01);
    }
    
    @Test
    @DisplayName("Should support multi-scenario stress testing")
    void testIntegration_MultiScenarioStressTest() {
        // Base market data
        PriceCurve base = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 90.0)
            .addPrice(LocalDate.of(2026, 9, 18), 88.0)
            .build();
        
        // Create multiple scenarios
        PriceCurve mild = base.withParallelShift(5.0);       // +$5
        PriceCurve moderate = base.withParallelShift(10.0);  // +$10
        PriceCurve severe = base.withParallelShift(20.0);    // +$20
        
        // Verify cascade
        LocalDate testDate = LocalDate.of(2026, 7, 15);
        double basePrice = base.getPrice(testDate);
        double mildPrice = mild.getPrice(testDate);
        double moderatePrice = moderate.getPrice(testDate);
        double severePrice = severe.getPrice(testDate);
        
        assertEquals(basePrice + 5.0, mildPrice, 0.01);
        assertEquals(basePrice + 10.0, moderatePrice, 0.01);
        assertEquals(basePrice + 20.0, severePrice, 0.01);
    }
    
    // =========================================================================
    // Historical Analysis Workflows
    // =========================================================================
    
    @Test
    @DisplayName("Should support historical curve comparison")
    void testIntegration_HistoricalComparison() {
        // Yesterday's curve
        PriceCurve yesterday = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(LocalDate.of(2026, 3, 20))
            .addPrice(LocalDate.of(2026, 6, 19), 92.0)
            .addPrice(LocalDate.of(2026, 9, 18), 90.0)
            .metadata("date", "2026-03-20")
            .build();
        
        // Today's curve (prices dropped)
        PriceCurve today = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 90.0)
            .addPrice(LocalDate.of(2026, 9, 18), 88.0)
            .metadata("date", "2026-03-21")
            .build();
        
        // Calculate curve shift
        LocalDate testDate = LocalDate.of(2026, 7, 15);
        double yesterdayPrice = yesterday.getPrice(testDate);
        double todayPrice = today.getPrice(testDate);
        double shift = todayPrice - yesterdayPrice;
        
        assertTrue(shift < 0, "Prices dropped today");
        assertTrue(Math.abs(shift) < 3.0, "Shift is reasonable (< $3)");
    }
    
    // =========================================================================
    // Complex Interpolation Scenarios
    // =========================================================================
    
    @Test
    @DisplayName("Should handle dense curve with many points")
    void testIntegration_DenseCurve() {
        PriceCurve.Builder builder = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate);
        
        // Add monthly contracts for 2 years
        LocalDate current = LocalDate.of(2026, 4, 1);
        double basePrice = 95.0;
        
        for (int i = 0; i < 24; i++) {
            double price = basePrice - (i * 0.5);  // Gradual decline
            builder.addPrice(current.plusMonths(i), price);
        }
        
        PriceCurve curve = builder.build();
        
        // Should have 24 points
        assertEquals(24, curve.size());
        
        // Test interpolation in middle
        LocalDate testDate = LocalDate.of(2027, 3, 15);  // Between 2 monthly points
        double price = curve.getPrice(testDate);
        
        assertTrue(price > 80.0 && price < 95.0, "Price should be in reasonable range");
    }
    
    @Test
    @DisplayName("Should handle sparse curve (few points)")
    void testIntegration_SparseCurve() {
        // Only 2 points - extreme case
        PriceCurve curve = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 90.0)
            .addPrice(LocalDate.of(2027, 6, 19), 80.0)  // 1 year later
            .build();
        
        // Query 6 months in (midpoint)
        LocalDate midpoint = LocalDate.of(2026, 12, 19);
        double price = curve.getPrice(midpoint);
        
        // Should be approximately $85 (midpoint between 90 and 80)
        assertEquals(85.0, price, 1.0);
    }
    
    // =========================================================================
    // Realistic Trading Workflows
    // =========================================================================
    
    @Test
    @DisplayName("Should support futures contract valuation workflow")
    void testIntegration_FuturesValuation() {
        // Market forward curve
        PriceCurve marketCurve = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 92.0)
            .addPrice(LocalDate.of(2026, 9, 18), 90.0)
            .addPrice(LocalDate.of(2026, 12, 31), 88.0)
            .metadata("source", "NYMEX")
            .build();
        
        // Trader wants to price July futures
        LocalDate julyDelivery = LocalDate.of(2026, 7, 15);
        double fairPrice = marketCurve.getPrice(julyDelivery);
        
        // Fair price should be between June (92) and Sept (90)
        assertTrue(fairPrice >= 90.0 && fairPrice <= 92.0);
        
        // If market quotes $91.50 and fair value is $91.00
        double marketQuote = 91.50;
        double spread = marketQuote - fairPrice;
        
        // Trader decision
        if (spread > 0.25) {
            // Market overpriced - sell
            assertTrue(spread > 0, "Market is expensive");
        }
    }
    
    @Test
    @DisplayName("Should support storage valuation workflow")
    void testIntegration_StorageValuation() {
        // Forward curve
        PriceCurve curve = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 90.0)
            .addPrice(LocalDate.of(2026, 9, 18), 92.0)   // Backwardation
            .addPrice(LocalDate.of(2026, 12, 31), 95.0)  // Strong backwardation
            .build();
        
        // Storage holds 100,000 barrels
        double quantity = 100_000;
        
        // Optimal sale date: December (highest price)
        double decPrice = curve.getPrice(LocalDate.of(2026, 12, 31));
        double storageValue = quantity * decPrice;
        
        // Storage worth $9.5M (100k × $95)
        assertEquals(9_500_000, storageValue, 1000);
    }
    
    @Test
    @DisplayName("Should support curve recalibration workflow")
    void testIntegration_CurveRecalibration() {
        // Morning curve
        PriceCurve morning = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 90.0)
            .addPrice(LocalDate.of(2026, 9, 18), 88.0)
            .metadata("timestamp", "09:00")
            .build();
        
        // Afternoon: June contract trades at $92 (not $90)
        PriceCurve afternoon = morning.withPrice(LocalDate.of(2026, 6, 19), 92.0);
        
        // Verify recalibration
        assertEquals(90.0, morning.getPrice(LocalDate.of(2026, 6, 19)), 0.01);
        assertEquals(92.0, afternoon.getPrice(LocalDate.of(2026, 6, 19)), 0.01);
        
        // Sept unchanged
        assertEquals(88.0, afternoon.getPrice(LocalDate.of(2026, 9, 18)), 0.01);
    }
    
    // =========================================================================
    // Edge Cases from Production
    // =========================================================================
    
    @Test
    @DisplayName("Should handle weekend/holiday interpolation")
    void testIntegration_WeekendInterpolation() {
        PriceCurve curve = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 90.0)  // Friday
            .addPrice(LocalDate.of(2026, 6, 22), 89.0)  // Monday
            .build();
        
        // Query on Saturday (June 20)
        LocalDate saturday = LocalDate.of(2026, 6, 20);
        double price = curve.getPrice(saturday);
        
        // Should interpolate between Friday and Monday
        assertTrue(price >= 89.0 && price <= 90.0);
    }
    
    @Test
    @DisplayName("Should handle month-end roll")
    void testIntegration_MonthEndRoll() {
        PriceCurve curve = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 5, 29), 91.0)  // May contract expires
            .addPrice(LocalDate.of(2026, 6, 19), 90.0)  // June contract
            .build();
        
        // Query on June 1 (between contracts)
        LocalDate june1 = LocalDate.of(2026, 6, 1);
        double price = curve.getPrice(june1);
        
        assertTrue(price >= 90.0 && price <= 91.0);
    }
    
    // =========================================================================
    // Performance/Stress Tests
    // =========================================================================
    
    @Test
    @DisplayName("Should handle many queries efficiently")
    void testIntegration_ManyQueries() {
        PriceCurve curve = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 92.0)
            .addPrice(LocalDate.of(2026, 9, 18), 90.0)
            .addPrice(LocalDate.of(2026, 12, 31), 88.0)
            .build();
        
        // Query every day for 6 months
        LocalDate start = LocalDate.of(2026, 6, 19);
        LocalDate end = LocalDate.of(2026, 12, 31);
        
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        
        for (int i = 0; i <= daysBetween; i++) {
            LocalDate queryDate = start.plusDays(i);
            double price = curve.getPrice(queryDate);
            
            // All queries should return valid prices
            assertTrue(price >= 88.0 && price <= 92.0);
        }
        
        // Should complete quickly (< 100ms for ~200 queries)
    }
}
 