package com.fdm.group.Etrm_Project_Prototype;
 
import static org.junit.jupiter.api.Assertions.*;
 
import java.time.LocalDate;
import java.util.OptionalDouble;
import java.util.Set;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
 
/**
 * Comprehensive unit tests for MarketDataProvider
 * 
 * Tests cover:
 * - Builder validation
 * - Spot prices
 * - Forward curves
 * - Volatility surfaces
 * - Historical data (TimeSeries)
 * - Metadata
 * - Scenario creation (toBuilder)
 * - Multi-commodity support
 * - Immutability
 */
@DisplayName("MarketDataProvider Unit Tests")
class MarketDataProviderTest {
    
    private LocalDate valuationDate;
    private PriceCurve crudeCurve;
    private VolatilitySurface crudeVol;
    private TimeSeries crudeHistory;
    
    @BeforeEach
    void setUp() {
        valuationDate = LocalDate.of(2026, 3, 21);
        
        // Setup sample forward curve
        crudeCurve = PriceCurve.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            .addPrice(LocalDate.of(2026, 6, 19), 92.0)
            .addPrice(LocalDate.of(2026, 9, 18), 90.0)
            .build();
        
        // Setup sample volatility surface (requires 4+ points for bilinear interpolation)
        crudeVol = VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(valuationDate)
            // 2x2 grid: 2 strikes × 2 expiries = 4 points minimum
            .addVolatility(85.0, LocalDate.of(2026, 6, 19), 0.27)  // OTM put, near-term
            .addVolatility(95.0, LocalDate.of(2026, 6, 19), 0.26)  // OTM call, near-term
            .addVolatility(85.0, LocalDate.of(2026, 9, 18), 0.25)  // OTM put, far-term
            .addVolatility(95.0, LocalDate.of(2026, 9, 18), 0.24)  // OTM call, far-term
            .build();
        
        // Setup sample historical data
        crudeHistory = TimeSeries.builder()
            .commodity("CRUDE_OIL")
            .addPoint(LocalDate.of(2026, 3, 1), 88.0)
            .addPoint(LocalDate.of(2026, 3, 15), 90.0)
            .addPoint(LocalDate.of(2026, 3, 20), 91.0)
            .createTimeSeries();
    }
    
    // =========================================================================
    // Builder Validation Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Builder Validation Tests")
    class BuilderValidationTests {
        
        @Test
        @DisplayName("Should throw if valuation date is null")
        void testBuild_NullValuationDate() {
            assertThrows(IllegalStateException.class, () -> {
                MarketDataProvider.builder()
                    // .valuationDate(valuationDate) ← Missing!
                    .addSpotPrice("CRUDE_OIL", 91.5)
                    .build();
            }, "Should throw IllegalStateException for null valuation date");
        }
        
        @Test
        @DisplayName("Should throw if no market data provided")
        void testBuild_NoMarketData() {
            assertThrows(IllegalStateException.class, () -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    // No data!
                    .build();
            }, "Should throw IllegalStateException for no market data");
        }
        
