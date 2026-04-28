package com.fdm.group.Etrm_Project_Prototype;
 
import static org.junit.jupiter.api.Assertions.*;
 
import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
 
/**
 * Comprehensive unit tests for TimeSeries class
 */
@DisplayName("TimeSeries Unit Tests")
class TimeSeriesTest {
    
    private TimeSeries crudeOilSeries;
    private LocalDate date1;
    private LocalDate date2;
    private LocalDate date3;
    private LocalDate date4;
    private LocalDate date5;
    
    @BeforeEach
    void setUp() {
        // Create a sample time series with 5 data points
        date1 = LocalDate.of(2026, 1, 2);   // Friday
        date2 = LocalDate.of(2026, 1, 5);   // Monday (skip weekend)
        date3 = LocalDate.of(2026, 1, 6);   // Tuesday
        date4 = LocalDate.of(2026, 1, 7);   // Wednesday
        date5 = LocalDate.of(2026, 1, 8);   // Thursday
        
        crudeOilSeries = TimeSeries.builder()
            .commodity("CRUDE_OIL")
            .addPoint(date1, 85.50)
            .addPoint(date2, 86.20)
            .addPoint(date3, 84.80)
            .addPoint(date4, 85.10)
            .addPoint(date5, 86.50)
            .createTimeSeries();
    }
    
    // =========================================================================
    // Builder Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("Should build valid time series with builder")
        void testBuilderCreatesValidTimeSeries() {
            TimeSeries ts = TimeSeries.builder()
                .commodity("NATURAL_GAS")
                .addPoint(LocalDate.of(2026, 1, 2), 3.25)
                .addPoint(LocalDate.of(2026, 1, 3), 3.30)
                .createTimeSeries();
            
            assertNotNull(ts);
            assertEquals(2, ts.size());
        }
        
        @Test
        @DisplayName("Should throw exception when building without commodity")
        void testBuilderRequiresCommodity() {
            TimeSeries.Builder builder = TimeSeries.builder()
                .addPoint(LocalDate.of(2026, 1, 2), 85.0);
            
            assertThrows(IllegalArgumentException.class, () -> builder.createTimeSeries());
        }
        
        @Test
        @DisplayName("Should allow building empty time series with commodity")
        void testBuilderAllowsEmptySeries() {
            TimeSeries ts = TimeSeries.builder()
                .commodity("EMPTY_COMMODITY")
                .createTimeSeries();
            
            assertNotNull(ts);
            assertTrue(ts.isEmpty());
            assertEquals(0, ts.size());
        }
        
