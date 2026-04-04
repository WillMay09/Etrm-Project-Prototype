package com.fdm.group.Etrm_Project_Prototype;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
 
/**
 * Unit tests for VolatilitySurface with Builder pattern
 * 
 * Tests cover:
 * - Builder validation
 * - Immutability
 * - Interpolation (bilinear, strike-only, time-only)
 * - Edge cases
 * - Scenario creation (toBuilder, withVolatility)
 * - Metadata
 */
@DisplayName("VolatilitySurface Builder Pattern Tests")
class VolatilitySurfaceBuilderTest {
    
    private LocalDate valuationDate;
    private LocalDate june;
    private LocalDate sept;
    private LocalDate august;
    
    @BeforeEach
    void setUp() {
        valuationDate = LocalDate.of(2026, 3, 21);
        june = LocalDate.of(2026, 6, 19);
        sept = LocalDate.of(2026, 9, 18);
        august = LocalDate.of(2026, 8, 1);
    }
    
    // =========================================================================
    // Builder Validation Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Builder Validation Tests")
    class BuilderValidationTests {
        
        @Test
        @DisplayName("Should throw exception if commodity is null")
        void testBuild_NullCommodity() {
            assertThrows(IllegalStateException.class, () -> {
                VolatilitySurface.builder()
                    // .commodity("CRUDE_OIL") ← Missing!
                    .valuationDate(valuationDate)
                    .addVolatility(80.0, june, 0.30)
                    .addVolatility(90.0, june, 0.26)
                    .addVolatility(100.0, june, 0.25)
                    .addVolatility(110.0, june, 0.27)
                    .build();
            }, "Should throw IllegalStateException for null commodity");
        }
        
        @Test
        @DisplayName("Should throw exception if commodity is empty")
        void testBuild_EmptyCommodity() {
            assertThrows(IllegalStateException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("   ")  // Empty string
                    .valuationDate(valuationDate)
                    .addVolatility(80.0, june, 0.30)
                    .addVolatility(90.0, june, 0.26)
                    .addVolatility(100.0, june, 0.25)
                    .addVolatility(110.0, june, 0.27)
                    .build();
            }, "Should throw IllegalStateException for empty commodity");
        }
        