        @Test
        @DisplayName("Should accept spot price only")
        void testBuild_SpotPriceOnly() {
            assertDoesNotThrow(() -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addSpotPrice("CRUDE_OIL", 91.5)
                    .build();
            }, "Should accept spot price only");
        }
        
        @Test
        @DisplayName("Should accept forward curve only")
        void testBuild_ForwardCurveOnly() {
            assertDoesNotThrow(() -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addForwardCurve("CRUDE_OIL", crudeCurve)
                    .build();
            }, "Should accept forward curve only");
        }
        
        @Test
        @DisplayName("Should accept volatility surface only")
        void testBuild_VolatilitySurfaceOnly() {
            assertDoesNotThrow(() -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addVolatilitySurface("CRUDE_OIL", crudeVol)
                    .build();
            }, "Should accept volatility surface only");
        }
        
        @Test
        @DisplayName("Should accept historical data only")
        void testBuild_HistoricalDataOnly() {
            assertDoesNotThrow(() -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addHistoricalData("CRUDE_OIL", crudeHistory)
                    .build();
            }, "Should accept historical data only");
        }
        
        @Test
        @DisplayName("Should throw if spot price is negative")
        void testAddSpotPrice_Negative() {
            assertThrows(IllegalArgumentException.class, () -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addSpotPrice("CRUDE_OIL", -91.5);  // Negative!
            }, "Should throw IllegalArgumentException for negative spot price");
        }
        
        @Test
        @DisplayName("Should throw if commodity is null")
        void testAddSpotPrice_NullCommodity() {
            assertThrows(IllegalArgumentException.class, () -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addSpotPrice(null, 91.5);  // null commodity!
            }, "Should throw IllegalArgumentException for null commodity");
        }
        
        @Test
        @DisplayName("Should throw if forward curve is null")
        void testAddForwardCurve_Null() {
            assertThrows(NullPointerException.class, () -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addForwardCurve("CRUDE_OIL", null);  // null curve!
            }, "Should throw NullPointerException for null curve");
        }
        
        @Test
        @DisplayName("Should throw if volatility surface is null")
        void testAddVolatilitySurface_Null() {
            assertThrows(NullPointerException.class, () -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addVolatilitySurface("CRUDE_OIL", null);  // null surface!
            }, "Should throw NullPointerException for null surface");
        }
        
        @Test
        @DisplayName("Should throw if time series is null")
        void testAddHistoricalData_Null() {
            assertThrows(NullPointerException.class, () -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addHistoricalData("CRUDE_OIL", null);  // null time series!
            }, "Should throw NullPointerException for null time series");
        }
    }
    
    // =========================================================================
    // Spot Price Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Spot Price Tests")
    class SpotPriceTests {
        
        @Test
        @DisplayName("Should store and retrieve spot price")
        void testSpotPrice_StoreAndRetrieve() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.50)
                .build();
            
            assertEquals(91.50, marketData.getSpotPrice("CRUDE_OIL"), 0.0001);
        }
        
        @Test
        @DisplayName("Should throw if spot price not found")
        void testSpotPrice_NotFound() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.50)
                .build();
            
            assertThrows(IllegalArgumentException.class, () -> {
                marketData.getSpotPrice("NATURAL_GAS");  // Not added!
            }, "Should throw IllegalArgumentException for missing commodity");
        }
        
        @Test
        @DisplayName("Should accept zero spot price")
        void testSpotPrice_Zero() {
            assertDoesNotThrow(() -> {
                MarketDataProvider.builder()
                    .valuationDate(valuationDate)
                    .addSpotPrice("CRUDE_OIL", 0.0)
                    .build();
            }, "Should accept zero spot price");
        }
        
        @Test
        @DisplayName("Should overwrite spot price for same commodity")
        void testSpotPrice_Overwrite() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 90.0)
                .addSpotPrice("CRUDE_OIL", 92.0)  // Overwrite
                .build();
            
            assertEquals(92.0, marketData.getSpotPrice("CRUDE_OIL"), 0.0001);
        }
        
        @Test
        @DisplayName("Should check if spot price exists")
        void testHasSpotPrice() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .build();
            
            assertTrue(marketData.hasSpotPrice("CRUDE_OIL"));
            assertFalse(marketData.hasSpotPrice("NATURAL_GAS"));
        }
    }
    
    // =========================================================================
    // Forward Curve Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Forward Curve Tests")
    class ForwardCurveTests {
        
        @Test
        @DisplayName("Should store and retrieve forward curve")
        void testForwardCurve_StoreAndRetrieve() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .build();
            
            PriceCurve retrieved = marketData.getForwardCurve("CRUDE_OIL");
            
            assertNotNull(retrieved);
            assertEquals("CRUDE_OIL", retrieved.getCommodity());
            assertEquals(2, retrieved.size());
        }
        
        @Test
        @DisplayName("Should throw if forward curve not found")
        void testForwardCurve_NotFound() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .build();
            
            assertThrows(IllegalArgumentException.class, () -> {
                marketData.getForwardCurve("NATURAL_GAS");  // Not added!
            }, "Should throw IllegalArgumentException for missing curve");
        }
        
        @Test
        @DisplayName("Should get forward price from curve")
        void testGetForwardPrice() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .build();
            
            double june = marketData.getForwardPrice("CRUDE_OIL", LocalDate.of(2026, 6, 19));
            double sept = marketData.getForwardPrice("CRUDE_OIL", LocalDate.of(2026, 9, 18));
            
            assertEquals(92.0, june, 0.0001);
            assertEquals(90.0, sept, 0.0001);
        }
        
        @Test
        @DisplayName("Should check if forward curve exists")
        void testHasForwardCurve() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .build();
            
            assertTrue(marketData.hasForwardCurve("CRUDE_OIL"));
            assertFalse(marketData.hasForwardCurve("NATURAL_GAS"));
        }
    }
    
    // =========================================================================
    // Volatility Surface Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Volatility Surface Tests")
    class VolatilitySurfaceTests {
        
        @Test
        @DisplayName("Should store and retrieve volatility surface")
        void testVolatilitySurface_StoreAndRetrieve() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addVolatilitySurface("CRUDE_OIL", crudeVol)
                .build();
            
            VolatilitySurface retrieved = marketData.getVolatilitySurface("CRUDE_OIL");
            
            assertNotNull(retrieved);
            assertEquals("CRUDE_OIL", retrieved.getCommodity());
            assertEquals(4, retrieved.size());  // 4 points for bilinear interpolation
        }
        
        @Test
        @DisplayName("Should throw if volatility surface not found")
        void testVolatilitySurface_NotFound() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addVolatilitySurface("CRUDE_OIL", crudeVol)
                .build();
            
            assertThrows(IllegalArgumentException.class, () -> {
                marketData.getVolatilitySurface("NATURAL_GAS");  // Not added!
            }, "Should throw IllegalArgumentException for missing surface");
        }
        
        @Test
        @DisplayName("Should get volatility from surface - exact match")
        void testGetVolatility_ExactMatch() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addVolatilitySurface("CRUDE_OIL", crudeVol)
                .build();
            
            // Exact matches from our 2x2 grid
            double vol1 = marketData.getVolatility("CRUDE_OIL", LocalDate.of(2026, 6, 19), 85.0);
            double vol2 = marketData.getVolatility("CRUDE_OIL", LocalDate.of(2026, 6, 19), 95.0);
            double vol3 = marketData.getVolatility("CRUDE_OIL", LocalDate.of(2026, 9, 18), 85.0);
            double vol4 = marketData.getVolatility("CRUDE_OIL", LocalDate.of(2026, 9, 18), 95.0);
            
            assertEquals(0.27, vol1, 0.0001);  // OTM put, near
            assertEquals(0.26, vol2, 0.0001);  // OTM call, near
            assertEquals(0.25, vol3, 0.0001);  // OTM put, far
            assertEquals(0.24, vol4, 0.0001);  // OTM call, far
        }
        
        @Test
        @DisplayName("Should interpolate volatility between points")
        void testGetVolatility_Interpolation() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addVolatilitySurface("CRUDE_OIL", crudeVol)
                .build();
            
            // Interpolate between strikes and expiries
            // ATM strike (90.0) between near-term and far-term
            double atmVol = marketData.getVolatility("CRUDE_OIL", LocalDate.of(2026, 6, 19), 90.0);
            
            // Should be between 0.24 and 0.27 (ATM between OTM put and call)
            assertTrue(atmVol >= 0.24 && atmVol <= 0.27);
        }
        
        @Test
        @DisplayName("Should check if volatility surface exists")
        void testHasVolatilitySurface() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addVolatilitySurface("CRUDE_OIL", crudeVol)
                .build();
            
            assertTrue(marketData.hasVolatilitySurface("CRUDE_OIL"));
            assertFalse(marketData.hasVolatilitySurface("NATURAL_GAS"));
        }
    }
    
    // =========================================================================
    // Historical Data Tests (TimeSeries)
    // =========================================================================
    
    @Nested
    @DisplayName("Historical Data Tests")
    class HistoricalDataTests {
        
        @Test
        @DisplayName("Should store and retrieve historical data")
        void testHistoricalData_StoreAndRetrieve() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            TimeSeries retrieved = marketData.getHistoricalData("CRUDE_OIL");
            
            assertNotNull(retrieved);
            assertEquals(3, retrieved.size());
        }
        
        @Test
        @DisplayName("Should throw if historical data not found")
        void testHistoricalData_NotFound() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            assertThrows(IllegalArgumentException.class, () -> {
                marketData.getHistoricalData("NATURAL_GAS");  // Not added!
            }, "Should throw IllegalArgumentException for missing data");
        }
        
        @Test
        @DisplayName("Should check if historical data exists")
        void testHasHistoricalData() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            assertTrue(marketData.hasHistoricalData("CRUDE_OIL"));
            assertFalse(marketData.hasHistoricalData("NATURAL_GAS"));
        }
        
        @Test
        @DisplayName("Should retrieve specific historical price")
        void testGetHistoricalPrice() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            OptionalDouble price = marketData.getHistoricalPrice("CRUDE_OIL", LocalDate.of(2026, 3, 15));
            
            assertTrue(price.isPresent(), "Price should be present for this date");
            assertEquals(90.0, price.getAsDouble(), 0.0001);
        }
        
        @Test
        @DisplayName("Should return empty when historical price not found for date")
        void testGetHistoricalPrice_DateNotFound() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            OptionalDouble price = marketData.getHistoricalPrice("CRUDE_OIL", LocalDate.of(2026, 3, 10));
            
            assertFalse(price.isPresent(), "Price should not be present for missing date");
        }
        
        @Test
        @DisplayName("Should throw when commodity has no historical data")
        void testGetHistoricalPrice_CommodityNotFound() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            assertThrows(IllegalArgumentException.class, () -> {
                marketData.getHistoricalPrice("NATURAL_GAS", LocalDate.of(2026, 3, 15));
            }, "Should throw when commodity has no historical data");
        }
        
        @Test
        @DisplayName("Should get earliest and latest historical dates")
        void testHistoricalData_EarliestLatest() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            TimeSeries history = marketData.getHistoricalData("CRUDE_OIL");
            
            assertEquals(LocalDate.of(2026, 3, 1), history.getEarliestDate());
            assertEquals(LocalDate.of(2026, 3, 20), history.getLatestDate());
        }
        
        @Test
        @DisplayName("Should calculate statistics from historical data")
        void testHistoricalData_Statistics() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            TimeSeries history = marketData.getHistoricalData("CRUDE_OIL");
            
            // Mean: (88 + 90 + 91) / 3 = 89.67
            assertEquals(89.67, history.meanValue(), 0.01);
            
            // Min and max
            assertEquals(88.0, history.minValue(), 0.01);
            assertEquals(91.0, history.maxValue(), 0.01);
        }
    }
    
    // =========================================================================
    // Metadata Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Metadata Tests")
    class MetadataTests {
        
        @Test
        @DisplayName("Should store and retrieve metadata")
        void testMetadata_StoreAndRetrieve() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .metadata("source", "Bloomberg")
                .metadata("timestamp", "2026-03-21T09:30:00")
                .build();
            
            assertEquals("Bloomberg", marketData.getMetadata("source", String.class));
            assertEquals("2026-03-21T09:30:00", marketData.getMetadata("timestamp", String.class));
        }
        
        @Test
        @DisplayName("Should return null for missing metadata")
        void testMetadata_Missing() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .build();
            
            assertNull(marketData.getMetadata("nonexistent", String.class));
        }
        
        @Test
        @DisplayName("Should throw if metadata type is wrong")
        void testMetadata_WrongType() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .metadata("source", "Bloomberg")  // String
                .build();
            
            assertThrows(ClassCastException.class, () -> {
                marketData.getMetadata("source", Integer.class);  // Request as Integer!
            }, "Should throw ClassCastException for wrong type");
        }
        
        @Test
        @DisplayName("Should get all metadata")
        void testGetAllMetadata() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .metadata("source", "Bloomberg")
                .metadata("version", "1.0")
                .build();
            
            var allMetadata = marketData.getAllMetadata();
            
            assertEquals(2, allMetadata.size());
            assertTrue(allMetadata.containsKey("source"));
            assertTrue(allMetadata.containsKey("version"));
        }
    }
    
    // =========================================================================
    // Multi-Commodity Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Multi-Commodity Tests")
    class MultiCommodityTests {
        
        @Test
        @DisplayName("Should handle multiple commodities")
        void testMultipleCommodities() {
            PriceCurve gasCurve = PriceCurve.builder()
                .commodity("NATURAL_GAS")
                .valuationDate(valuationDate)
                .addPrice(LocalDate.of(2026, 6, 19), 3.50)
                .addPrice(LocalDate.of(2026, 9, 18), 3.25)
                .build();
            
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .addSpotPrice("NATURAL_GAS", 3.50)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .addForwardCurve("NATURAL_GAS", gasCurve)
                .build();
            
            assertEquals(91.5, marketData.getSpotPrice("CRUDE_OIL"), 0.0001);
            assertEquals(3.50, marketData.getSpotPrice("NATURAL_GAS"), 0.0001);
            
            assertEquals(92.0, marketData.getForwardPrice("CRUDE_OIL", LocalDate.of(2026, 6, 19)), 0.0001);
            assertEquals(3.50, marketData.getForwardPrice("NATURAL_GAS", LocalDate.of(2026, 6, 19)), 0.0001);
        }
        
        @Test
        @DisplayName("Should get all commodities")
        void testGetCommodities() {
            PriceCurve gasCurve = PriceCurve.builder()
                .commodity("NATURAL_GAS")
                .valuationDate(valuationDate)
                .addPrice(LocalDate.of(2026, 6, 19), 3.50)
                .addPrice(LocalDate.of(2026, 9, 18), 3.25)
                .build();
            
            // Create volatility surface for GOLD with 4 points
            VolatilitySurface goldVol = VolatilitySurface.builder()
                .commodity("GOLD")
                .valuationDate(valuationDate)
                .addVolatility(1800.0, LocalDate.of(2026, 6, 19), 0.15)
                .addVolatility(1900.0, LocalDate.of(2026, 6, 19), 0.14)
                .addVolatility(1800.0, LocalDate.of(2026, 9, 18), 0.13)
                .addVolatility(1900.0, LocalDate.of(2026, 9, 18), 0.12)
                .build();
            
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .addForwardCurve("NATURAL_GAS", gasCurve)
                .addVolatilitySurface("GOLD", goldVol)
                .build();
            
            Set<String> commodities = marketData.getCommodities();
            
            assertEquals(3, commodities.size());
            assertTrue(commodities.contains("CRUDE_OIL"));
            assertTrue(commodities.contains("NATURAL_GAS"));
            assertTrue(commodities.contains("GOLD"));
        }
        
        @Test
        @DisplayName("Should handle commodity with partial data")
        void testPartialData() {
            // Crude has everything, Gas only has spot
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .addSpotPrice("NATURAL_GAS", 3.50)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .addVolatilitySurface("CRUDE_OIL", crudeVol)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            // Crude: has everything
            assertTrue(marketData.hasSpotPrice("CRUDE_OIL"));
            assertTrue(marketData.hasForwardCurve("CRUDE_OIL"));
            assertTrue(marketData.hasVolatilitySurface("CRUDE_OIL"));
            assertTrue(marketData.hasHistoricalData("CRUDE_OIL"));
            
            // Gas: only has spot
            assertTrue(marketData.hasSpotPrice("NATURAL_GAS"));
            assertFalse(marketData.hasForwardCurve("NATURAL_GAS"));
            assertFalse(marketData.hasVolatilitySurface("NATURAL_GAS"));
            assertFalse(marketData.hasHistoricalData("NATURAL_GAS"));
        }
    }
    
    // =========================================================================
    // Scenario Creation Tests (toBuilder)
    // =========================================================================
    
    @Nested
    @DisplayName("Scenario Creation Tests")
    class ScenarioCreationTests {
        
        @Test
        @DisplayName("Should create copy with toBuilder()")
        void testToBuilder_CreatesCopy() {
            MarketDataProvider original = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .metadata("scenario", "BASE_CASE")
                .build();
            
            MarketDataProvider modified = original.toBuilder()
                .addSpotPrice("CRUDE_OIL", 100.0)  // Overwrite spot
                .metadata("scenario", "SUPPLY_SHOCK")
                .build();
            
            // Original unchanged
            assertEquals(91.5, original.getSpotPrice("CRUDE_OIL"), 0.0001);
            assertEquals("BASE_CASE", original.getMetadata("scenario", String.class));
            
            // Modified has new values
            assertEquals(100.0, modified.getSpotPrice("CRUDE_OIL"), 0.0001);
            assertEquals("SUPPLY_SHOCK", modified.getMetadata("scenario", String.class));
        }
        
        @Test
        @DisplayName("Should create stress scenario")
        void testToBuilder_StressScenario() {
            MarketDataProvider base = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 90.0)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .build();
            
            // Create shocked curve
            PriceCurve shockedCurve = base.getForwardCurve("CRUDE_OIL")
                .withParallelShift(10.0);
            
            MarketDataProvider shocked = base.toBuilder()
                .addSpotPrice("CRUDE_OIL", 100.0)  // +$10
                .addForwardCurve("CRUDE_OIL", shockedCurve)
                .metadata("scenario", "SUPPLY_SHOCK_+10")
                .build();
            
            // Base unchanged
            assertEquals(90.0, base.getSpotPrice("CRUDE_OIL"), 0.0001);
            assertEquals(92.0, base.getForwardPrice("CRUDE_OIL", LocalDate.of(2026, 6, 19)), 0.0001);
            
            // Shocked has +$10
            assertEquals(100.0, shocked.getSpotPrice("CRUDE_OIL"), 0.0001);
            assertEquals(102.0, shocked.getForwardPrice("CRUDE_OIL", LocalDate.of(2026, 6, 19)), 0.0001);
        }
        
        @Test
        @DisplayName("Should add new commodity via toBuilder()")
        void testToBuilder_AddCommodity() {
            MarketDataProvider original = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .build();
            
            PriceCurve gasCurve = PriceCurve.builder()
                .commodity("NATURAL_GAS")
                .valuationDate(valuationDate)
                .addPrice(LocalDate.of(2026, 6, 19), 3.50)
                .addPrice(LocalDate.of(2026, 9, 18), 3.25)
                .build();
            
            MarketDataProvider expanded = original.toBuilder()
                .addSpotPrice("NATURAL_GAS", 3.50)
                .addForwardCurve("NATURAL_GAS", gasCurve)
                .build();
            
            // Original: only crude
            assertEquals(1, original.getCommodities().size());
            
            // Expanded: both commodities
            assertEquals(2, expanded.getCommodities().size());
            assertTrue(expanded.hasSpotPrice("CRUDE_OIL"));
            assertTrue(expanded.hasSpotPrice("NATURAL_GAS"));
        }
        
        @Test
        @DisplayName("Should modify historical data via toBuilder()")
        void testToBuilder_ModifyHistoricalData() {
            MarketDataProvider original = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            // Create new history with additional point
            TimeSeries extendedHistory = TimeSeries.builder()
                .commodity("CRUDE_OIL")
                .addPoint(LocalDate.of(2026, 3, 1), 88.0)
                .addPoint(LocalDate.of(2026, 3, 15), 90.0)
                .addPoint(LocalDate.of(2026, 3, 20), 91.0)
                .addPoint(LocalDate.of(2026, 3, 21), 92.0)  // New!
                .createTimeSeries();
            
            MarketDataProvider updated = original.toBuilder()
                .addHistoricalData("CRUDE_OIL", extendedHistory)
                .build();
            
            // Original: 3 points
            assertEquals(3, original.getHistoricalData("CRUDE_OIL").size());
            
            // Updated: 4 points
            assertEquals(4, updated.getHistoricalData("CRUDE_OIL").size());
        }
    }
    
    // =========================================================================
    // Immutability Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {
        
        @Test
        @DisplayName("Should be immutable after build")
        void testImmutability() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .build();
            
            // Verify state
            assertEquals(91.5, marketData.getSpotPrice("CRUDE_OIL"), 0.0001);
            
            // No way to modify after build (no setters)
            // This would be a compile error:
            // marketData.addSpotPrice("NATURAL_GAS", 3.50); ← Doesn't exist!
        }
        
        @Test
        @DisplayName("Should return unmodifiable metadata")
        void testUnmodifiableMetadata() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .metadata("source", "Bloomberg")
                .build();
            
            var metadata = marketData.getAllMetadata();
            
            // Try to modify (should throw)
            assertThrows(UnsupportedOperationException.class, () -> {
                metadata.put("newKey", "newValue");
            });
        }
        
        @Test
        @DisplayName("Should return unmodifiable commodity set")
        void testUnmodifiableCommodities() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .build();
            
            Set<String> commodities = marketData.getCommodities();
            
            // Try to modify (should throw)
            assertThrows(UnsupportedOperationException.class, () -> {
                commodities.add("NATURAL_GAS");
            });
        }
    }
    
    // =========================================================================
    // Object Methods Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Object Methods Tests")
    class ObjectMethodsTests {
        
        @Test
        @DisplayName("Should have meaningful toString()")
        void testToString() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .addVolatilitySurface("CRUDE_OIL", crudeVol)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            String str = marketData.toString();
            
            assertTrue(str.contains("MarketDataProvider"));
            assertTrue(str.contains("2026-03-21"));
            assertTrue(str.contains("commodities=1"));
        }
        
        @Test
        @DisplayName("Should return valuation date")
        void testGetValuationDate() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .build();
            
            assertEquals(valuationDate, marketData.getValuationDate());
        }
    }
    
    // =========================================================================
    // Integration Tests with TimeSeries
    // =========================================================================
    
    @Nested
    @DisplayName("TimeSeries Integration Tests")
    class TimeSeriesIntegrationTests {
        
        @Test
        @DisplayName("Should calculate volatility from historical data")
        void testCalculateVolatility() {
            // Build larger historical dataset for volatility
            TimeSeries.Builder builder = TimeSeries.builder().commodity("CRUDE_OIL");
            
            LocalDate start = LocalDate.of(2026, 1, 2);
            double[] prices = {85.0, 86.0, 84.5, 87.0, 85.5, 88.0, 86.5, 87.5, 86.0, 89.0};
            
            for (int i = 0; i < prices.length; i++) {
                builder.addPoint(start.plusDays(i), prices[i]);
            }
            
            TimeSeries history = builder.createTimeSeries();
            
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", history)
                .build();
            
            // Calculate volatility from historical data
            TimeSeries retrieved = marketData.getHistoricalData("CRUDE_OIL");
            double vol = retrieved.calculateVolatility(9);  // 9 returns from 10 prices
            
            assertTrue(vol > 0, "Volatility should be positive");
            assertTrue(vol < 1.0, "Annualized volatility should be reasonable");
        }
        
        @Test
        @DisplayName("Should use historical data for P&L calculation")
        void testPnLFromHistoricalData() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            TimeSeries history = marketData.getHistoricalData("CRUDE_OIL");
            
            // Calculate P&L: Today vs Yesterday
            // Position: Long 100,000 barrels
            double position = 100_000;
            
            double today = history.getValue(LocalDate.of(2026, 3, 20)).getAsDouble();    // 91.0
            double yesterday = history.getValue(LocalDate.of(2026, 3, 15)).getAsDouble(); // 90.0
            
            double pnl = (today - yesterday) * position;
            
            // $1/barrel × 100k barrels = $100,000 profit
            assertEquals(100_000, pnl, 0.01);
        }
        
        @Test
        @DisplayName("Should combine spot, forward, and historical data")
        void testCombinedMarketData() {
            MarketDataProvider marketData = MarketDataProvider.builder()
                .valuationDate(valuationDate)
                .addSpotPrice("CRUDE_OIL", 91.5)
                .addForwardCurve("CRUDE_OIL", crudeCurve)
                .addHistoricalData("CRUDE_OIL", crudeHistory)
                .build();
            
            // Spot price
            assertEquals(91.5, marketData.getSpotPrice("CRUDE_OIL"), 0.01);
            
            // Forward price
            assertEquals(92.0, marketData.getForwardPrice("CRUDE_OIL", LocalDate.of(2026, 6, 19)), 0.01);
            
            // Historical price
            OptionalDouble historicalPrice = marketData.getHistoricalPrice("CRUDE_OIL", LocalDate.of(2026, 3, 15));
            assertTrue(historicalPrice.isPresent());
            assertEquals(90.0, historicalPrice.getAsDouble(), 0.01);
            
            // All three data types coexist
            assertTrue(marketData.hasSpotPrice("CRUDE_OIL"));
            assertTrue(marketData.hasForwardCurve("CRUDE_OIL"));
            assertTrue(marketData.hasHistoricalData("CRUDE_OIL"));
        }
    }
}
 