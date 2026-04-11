package com.fdm.group.Etrm_Project_Prototype;
 
import static org.junit.jupiter.api.Assertions.*;
 
import java.time.LocalDate;
import com.fdm.group.Etrm_Project_Prototype.PriceCurve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
 
/**
 * Comprehensive unit tests for PriceCurve with Builder pattern
 * 
 * Tests cover:
 * - Builder validation
 * - Immutability
 * - Exact match queries
 * - Linear interpolation
 * - Extrapolation
 * - Edge cases
 * - Scenario creation
 * - Metadata
 */
@DisplayName("PriceCurve Unit Tests")
class PriceCurveTest {
    
    private LocalDate valuationDate;
    private LocalDate june;
    private LocalDate july;
    private LocalDate august;
    private LocalDate september;
    private LocalDate december;
    
    @BeforeEach
    void setUp() {
        valuationDate = LocalDate.of(2026, 3, 21);
        june = LocalDate.of(2026, 6, 19);
        july = LocalDate.of(2026, 7, 15);
        august = LocalDate.of(2026, 8, 1);
        september = LocalDate.of(2026, 9, 18);
        december = LocalDate.of(2026, 12, 31);
    }
    
    // =========================================================================
    // Builder Validation Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Builder Validation Tests")
    class BuilderValidationTests {
        
        @Test
        @DisplayName("Should throw if commodity is null")
        void testBuild_NullCommodity() {
            assertThrows(IllegalStateException.class, () -> {
                PriceCurve.builder()
                    // .commodity("CRUDE_OIL") ← Missing!
                    .valuationDate(valuationDate)
                    .addPrice(june, 92.0)
                    .addPrice(september, 90.0)
                    .build();
            }, "Should throw IllegalStateException for null commodity");
        }
        
        @Test
        @DisplayName("Should throw if commodity is empty")
        void testBuild_EmptyCommodity() {
            assertThrows(IllegalStateException.class, () -> {
                PriceCurve.builder()
                    .commodity("   ")  // Empty string
                    .valuationDate(valuationDate)
                    .addPrice(june, 92.0)
                    .addPrice(september, 90.0)
                    .build();
            }, "Should throw IllegalStateException for empty commodity");
        }
        
        @Test
        @DisplayName("Should throw if valuation date is null")
        void testBuild_NullValuationDate() {
            assertThrows(IllegalStateException.class, () -> {
                PriceCurve.builder()
                    .commodity("CRUDE_OIL")
                    // .valuationDate(valuationDate) ← Missing!
                    .addPrice(june, 92.0)
                    .addPrice(september, 90.0)
                    .build();
            }, "Should throw IllegalStateException for null valuation date");
        }
        
        @Test
        @DisplayName("Should throw if no prices added")
        void testBuild_NoPrices() {
            assertThrows(IllegalStateException.class, () -> {
                PriceCurve.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    // No prices!
                    .build();
            }, "Should throw IllegalStateException for no prices");
        }
        
        @Test
        @DisplayName("Should throw if only one price")
        void testBuild_OnlyOnePrice() {
            assertThrows(IllegalStateException.class, () -> {
                PriceCurve.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addPrice(june, 92.0)  // Only 1 price
                    .build();
            }, "Should throw IllegalStateException for < 2 prices");
        }
        
        @Test
        @DisplayName("Should accept exactly 2 prices")
        void testBuild_ExactlyTwoPrices() {
            assertDoesNotThrow(() -> {
                PriceCurve.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addPrice(june, 92.0)
                    .addPrice(september, 90.0)
                    .build();
            }, "Should accept exactly 2 prices");
        }
        
        @Test
        @DisplayName("Should throw if price is negative")
        void testAddPrice_NegativePrice() {
            assertThrows(IllegalArgumentException.class, () -> {
                PriceCurve.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addPrice(june, -92.0);  // Negative!
            }, "Should throw IllegalArgumentException for negative price");
        }
        
        @Test
        @DisplayName("Should throw if delivery date is before valuation date")
        void testAddPrice_PastDeliveryDate() {
            assertThrows(IllegalArgumentException.class, () -> {
                LocalDate pastDate = LocalDate.of(2026, 1, 1);  // Before March 21
                PriceCurve.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addPrice(pastDate, 92.0);  // Date before valuation!
            }, "Should throw IllegalArgumentException for past delivery date");
        }
        