        @Test
        @DisplayName("Should overwrite duplicate dates")
        void testBuilderOverwritesDuplicates() {
            LocalDate date = LocalDate.of(2026, 1, 2);
            
            TimeSeries ts = TimeSeries.builder()
                .commodity("TEST")
                .addPoint(date, 100.0)
                .addPoint(date, 200.0)  // Overwrite
                .createTimeSeries();
            
            assertEquals(1, ts.size());
            assertEquals(200.0, ts.getValue(date).getAsDouble(), 0.01);
        }
    }
    
    // =========================================================================
    // Basic Query Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Basic Query Tests")
    class BasicQueryTests {
        
        @Test
        @DisplayName("Should return correct size")
        void testSize() {
            assertEquals(5, crudeOilSeries.size());
        }
        
        @Test
        @DisplayName("Should not be empty when data exists")
        void testIsNotEmpty() {
            assertFalse(crudeOilSeries.isEmpty());
        }
        
        @Test
        @DisplayName("Should be empty when no data")
        void testIsEmpty() {
            TimeSeries empty = TimeSeries.builder()
                .commodity("EMPTY")
                .createTimeSeries();
            
            assertTrue(empty.isEmpty());
        }
        
        @Test
        @DisplayName("Should return true when date exists")
        void testContainsDate() {
            assertTrue(crudeOilSeries.containsDate(date1));
            assertTrue(crudeOilSeries.containsDate(date5));
        }
        
        @Test
        @DisplayName("Should return false when date doesn't exist")
        void testDoesNotContainDate() {
            LocalDate weekend = LocalDate.of(2026, 1, 3);  // Saturday
            assertFalse(crudeOilSeries.containsDate(weekend));
        }
        
        @Test
        @DisplayName("Should return earliest date")
        void testGetEarliestDate() {
            assertEquals(date1, crudeOilSeries.getEarliestDate());
        }
        
        @Test
        @DisplayName("Should return latest date")
        void testGetLatestDate() {
            assertEquals(date5, crudeOilSeries.getLatestDate());
        }
        
        @Test
        @DisplayName("Should throw exception when getting earliest from empty series")
        void testGetEarliestDateFromEmpty() {
            TimeSeries empty = TimeSeries.builder()
                .commodity("EMPTY")
                .createTimeSeries();
            
            assertThrows(IllegalStateException.class, () -> empty.getEarliestDate());
        }
        
        @Test
        @DisplayName("Should throw exception when getting latest from empty series")
        void testGetLatestDateFromEmpty() {
            TimeSeries empty = TimeSeries.builder()
                .commodity("EMPTY")
                .createTimeSeries();
            
            assertThrows(IllegalStateException.class, () -> empty.getLatestDate());
        }
    }
    
    // =========================================================================
    // getValue Tests
    // =========================================================================
    
    @Nested
    @DisplayName("getValue Tests")
    class GetValueTests {
        
        @Test
        @DisplayName("Should return value for existing date")
        void testGetValueExistingDate() {
            OptionalDouble value = crudeOilSeries.getValue(date1);
            
            assertTrue(value.isPresent());
            assertEquals(85.50, value.getAsDouble(), 0.01);
        }
        
        @Test
        @DisplayName("Should return empty for missing date")
        void testGetValueMissingDate() {
            LocalDate weekend = LocalDate.of(2026, 1, 3);  // Saturday
            OptionalDouble value = crudeOilSeries.getValue(weekend);
            
            assertFalse(value.isPresent());
        }
        
        @Test
        @DisplayName("Should allow using orElse for missing values")
        void testGetValueWithDefault() {
            LocalDate missing = LocalDate.of(2026, 1, 3);
            double value = crudeOilSeries.getValue(missing).orElse(0.0);
            
            assertEquals(0.0, value, 0.01);
        }
        
        @Test
        @DisplayName("Should return all different values correctly")
        void testGetValueMultipleDates() {
            assertEquals(85.50, crudeOilSeries.getValue(date1).getAsDouble(), 0.01);
            assertEquals(86.20, crudeOilSeries.getValue(date2).getAsDouble(), 0.01);
            assertEquals(84.80, crudeOilSeries.getValue(date3).getAsDouble(), 0.01);
            assertEquals(85.10, crudeOilSeries.getValue(date4).getAsDouble(), 0.01);
            assertEquals(86.50, crudeOilSeries.getValue(date5).getAsDouble(), 0.01);
        }
    }
    
    // =========================================================================
    // Statistics Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {
        
        @Test
        @DisplayName("Should calculate minimum value")
        void testMinValue() {
            double min = crudeOilSeries.minValue();
            assertEquals(84.80, min, 0.01);
        }
        
        @Test
        @DisplayName("Should calculate maximum value")
        void testMaxValue() {
            double max = crudeOilSeries.maxValue();
            assertEquals(86.50, max, 0.01);
        }
        
        @Test
        @DisplayName("Should calculate mean value")
        void testMeanValue() {
            // (85.50 + 86.20 + 84.80 + 85.10 + 86.50) / 5 = 85.62
            double mean = crudeOilSeries.meanValue();
            assertEquals(85.62, mean, 0.01);
        }
        
        @Test
        @DisplayName("Should calculate standard deviation")
        void testStandardDeviation() {
            double stdDev = crudeOilSeries.standardDeviation();
            
            // Manual calculation:
            // mean = 85.62
            // deviations: [-0.12, 0.58, -0.82, -0.52, 0.88]
            // squared: [0.0144, 0.3364, 0.6724, 0.2704, 0.7744]
            // sum: 2.068
            // variance: 2.068 / 4 = 0.517
            // stddev: sqrt(0.517) = 0.719
            
            assertEquals(0.719, stdDev, 0.01);
        }
        
        @Test
        @DisplayName("Should throw exception for min on empty series")
        void testMinValueEmpty() {
            TimeSeries empty = TimeSeries.builder()
                .commodity("EMPTY")
                .createTimeSeries();
            
            assertThrows(IllegalStateException.class, () -> empty.minValue());
        }
        
        @Test
        @DisplayName("Should throw exception for standard deviation with fewer than 2 points")
        void testStandardDeviationInsufficientData() {
            TimeSeries single = TimeSeries.builder()
                .commodity("SINGLE")
                .addPoint(LocalDate.of(2026, 1, 2), 85.0)
                .createTimeSeries();
            
            assertThrows(IllegalStateException.class, () -> single.standardDeviation());
        }
        
        @Test
        @DisplayName("Should calculate statistics for series with 2 points")
        void testStatisticsTwoPoints() {
            TimeSeries twoPoints = TimeSeries.builder()
                .commodity("TWO")
                .addPoint(LocalDate.of(2026, 1, 2), 80.0)
                .addPoint(LocalDate.of(2026, 1, 3), 90.0)
                .createTimeSeries();
            
            assertEquals(80.0, twoPoints.minValue(), 0.01);
            assertEquals(90.0, twoPoints.maxValue(), 0.01);
            assertEquals(85.0, twoPoints.meanValue(), 0.01);
            
            // Std dev of [80, 90]: mean=85, deviations=[-5, 5], variance=50/1=50, stddev=7.07
            assertEquals(7.07, twoPoints.standardDeviation(), 0.01);
        }
    }
    
    // =========================================================================
    // SubSeries Tests
    // =========================================================================
    
    @Nested
    @DisplayName("SubSeries Tests")
    class SubSeriesTests {
        
        @Test
        @DisplayName("Should return subseries for valid range")
        void testSubSeries() {
            List<Double> values = crudeOilSeries.subSeries(date2, date4);
            
            // Should include date2, date3, date4 (3 values)
            assertEquals(3, values.size());
            assertTrue(values.contains(86.20));  // date2
            assertTrue(values.contains(84.80));  // date3
            assertTrue(values.contains(85.10));  // date4
        }
        
        @Test
        @DisplayName("Should return empty list for range with no data")
        void testSubSeriesNoData() {
            LocalDate start = LocalDate.of(2027, 1, 1);
            LocalDate end = LocalDate.of(2027, 1, 31);
            
            List<Double> values = crudeOilSeries.subSeries(start, end);
            
            assertTrue(values.isEmpty());
        }
        
        @Test
        @DisplayName("Should include boundary dates when inclusive")
        void testSubSeriesInclusive() {
            List<Double> values = crudeOilSeries.subSeries(date1, date1);
            
            // Both boundaries same and inclusive -> 1 value
            assertEquals(1, values.size());
            assertEquals(85.50, values.get(0), 0.01);
        }
    }
    
    // =========================================================================
    // TailSeries Tests
    // =========================================================================
    
    @Nested
    @DisplayName("TailSeries Tests")
    class TailSeriesTests {
        
        @Test
        @DisplayName("Should return last N points")
        void testTailSeries() {
            TimeSeries tail = crudeOilSeries.tailSeries(3);
            
            assertEquals(3, tail.size());
            
            // Should have last 3 dates: date3, date4, date5
            assertTrue(tail.containsDate(date3));
            assertTrue(tail.containsDate(date4));
            assertTrue(tail.containsDate(date5));
            
            assertFalse(tail.containsDate(date1));
            assertFalse(tail.containsDate(date2));
        }
        
        @Test
        @DisplayName("Should return all points when N >= size")
        void testTailSeriesAllPoints() {
            TimeSeries tail = crudeOilSeries.tailSeries(10);
            
            assertEquals(5, tail.size());
            assertEquals(crudeOilSeries.size(), tail.size());
        }
        
        @Test
        @DisplayName("Should throw exception for negative N")
        void testTailSeriesNegative() {
            assertThrows(IllegalArgumentException.class, () -> 
                crudeOilSeries.tailSeries(-1)
            );
        }
        
        @Test
        @DisplayName("Should return copy, not reference")
        void testTailSeriesImmutability() {
            TimeSeries tail = crudeOilSeries.tailSeries(3);
            
            // Adding to tail shouldn't affect original
            tail.addPoint(LocalDate.of(2026, 1, 9), 90.0);
            
            assertEquals(4, tail.size());
            assertEquals(5, crudeOilSeries.size());  // Original unchanged
        }
    }
    
    // =========================================================================
    // HeadSeries Tests
    // =========================================================================
    
    @Nested
    @DisplayName("HeadSeries Tests")
    class HeadSeriesTests {
        
        @Test
        @DisplayName("Should return first N points")
        void testHeadSeries() {
            TimeSeries head = crudeOilSeries.headSeries(3);
            
            assertEquals(3, head.size());
            
            // Should have first 3 dates: date1, date2, date3
            assertTrue(head.containsDate(date1));
            assertTrue(head.containsDate(date2));
            assertTrue(head.containsDate(date3));
            
            assertFalse(head.containsDate(date4));
            assertFalse(head.containsDate(date5));
        }
        
        @Test
        @DisplayName("Should return all points when N >= size")
        void testHeadSeriesAllPoints() {
            TimeSeries head = crudeOilSeries.headSeries(10);
            
            assertEquals(5, head.size());
        }
        
        @Test
        @DisplayName("Should throw exception for negative N")
        void testHeadSeriesNegative() {
            assertThrows(IllegalArgumentException.class, () -> 
                crudeOilSeries.headSeries(-1)
            );
        }
    }
    
    // =========================================================================
    // MapValues Tests
    // =========================================================================
    
    @Nested
    @DisplayName("MapValues Tests")
    class MapValuesTests {
        
        @Test
        @DisplayName("Should apply function to all values")
        void testMapValues() {
            TimeSeries doubled = crudeOilSeries.mapValues(price -> price * 2);
            
            assertEquals(5, doubled.size());
            assertEquals(171.0, doubled.getValue(date1).getAsDouble(), 0.01);  // 85.50 * 2
            assertEquals(172.4, doubled.getValue(date2).getAsDouble(), 0.01);  // 86.20 * 2
        }
        
        @Test
        @DisplayName("Should convert units (dollars to euros)")
        void testMapValuesUnitConversion() {
            TimeSeries euros = crudeOilSeries.mapValues(price -> price * 0.92);
            
            assertEquals(78.66, euros.getValue(date1).getAsDouble(), 0.01);  // 85.50 * 0.92
        }
        
        @Test
        @DisplayName("Should add premium to all prices")
        void testMapValuesAddPremium() {
            TimeSeries withPremium = crudeOilSeries.mapValues(price -> price + 5.0);
            
            assertEquals(90.50, withPremium.getValue(date1).getAsDouble(), 0.01);  // 85.50 + 5
        }
        
        @Test
        @DisplayName("Should not modify original series")
        void testMapValuesImmutability() {
            TimeSeries modified = crudeOilSeries.mapValues(price -> price * 2);
            
            // Original should be unchanged
            assertEquals(85.50, crudeOilSeries.getValue(date1).getAsDouble(), 0.01);
            
            // New series should have modified values
            assertEquals(171.0, modified.getValue(date1).getAsDouble(), 0.01);
        }
        
        @Test
        @DisplayName("Should throw exception for null mapper")
        void testMapValuesNullMapper() {
            assertThrows(NullPointerException.class, () -> 
                crudeOilSeries.mapValues(null)
            );
        }
    }
    
    // =========================================================================
    // Volatility Calculation Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Volatility Calculation Tests")
    class VolatilityTests {
        
        @Test
        @DisplayName("Should calculate volatility for valid periods")
        void testCalculateVolatility() {
            double vol = crudeOilSeries.calculateVolatility(4);
            
            // Should calculate log returns and annualize
            assertTrue(vol > 0, "Volatility should be positive");
            assertTrue(vol < 1.0, "Annualized volatility should be reasonable");
        }
        
        @Test
        @DisplayName("Should throw exception when insufficient data")
        void testCalculateVolatilityInsufficientData() {
            assertThrows(IllegalArgumentException.class, () -> 
                crudeOilSeries.calculateVolatility(10)  // Only 5 points, need 11
            );
        }
        
        @Test
        @DisplayName("Should calculate volatility for minimum required periods")
        void testCalculateVolatilityMinimum() {
            double vol = crudeOilSeries.calculateVolatility(1);
            
            // Should work with just 2 points (1 return)
            assertTrue(vol >= 0);
        }
    }
    
    // =========================================================================
    // Edge Cases
    // =========================================================================
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle single data point")
        void testSingleDataPoint() {
            TimeSeries single = TimeSeries.builder()
                .commodity("SINGLE")
                .addPoint(LocalDate.of(2026, 1, 2), 100.0)
                .createTimeSeries();
            
            assertEquals(1, single.size());
            assertFalse(single.isEmpty());
            assertEquals(100.0, single.getValue(LocalDate.of(2026, 1, 2)).getAsDouble());
        }
        
        @Test
        @DisplayName("Should handle large price values")
        void testLargePrices() {
            TimeSeries large = TimeSeries.builder()
                .commodity("EXPENSIVE")
                .addPoint(LocalDate.of(2026, 1, 2), 1_000_000.0)
                .addPoint(LocalDate.of(2026, 1, 3), 2_000_000.0)
                .createTimeSeries();
            
            assertEquals(1_500_000.0, large.meanValue(), 0.01);
        }
        
        @Test
        @DisplayName("Should handle small price values")
        void testSmallPrices() {
            TimeSeries small = TimeSeries.builder()
                .commodity("CHEAP")
                .addPoint(LocalDate.of(2026, 1, 2), 0.001)
                .addPoint(LocalDate.of(2026, 1, 3), 0.002)
                .createTimeSeries();
            
            assertEquals(0.0015, small.meanValue(), 0.0001);
        }
        
        @Test
        @DisplayName("Should handle dates spanning multiple years")
        void testMultiYearRange() {
            TimeSeries multiYear = TimeSeries.builder()
                .commodity("MULTI_YEAR")
                .addPoint(LocalDate.of(2024, 1, 1), 80.0)
                .addPoint(LocalDate.of(2025, 1, 1), 85.0)
                .addPoint(LocalDate.of(2026, 1, 1), 90.0)
                .createTimeSeries();
            
            assertEquals(LocalDate.of(2024, 1, 1), multiYear.getEarliestDate());
            assertEquals(LocalDate.of(2026, 1, 1), multiYear.getLatestDate());
            assertEquals(3, multiYear.size());
        }
    }
}
 