        @Test
        @DisplayName("Should throw exception if valuation date is null")
        void testBuild_NullValuationDate() {
            assertThrows(IllegalStateException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    // .valuationDate(valuationDate) ← Missing!
                    .addVolatility(80.0, june, 0.30)
                    .addVolatility(90.0, june, 0.26)
                    .addVolatility(100.0, june, 0.25)
                    .addVolatility(110.0, june, 0.27)
                    .build();
            }, "Should throw IllegalStateException for null valuation date");
        }
        
        @Test
        @DisplayName("Should throw exception if less than 4 points")
        void testBuild_InsufficientPoints() {
            assertThrows(IllegalStateException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(100.0, june, 0.25)
                    .addVolatility(100.0, sept, 0.24)
                    // Only 2 points - need 4!
                    .build();
            }, "Should throw IllegalStateException for less than 4 points");
        }
        
        @Test
        @DisplayName("Should accept exactly 4 points")
        void testBuild_ExactlyFourPoints() {
            assertDoesNotThrow(() -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(80.0, june, 0.30)
                    .addVolatility(90.0, june, 0.26)
                    .addVolatility(100.0, june, 0.25)
                    .addVolatility(110.0, june, 0.27)
                    .build();
            }, "Should accept exactly 4 points");
        }
        
        @Test
        @DisplayName("Should validate strike is positive")
        void testAddVolatility_NegativeStrike() {
            assertThrows(IllegalArgumentException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(-100.0, june, 0.25);  // Negative strike!
            }, "Should throw IllegalArgumentException for negative strike");
        }
        
        @Test
        @DisplayName("Should validate volatility is in range")
        void testAddVolatility_InvalidVolatility() {
            assertThrows(IllegalArgumentException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(100.0, june, -0.25);  // Negative vol!
            }, "Should throw IllegalArgumentException for negative volatility");
            
            assertThrows(IllegalArgumentException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(100.0, june, 15.0);  // 1500% vol!
            }, "Should throw IllegalArgumentException for volatility > 1000%");
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
            // Build surface
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            // Verify initial state
            assertEquals(4, surface.size());
            assertEquals(0.25, surface.getVolatility(100.0, june), 0.0001);
            
            // Surface should be immutable - no way to call addVolatility()
            // This would be a compile error:
           // surface.addVolatility(120.0, june, 0.28);// ← Method doesn't exist!
        }
        
        @Test
        @DisplayName("Should create defensive copy of calibrated points")
        void testDefensiveCopy_CalibratedPoints() {
            // Build surface
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            // Get calibrated points
            var points = surface.getCalibratedPoints();
            
            // Try to modify (should throw)
            assertThrows(UnsupportedOperationException.class, () -> {
                points.put(new VolatilitySurface.VolatilityPoint(120.0, june), 0.28);
            }, "Calibrated points should be unmodifiable");
        }
        
        @Test
        @DisplayName("Should create defensive copy of metadata")
        void testDefensiveCopy_Metadata() {
            // Build surface with metadata
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .metadata("source", "Bloomberg")
                .build();
            
            // Get metadata
            var metadata = surface.getAllMetadata();
            
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
        @DisplayName("Should return exact match for calibrated point")
        void testGetVolatility_ExactMatch() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            assertEquals(0.25, surface.getVolatility(100.0, june), 0.0001);
            assertEquals(0.30, surface.getVolatility(80.0, june), 0.0001);
        }
        
        @Test
        @DisplayName("Should return correct commodity")
        void testGetCommodity() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            assertEquals("CRUDE_OIL", surface.getCommodity());
        }
        
        @Test
        @DisplayName("Should return correct valuation date")
        void testGetValuationDate() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            assertEquals(valuationDate, surface.getValuationDate());
        }
        
        @Test
        @DisplayName("Should return correct size")
        void testSize() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .addVolatility(80.0, sept, 0.28)
                .addVolatility(90.0, sept, 0.25)
                .build();
            
            assertEquals(6, surface.size());
        }
    }
    
    // =========================================================================
    // Interpolation Tests - Strike Dimension
    // =========================================================================
    
    @Nested
    @DisplayName("Strike Interpolation Tests")
    class StrikeInterpolationTests {
        
        @Test
        @DisplayName("Should interpolate between two strikes (same expiry)")
        void testInterpolation_StrikeOnly() {
            // Flat time profile (same expiry for all strikes)
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            // Query at strike 95 (between 90 and 100)
            double vol = surface.getVolatility(95.0, june);
            
            // Should be between 0.26 and 0.25
            assertTrue(vol >= 0.25 && vol <= 0.26, "Vol should be in range [0.25, 0.26], got: " + vol);
            
            // At midpoint, should be approximately average
            assertEquals(0.255, vol, 0.001);
        }
        
        @Test
        @DisplayName("Should handle strike interpolation at 25% point")
        void testInterpolation_StrikeQuarterPoint() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            // Query at 92.5 (25% between 90 and 100)
            double vol = surface.getVolatility(92.5, june);
            
            // vol = 0.26 + (0.25 - 0.26) * 0.25 = 0.2575
            assertEquals(0.2575, vol, 0.001);
        }
    }
    
    // =========================================================================
    // Interpolation Tests - Time Dimension
    // =========================================================================
    
    @Nested
    @DisplayName("Time Interpolation Tests")
    class TimeInterpolationTests {
        
        @Test
        @DisplayName("Should interpolate between two expiries (same strike)")
        void testInterpolation_TimeOnly() {
            // Flat strike profile (same strike for all expiries)
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(110.0, june, 0.26)
                .addVolatility(90.0, sept, 0.24)
                .addVolatility(110.0, sept, 0.24)
                .build();
            
            // Query at strike 100 (between 90 and 110), date in August
            double vol = surface.getVolatility(100.0, august);
            
            // Should be between June (0.26) and Sept (0.24)
            assertTrue(vol >= 0.24 && vol <= 0.26, 
                "Vol should be between 0.24 and 0.26, got: " + vol);
        }
        
        @Test
        @DisplayName("Should handle time interpolation correctly")
        void testInterpolation_TimeCalculation() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(90.0, june, 0.25)
                .addVolatility(110.0, june, 0.25)
                .addVolatility(90.0, sept, 0.24)
                .addVolatility(110.0, sept, 0.24)
                .build();
            
            // Query exactly at June expiry
            double volJune = surface.getVolatility(100.0, june);
            assertEquals(0.25, volJune, 0.001);
            
            // Query exactly at Sept expiry
            double volSept = surface.getVolatility(100.0, sept);
            assertEquals(0.24, volSept, 0.001);
        }
    }
    
    // =========================================================================
    // Bilinear Interpolation Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Bilinear Interpolation Tests")
    class BilinearInterpolationTests {
        
        @Test
        @DisplayName("Should perform bilinear interpolation")
        void testInterpolation_Bilinear() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .addVolatility(80.0, sept, 0.28)
                .addVolatility(90.0, sept, 0.25)
                .addVolatility(100.0, sept, 0.24)
                .addVolatility(110.0, sept, 0.25)
                .build();
            
            // Query at (95.0, August) - interpolates in both dimensions
            double vol = surface.getVolatility(95.0, august);
            
            // Should be in overall range
            assertTrue(vol >= 0.24 && vol <= 0.30, 
                "Vol should be in range [0.24, 0.30], got: " + vol);
        }
    }
    
    // =========================================================================
    // Edge Case Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle strikes very close together")
        void testEdgeCase_CloseStrikes() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(100.0, sept, 0.25)
                .addVolatility(100.01, june, 0.26)
                .addVolatility(100.01, sept, 0.26)
                .build();
            
            // Query at midpoint
            double vol = surface.getVolatility(100.005, june);
            
            assertTrue(vol >= 0.25 && vol <= 0.26, "Vol should be in range, got: " + vol);
            assertEquals(0.255, vol, 0.001);
        }
        
        @Test
        @DisplayName("Should handle expiries very close together")
        void testEdgeCase_CloseExpiries() {
            LocalDate june19 = LocalDate.of(2026, 6, 19);
            LocalDate june20 = LocalDate.of(2026, 6, 20);  // 1 day later
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(90.0, june19, 0.25)
                .addVolatility(110.0, june19, 0.25)
                .addVolatility(90.0, june20, 0.26)
                .addVolatility(110.0, june20, 0.26)
                .build();
            
            // Query at June 19
            double vol = surface.getVolatility(100.0, june19);
            
            assertTrue(vol >= 0.25 && vol <= 0.26, 
                "Should handle 1-day spacing, got: " + vol);
        }
        
        @Test
        @DisplayName("Should handle very small volatilities")
        void testEdgeCase_SmallVolatilities() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.01)   // 1%
                .addVolatility(90.0, june, 0.015)  // 1.5%
                .addVolatility(100.0, june, 0.02)  // 2%
                .addVolatility(110.0, june, 0.025) // 2.5%
                .build();
            
            double vol = surface.getVolatility(95.0, june);
            
            assertTrue(vol >= 0.015 && vol <= 0.02, "Small vols should interpolate correctly");
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
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .metadata("source", "Bloomberg")
                .metadata("version", "1.0")
                .build();
            
            assertEquals("Bloomberg", surface.getMetadata("source", String.class));
            assertEquals("1.0", surface.getMetadata("version", String.class));
        }
        
        @Test
        @DisplayName("Should return null for missing metadata")
        void testMetadata_MissingKey() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            assertNull(surface.getMetadata("nonexistent", String.class));
        }
        
        @Test
        @DisplayName("Should handle multiple metadata entries")
        void testMetadata_Multiple() {
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .metadata("source", "Bloomberg")
                .metadata("calibrationTime", "2026-03-21T09:30:00")
                .metadata("interpolationType", "BILINEAR")
                .metadata("dayCount", "ACT_365")
                .build();
            
            assertEquals(4, surface.getAllMetadata().size());
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
            // Original surface
            VolatilitySurface original = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .metadata("source", "Bloomberg")
                .build();
            
            // Create modified version
            VolatilitySurface modified = original.toBuilder()
                .addVolatility(120.0, june, 0.28)
                .metadata("modification", "Added 120 strike")
                .build();
            
            // Original unchanged
            assertEquals(4, original.size());
            assertEquals("Bloomberg", original.getMetadata("source", String.class));
            assertNull(original.getMetadata("modification", String.class));
            
            // Modified has new point
            assertEquals(5, modified.size());
            assertEquals("Bloomberg", modified.getMetadata("source", String.class));
            assertEquals("Added 120 strike", modified.getMetadata("modification", String.class));
        }
        
        @Test
        @DisplayName("Should update single point with withVolatility()")
        void testWithVolatility_UpdatesPoint() {
            // Original surface
            VolatilitySurface original = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            // Update one point
            VolatilitySurface updated = original.withVolatility(100.0, june, 0.28);
            
            // Original unchanged
            assertEquals(0.25, original.getVolatility(100.0, june), 0.0001);
            
            // Updated has new value
            assertEquals(0.28, updated.getVolatility(100.0, june), 0.0001);
        }
        
        @Test
        @DisplayName("Should preserve other points when updating")
        void testWithVolatility_PreservesOtherPoints() {
            VolatilitySurface original = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .build();
            
            VolatilitySurface updated = original.withVolatility(100.0, june, 0.28);
            
            // Other points unchanged
            assertEquals(0.30, updated.getVolatility(80.0, june), 0.0001);
            assertEquals(0.26, updated.getVolatility(90.0, june), 0.0001);
            assertEquals(0.27, updated.getVolatility(110.0, june), 0.0001);
        }
    }
    
    // =========================================================================
    // Integration Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should handle complete workflow")
        void testIntegration_CompleteWorkflow() {
            // Build realistic surface
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                // June expiry (3 months)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                // September expiry (6 months)
                .addVolatility(80.0, sept, 0.28)
                .addVolatility(90.0, sept, 0.25)
                .addVolatility(100.0, sept, 0.24)
                .addVolatility(110.0, sept, 0.25)
                .metadata("source", "Bloomberg")
                .build();
            
            // Test various query types
            
            // 1. Exact match
            double exactMatch = surface.getVolatility(100.0, june);
            assertEquals(0.25, exactMatch, 0.0001);
            
            // 2. Strike interpolation
            double strikeInterp = surface.getVolatility(95.0, june);
            assertTrue(strikeInterp >= 0.25 && strikeInterp <= 0.26);
            assertEquals(0.255, strikeInterp, 0.001);
            
            // 3. Time interpolation
            double timeInterp = surface.getVolatility(100.0, august);
            assertTrue(timeInterp >= 0.24 && timeInterp <= 0.25);
            
            // 4. Bilinear interpolation
            double bilinearInterp = surface.getVolatility(95.0, august);
            assertTrue(bilinearInterp >= 0.24 && bilinearInterp <= 0.27);
            
            // 5. Metadata
            assertEquals("Bloomberg", surface.getMetadata("source", String.class));
        }
        
        @Test
        @DisplayName("Should support realistic trading workflow")
        void testIntegration_TradingWorkflow() {
            // Morning: Load base surface
            VolatilitySurface morning = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, june, 0.30)
                .addVolatility(90.0, june, 0.26)
                .addVolatility(100.0, june, 0.25)
                .addVolatility(110.0, june, 0.27)
                .metadata("source", "Bloomberg")
                .metadata("timestamp", "09:00")
                .build();
            
            // Midday: Bloomberg updates one point
            VolatilitySurface midday = morning.withVolatility(100.0, june, 0.28);
            
            // Verify
            assertEquals(0.25, morning.getVolatility(100.0, june), 0.0001);
            assertEquals(0.28, midday.getVolatility(100.0, june), 0.0001);
            
            // Both surfaces exist independently
            assertNotEquals(morning, midday);
        }
    }
}
 