        @Test
        @DisplayName("Should accept zero price")
        void testAddPrice_ZeroPrice() {
            assertDoesNotThrow(() -> {
                PriceCurve.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addPrice(june, 0.0)
                    .addPrice(september, 0.0)
                    .build();
            }, "Should accept zero price");
        }
    }
    
    // =========================================================================
    // Immutability Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {
        
        @Test
        @DisplayName("Should be immutable after build()")
        void testImmutability() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            // Verify initial state
            assertEquals(2, curve.size());
            assertEquals(92.0, curve.getPrice(june), 0.0001);
            
            // Curve is immutable - no way to modify it
            // This would be a compile error:
            // curve.addPrice(december, 88.0); ← Method doesn't exist!
        }
        
        @Test
        @DisplayName("Prices should be unmodifiable")
        void testDefensiveCopy_Prices() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            var prices = curve.getAllPrices();
            
            // Try to modify (should throw)
            assertThrows(UnsupportedOperationException.class, () -> {
                prices.put(december, 88.0);
            }, "Prices map should be unmodifiable");
        }
        
        @Test
        @DisplayName("Metadata should be unmodifiable")
        void testDefensiveCopy_Metadata() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .metadata("source", "Bloomberg")
                .build();
            
            var metadata = curve.getAllMetaData();
            
            // Try to modify (should throw)
            assertThrows(UnsupportedOperationException.class, () -> {
                metadata.put("newKey", "newValue");
            }, "Metadata should be unmodifiable");
        }
    }
    
    // =========================================================================
    // Basic Functionality Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Basic Functionality Tests")
    class BasicFunctionalityTests {
        
        @Test
        @DisplayName("Should return exact match for calibrated date")
        void testGetPrice_ExactMatch() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            assertEquals(92.0, curve.getPrice(june), 0.0001);
            assertEquals(90.0, curve.getPrice(september), 0.0001);
        }
        
        @Test
        @DisplayName("Should return correct commodity")
        void testGetCommodity() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            assertEquals("CRUDE_OIL", curve.getCommodity());
        }
        
        @Test
        @DisplayName("Should return correct valuation date")
        void testGetValuationDate() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            assertEquals(valuationDate, curve.getValuationDate());
        }
        
        @Test
        @DisplayName("Should return correct size")
        void testSize() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .addPrice(december, 88.0)
                .build();
            
            assertEquals(3, curve.size());
        }
        
        @Test
        @DisplayName("Should overwrite price at same date")
        void testAddPrice_Overwrite() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(june, 95.0)  // Overwrite
                .addPrice(september, 90.0)
                .build();
            
            assertEquals(95.0, curve.getPrice(june), 0.0001);
            assertEquals(2, curve.size());  // Still 2 points
        }
    }
    
    // =========================================================================
    // Linear Interpolation Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Linear Interpolation Tests")
    class LinearInterpolationTests {
        
        @Test
        @DisplayName("Should interpolate at midpoint")
        void testInterpolation_Midpoint() {
            // June: $92, Sept: $90
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            // August 1 is roughly midpoint between June 19 and Sept 18
            // Days: June 19 → Aug 1 = 43 days
            // Total: June 19 → Sept 18 = 91 days
            // Weight: 43/91 = 0.4725
            // Price: 92 + 0.4725 * (90 - 92) = 92 - 0.945 = 91.055
            
            double price = curve.getPrice(august);
            
            assertTrue(price > 90.0 && price < 92.0, 
                "Price should be between 90 and 92, got: " + price);
            assertEquals(91.055, price, 0.01);  // Allow small tolerance
        }
        
        @Test
        @DisplayName("Should interpolate at 25% point")
        void testInterpolation_QuarterPoint() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            // July 15 is about 26 days after June 19
            // Total: 91 days
            // Weight: 26/91 = 0.2857
            // Price: 92 + 0.2857 * (90 - 92) = 91.4286
            
            double price = curve.getPrice(july);
            
            assertEquals(91.43, price, 0.01);
        }
        
        @Test
        @DisplayName("Should interpolate with increasing prices (backwardation)")
        void testInterpolation_IncreasingPrices() {
            // Price increasing over time
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 88.0)
                .addPrice(september, 92.0)
                .build();
            
            double price = curve.getPrice(july);
            
            assertTrue(price > 88.0 && price < 92.0, 
                "Price should be between 88 and 92, got: " + price);
        }
        
        @Test
        @DisplayName("Should handle multiple segments")
        void testInterpolation_MultipleSegments() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .addPrice(december, 88.0)
                .build();
            
            // Query in first segment (June → Sept)
            double priceJuly = curve.getPrice(july);
            assertTrue(priceJuly > 90.0 && priceJuly < 92.0);
            
            // Query in second segment (Sept → Dec)
            LocalDate october = LocalDate.of(2026, 10, 15);
            double priceOct = curve.getPrice(october);
            assertTrue(priceOct > 88.0 && priceOct < 90.0);
        }
    }
    
    // =========================================================================
    // Extrapolation Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Extrapolation Tests")
    class ExtrapolationTests {
        
        @Test
        @DisplayName("Should use earliest price for dates before all calibrated dates")
        void testExtrapolation_BeforeAllDates() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            // May 1 is before June 19
            LocalDate may = LocalDate.of(2026, 5, 1);
            double price = curve.getPrice(may);
            
            assertEquals(92.0, price, 0.0001, 
                "Should use earliest price (June)");
        }
        
        @Test
        @DisplayName("Should use latest price for dates after all calibrated dates")
        void testExtrapolation_AfterAllDates() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            System.out.println("Latest price test");
            // January 2027 is after September
            LocalDate january2027 = LocalDate.of(2027, 1, 15);
            double price = curve.getPrice(january2027);
            
            assertEquals(90.0, price, 0.0001, 
                "Should use latest price (September)");
        }
        
        @Test
        @DisplayName("Should extrapolate far into future")
        void testExtrapolation_FarFuture() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            LocalDate farFuture = LocalDate.of(2030, 1, 1);  // 4 years out
            double price = curve.getPrice(farFuture);
            
            assertEquals(90.0, price, 0.0001);
        }
    }
    
    // =========================================================================
    // Edge Case Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle dates very close together")
        void testEdgeCase_CloseDates() {
            LocalDate june19 = LocalDate.of(2026, 6, 19);
            LocalDate june20 = LocalDate.of(2026, 6, 20);  // 1 day later
            
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june19, 92.0)
                .addPrice(june20, 90.0)
                .build();
            
            // Should handle 1-day spacing
            double priceJune19 = curve.getPrice(june19);
            double priceJune20 = curve.getPrice(june20);
            
            assertEquals(92.0, priceJune19, 0.0001);
            assertEquals(90.0, priceJune20, 0.0001);
        }
        
        @Test
        @DisplayName("Should handle very small prices")
        void testEdgeCase_SmallPrices() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 0.01)    // $0.01
                .addPrice(september, 0.02)  // $0.02
                .build();
            
            double price = curve.getPrice(july);
            
            assertTrue(price >= 0.01 && price <= 0.02);
        }
        
        @Test
        @DisplayName("Should handle large prices")
        void testEdgeCase_LargePrices() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 500.0)
                .addPrice(september, 550.0)
                .build();
            
            double price = curve.getPrice(july);
            
            assertTrue(price >= 500.0 && price <= 550.0);
        }
        
        @Test
        @DisplayName("Should handle flat curve (same price)")
        void testEdgeCase_FlatCurve() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 90.0)
                .addPrice(september, 90.0)
                .addPrice(december, 90.0)
                .build();
            
            // All interpolations should return 90.0
            assertEquals(90.0, curve.getPrice(july), 0.0001);
            assertEquals(90.0, curve.getPrice(august), 0.0001);
        }
        
        @Test
        @DisplayName("Should handle large time span")
        void testEdgeCase_LargeTimeSpan() {
            LocalDate year1 = LocalDate.of(2026, 6, 19);
            LocalDate year5 = LocalDate.of(2030, 6, 19);  // 4 years later
            
            System.out.println("Test Case: Large time span difference");
            
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(year1, 92.0)
                .addPrice(year5, 80.0)
                .build();
            
            // Midpoint: 2 years out
            LocalDate year3 = LocalDate.of(2028, 6, 19);
            double price = curve.getPrice(year3);
            
            // Should be approximately midpoint
            assertTrue(price >= 80.0 && price <= 92.0);
            assertEquals(86.0, price, 1.0);  // Approximately $86
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
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .metadata("source", "Bloomberg")
                .metadata("version", "1.0")
                .build();
            
            assertEquals("Bloomberg", curve.getMetadata("source", String.class));
            assertEquals("1.0", curve.getMetadata("version", String.class));
        }
        
        @Test
        @DisplayName("Should return null for missing metadata")
        void testMetadata_MissingKey() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            assertNull(curve.getMetadata("nonexistent", String.class));
        }
        
        @Test
        @DisplayName("Should handle multiple metadata entries")
        void testMetadata_Multiple() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .metadata("source", "Bloomberg")
                .metadata("calibrationTime", "2026-03-21T09:30:00")
                .metadata("curveType", "FORWARD")
                .build();
            
            assertEquals(3, curve.getAllMetaData().size());
        }
    }
    
    // =========================================================================
    // Scenario Creation Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Scenario Creation Tests")
    class ScenarioCreationTests {
        
        @Test
        @DisplayName("Should create copy with toBuilder()")
        void testToBuilder_CreatesCopy() {
            PriceCurve original = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .metadata("source", "Bloomberg")
                .build();
            
            PriceCurve modified = original.toBuilder()
                .addPrice(december, 88.0)
                .metadata("modification", "Added Dec")
                .build();
            
            // Original unchanged
            assertEquals(2, original.size());
            assertNull(original.getMetadata("modification", String.class));
            
            // Modified has new point
            assertEquals(3, modified.size());
            assertEquals("Added Dec", modified.getMetadata("modification", String.class));
        }
        
        @Test
        @DisplayName("Should apply parallel shift")
        void testWithParallelShift_Positive() {
            PriceCurve base = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            PriceCurve shifted = base.withParallelShift(5.0);
            
            // Original unchanged
            assertEquals(92.0, base.getPrice(june), 0.0001);
            assertEquals(90.0, base.getPrice(september), 0.0001);
            
            // Shifted has +$5
            assertEquals(97.0, shifted.getPrice(june), 0.0001);
            assertEquals(95.0, shifted.getPrice(september), 0.0001);
        }
        
        @Test
        @DisplayName("Should apply negative parallel shift")
        void testWithParallelShift_Negative() {
            PriceCurve base = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            PriceCurve shifted = base.withParallelShift(-5.0);
            
            assertEquals(87.0, shifted.getPrice(june), 0.0001);
            assertEquals(85.0, shifted.getPrice(september), 0.0001);
        }
        
        @Test
        @DisplayName("Should apply percentage bump")
        void testWithBump_Positive() {
            PriceCurve base = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 100.0)
                .addPrice(september, 100.0)
                .build();
            
            PriceCurve bumped = base.withBump(0.10);  // +10%
            
            assertEquals(110.0, bumped.getPrice(june), 0.0001);
            assertEquals(110.0, bumped.getPrice(september), 0.0001);
        }
        
        @Test
        @DisplayName("Should apply negative percentage bump")
        void testWithBump_Negative() {
            PriceCurve base = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 100.0)
                .addPrice(september, 100.0)
                .build();
            
            PriceCurve bumped = base.withBump(-0.10);  // -10%
            
            assertEquals(90.0, bumped.getPrice(june), 0.0001);
            assertEquals(90.0, bumped.getPrice(september), 0.0001);
        }
        
        @Test
        @DisplayName("Should update single price with withPrice()")
        void testWithPrice_UpdatesPoint() {
            PriceCurve original = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            PriceCurve updated = original.withPrice(june, 95.0);
            
            // Original unchanged
            assertEquals(92.0, original.getPrice(june), 0.0001);
            
            // Updated has new value
            assertEquals(95.0, updated.getPrice(june), 0.0001);
            assertEquals(90.0, updated.getPrice(september), 0.0001);  // Other unchanged
        }
    }
    
    // =========================================================================
    // toString, equals, hashCode Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Object Method Tests")
    class ObjectMethodTests {
        
        @Test
        @DisplayName("Should have meaningful toString()")
        void testToString() {
            PriceCurve curve = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            String str = curve.toString();
            
            assertTrue(str.contains("CRUDE_OIL"));
            assertTrue(str.contains("2026-03-21"));
            assertTrue(str.contains("2"));  // Number of points
        }
        
        @Test
        @DisplayName("Should implement equals() correctly")
        void testEquals() {
            PriceCurve curve1 = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            PriceCurve curve2 = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            assertEquals(curve1, curve2);
        }
        
        @Test
        @DisplayName("Should implement hashCode() correctly")
        void testHashCode() {
            PriceCurve curve1 = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            PriceCurve curve2 = PriceCurve.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addPrice(june, 92.0)
                .addPrice(september, 90.0)
                .build();
            
            assertEquals(curve1.hashCode(), curve2.hashCode());
        }
    }
}