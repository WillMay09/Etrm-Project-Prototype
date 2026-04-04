package com.fdm.group.Etrm_Project_Prototype;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import java.time.LocalDate;
 
import static org.junit.jupiter.api.Assertions.*;
 
/**
 * Comprehensive unit tests for VolatilitySurface class
 * Tests cover construction, exact matches, interpolation, nearest neighbor, and edge cases
 * Converted tests from standard class to builder
 */


@DisplayName("VolatilitySurface Tests")
class VolatilitySurfaceTest {
    
    private LocalDate valuationDate;
    
    @BeforeEach
    void setUp() {
        valuationDate = LocalDate.of(2026, 3, 21);
    }
    
    // ============================================
    // Construction Tests
    // ============================================
    
    @Nested
    @DisplayName("Construction Tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("Should create surface with valid inputs")
        void testConstructor_ValidInputs() {
            // Arrange & Act
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.30)
                .addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.26)
                .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25)
                .addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.27)
                .build();
            
            // Assert
            assertNotNull(surface);
            assertEquals("CRUDE_OIL", surface.getCommodity());
            assertEquals(valuationDate, surface.getValuationDate());
        }
        
        @Test
        @DisplayName("Should throw exception for null commodity")
        void testConstructor_NullCommodity() {
            // Act & Assert
            assertThrows(IllegalStateException.class, () -> {
                VolatilitySurface.builder()
                    // .commodity("CRUDE_OIL") ← Missing!
                    .valuationDate(valuationDate)
                    .addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.30)
                    .addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.26)
                    .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25)
                    .addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.27)
                    .build();
            });
        }
        
        @Test
        @DisplayName("Should throw exception for null valuation date")
        void testConstructor_NullValuationDate() {
            // Act & Assert
            assertThrows(IllegalStateException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    // .valuationDate(valuationDate) ← Missing!
                    .addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.30)
                    .addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.26)
                    .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25)
                    .addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.27)
                    .build();
            });
        }
        
        @Test
        @DisplayName("Should throw exception if less than 4 points")
        void testConstructor_InsufficientPoints() {
            // Act & Assert
            assertThrows(IllegalStateException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25)
                    .addVolatility(100.0, LocalDate.of(2026, 9, 18), 0.24)
                    // Only 2 points - need 4!
                    .build();
            });
        }
    }
    
    // ============================================
    // Add Volatility Tests (now in Builder)
    // ============================================
    
    @Nested
    @DisplayName("Add Volatility Tests")
    class AddVolatilityTests {
        
        @Test
        @DisplayName("Should add single volatility point")
        void testAddVolatility_SinglePoint() {
            // Arrange & Act
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25)
                .addVolatility(100.0, LocalDate.of(2026, 9, 18), 0.24)
                .addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.26)
                .addVolatility(90.0, LocalDate.of(2026, 9, 18), 0.25)
                .build();
            
            // Assert
            double vol = surface.getVolatility(100.0, LocalDate.of(2026, 6, 19));
            assertEquals(0.25, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should add multiple volatility points")
        void testAddVolatility_MultiplePoints() {
            // Arrange & Act
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.30)
                .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25)
                .addVolatility(120.0, LocalDate.of(2026, 6, 19), 0.28)
                .addVolatility(80.0, LocalDate.of(2026, 9, 18), 0.28)
                .build();
            
            // Assert
            assertEquals(0.30, surface.getVolatility(80.0, LocalDate.of(2026, 6, 19)), 0.0001);
            assertEquals(0.25, surface.getVolatility(100.0, LocalDate.of(2026, 6, 19)), 0.0001);
            assertEquals(0.28, surface.getVolatility(120.0, LocalDate.of(2026, 6, 19)), 0.0001);
        }
        
        @Test
        @DisplayName("Should throw exception for negative volatility")
        void testAddVolatility_NegativeVolatility() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(100.0, LocalDate.of(2026, 6, 19), -0.25);
            });
        }
        
        @Test
        @DisplayName("Should throw exception for volatility > 10.0 (1000%)")
        void testAddVolatility_VolatilityTooHigh() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(100.0, LocalDate.of(2026, 6, 19), 15.0);
            });
        }
        
        @Test
        @DisplayName("Should accept zero volatility")
        void testAddVolatility_ZeroVolatility() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.0)
                    .addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.0)
                    .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.0)
                    .addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.0)
                    .build();
            });
        }
        
        @Test
        @DisplayName("Should accept volatility of 1.0 (100%)")
        void testAddVolatility_MaxVolatility() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                VolatilitySurface.builder()
                    .commodity("CRUDE_OIL")
                    .valuationDate(valuationDate)
                    .addVolatility(80.0, LocalDate.of(2026, 6, 19), 1.0)
                    .addVolatility(90.0, LocalDate.of(2026, 6, 19), 1.0)
                    .addVolatility(100.0, LocalDate.of(2026, 6, 19), 1.0)
                    .addVolatility(110.0, LocalDate.of(2026, 6, 19), 1.0)
                    .build();
            });
        }
        
        @Test
        @DisplayName("Should overwrite existing volatility at same point")
        void testAddVolatility_Overwrite() {
            // Arrange & Act
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25)
                .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.30)  // Overwrites previous
                .addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.26)
                .addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.27)
                .addVolatility(80.0, LocalDate.of(2026, 9, 18), 0.28)
                .build();
            
            // Assert - should have new value
            assertEquals(0.30, surface.getVolatility(100.0, LocalDate.of(2026, 6, 19)), 0.0001);
        }
    }
    
    // ============================================
    // Exact Match Tests
    // ============================================
    
    @Nested
    @DisplayName("Exact Match Tests")
    class ExactMatchTests {
        
        @Test
        @DisplayName("Should return exact volatility when point exists")
        void testGetVolatility_ExactMatch() {
            // Arrange
            LocalDate expiry = LocalDate.of(2026, 6, 19);
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, expiry, 0.30)
                .addVolatility(90.0, expiry, 0.26)
                .addVolatility(100.0, expiry, 0.25)
                .addVolatility(110.0, expiry, 0.27)
                .build();
            
            // Act
            double vol = surface.getVolatility(100.0, expiry);
            
            // Assert
            assertEquals(0.25, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should return correct volatility for multiple exact matches")
        void testGetVolatility_MultipleExactMatches() {
            // Arrange
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, expiry1, 0.30)
                .addVolatility(100.0, expiry1, 0.25)
                .addVolatility(80.0, expiry2, 0.28)
                .addVolatility(100.0, expiry2, 0.24)
                .build();
            
            // Act & Assert
            assertEquals(0.30, surface.getVolatility(80.0, expiry1), 0.0001);
            assertEquals(0.25, surface.getVolatility(100.0, expiry1), 0.0001);
            assertEquals(0.28, surface.getVolatility(80.0, expiry2), 0.0001);
        }
    }
    
    // ============================================
    // Bilinear Interpolation Tests
    // ============================================
    
    @Nested
    @DisplayName("Bilinear Interpolation Tests")
    class BilinearInterpolationTests {
        
        @Test
        @DisplayName("Should interpolate in strike dimension only (bilinear test with flat time profile)")
        void testInterpolation_StrikeOnly() {
            // Arrange - 4 points where vol varies only by strike, not time
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, expiry1, 0.30)   // Strike 80: 0.30 at both times
                .addVolatility(80.0, expiry2, 0.30)
                .addVolatility(100.0, expiry1, 0.26)  // Strike 100: 0.26 at both times
                .addVolatility(100.0, expiry2, 0.26)
                .build();
            
            // Act - Query at strike=90 (midpoint), any time
            LocalDate midExpiry = LocalDate.of(2026, 7, 19);
            double vol = surface.getVolatility(90.0, midExpiry);
            
            // Assert - Should be midpoint of 0.30 and 0.26
            assertEquals(0.28, vol, 0.01);
        }
        
        @Test
        @DisplayName("Should interpolate in time dimension only (bilinear with flat strike profile)")
        void testInterpolation_TimeOnly() {
            // Arrange - 4 points where vol varies only by time
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, expiry1, 0.30)   // Jun: 0.30 at both strikes
                .addVolatility(120.0, expiry1, 0.30)
                .addVolatility(80.0, expiry2, 0.26)   // Sep: 0.26 at both strikes
                .addVolatility(120.0, expiry2, 0.26)
                .build();
            
            // Act - Query at strike=100, time=July
            LocalDate midExpiry = LocalDate.of(2026, 7, 19);
            double vol = surface.getVolatility(100.0, midExpiry);
            
            // Assert
            assertTrue(vol > 0.26 && vol < 0.30);
        }
        
        @Test
        @DisplayName("Should interpolate in both dimensions (bilinear)")
        void testInterpolation_Bilinear() {
            // Arrange - Create 2x2 grid
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, expiry1, 0.30)   // Lower-left
                .addVolatility(100.0, expiry1, 0.28)  // Lower-right
                .addVolatility(80.0, expiry2, 0.28)   // Upper-left
                .addVolatility(100.0, expiry2, 0.26)  // Upper-right
                .build();
            
            // Act - interpolate at center of grid (strike=90, Aug 1)
            LocalDate midExpiry = LocalDate.of(2026, 8, 1);
            double vol = surface.getVolatility(90.0, midExpiry);
            
            // Assert - should be between min and max
            assertTrue(vol >= 0.26 && vol <= 0.30, 
                "Vol should be between 0.26 and 0.30, but was " + vol);
        }
        
        @Test
        @DisplayName("Should handle asymmetric grid interpolation")
        void testInterpolation_AsymmetricGrid() {
            // Arrange - Non-uniform spacing
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 12, 31);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(70.0, expiry1, 0.35)
                .addVolatility(120.0, expiry1, 0.28)
                .addVolatility(70.0, expiry2, 0.32)
                .addVolatility(120.0, expiry2, 0.25)
                .build();
            
            // Act
            LocalDate midExpiry = LocalDate.of(2026, 9, 15);
            double vol = surface.getVolatility(95.0, midExpiry);
            
            // Assert
            assertTrue(vol >= 0.25 && vol <= 0.35, 
                "Vol should be between 0.25 and 0.35, but was " + vol);
        }
        
        @Test
        @DisplayName("Should interpolate at quarter points correctly (25% and 75% positions)")
        void testInterpolation_QuarterPoints() {
            // Arrange - 4 points with flat time profile
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, expiry1, 0.32)
                .addVolatility(80.0, expiry2, 0.32)
                .addVolatility(100.0, expiry1, 0.24)
                .addVolatility(100.0, expiry2, 0.24)
                .build();
            
            // Act - Query at 25% and 75% points
            double vol85 = surface.getVolatility(85.0, expiry1);
            double vol95 = surface.getVolatility(95.0, expiry1);
            
            // Assert
            assertEquals(0.30, vol85, 0.0001);
            assertEquals(0.26, vol95, 0.0001);
        }
    }
    
    // ============================================
    // Nearest Neighbor Tests
    // ============================================
    
    @Nested
    @DisplayName("Nearest Neighbor Tests")
    class NearestNeighborTests {
        
        @Test
        @DisplayName("Should use nearest neighbor when strike is above grid")
        void testNearestNeighbor_StrikeAboveGrid() {
            // Arrange
            LocalDate expiry = LocalDate.of(2026, 6, 19);
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, expiry, 0.30)
                .addVolatility(100.0, expiry, 0.25)
                .addVolatility(80.0, LocalDate.of(2026, 9, 18), 0.28)  // ← Add 2 more
                .addVolatility(100.0, LocalDate.of(2026, 9, 18), 0.24) // ← points
                .build();
            
            // Act - strike 120 is outside grid (max is 100)
            double vol = surface.getVolatility(120.0, expiry);
            
            // Assert - should use nearest point (100, expiry) = 0.25
            assertEquals(0.25, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should use nearest neighbor when strike is below grid")
        void testNearestNeighbor_StrikeBelowGrid() {
            // Arrange
            LocalDate expiry = LocalDate.of(2026, 6, 19);
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, expiry, 0.30)
                .addVolatility(100.0, expiry, 0.25)
                .addVolatility(80.0, LocalDate.of(2026, 9, 18), 0.28)  // ← Add 2 more
                .addVolatility(100.0, LocalDate.of(2026, 9, 18), 0.24) // ← points
                .build();
            
            // Act - strike 60 is outside grid (min is 80)
            double vol = surface.getVolatility(60.0, expiry);
            
            // Assert - should use nearest point (80, expiry) = 0.30
            assertEquals(0.30, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should use nearest neighbor when expiry is after grid")
        void testNearestNeighbor_ExpiryAfterGrid() {
            // Arrange
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(100.0, expiry1, 0.30)
                .addVolatility(100.0, expiry2, 0.26)
                .addVolatility(110.0, expiry1, 0.28)  // ← Add 2 more points
                .addVolatility(110.0, expiry2, 0.25)  // ← with different strike
                .build();
            
            // Act - Dec 31 is after Sep 18 (outside grid)
            LocalDate futureExpiry = LocalDate.of(2026, 12, 31);
            double vol = surface.getVolatility(100.0, futureExpiry);
            
            // Assert - should use nearest point (100, Sep 18) = 0.26
            assertEquals(0.26, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should use nearest neighbor when expiry is before grid")
        void testNearestNeighbor_ExpiryBeforeGrid() {
            // Arrange
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(100.0, expiry1, 0.30)
                .addVolatility(100.0, expiry2, 0.26)
                .addVolatility(110.0, expiry1, 0.28)  // ← Add 2 more points
                .addVolatility(110.0, expiry2, 0.25)  // ← with different strike
                .build();
            
            // Act - April is before June (outside grid)
            LocalDate pastExpiry = LocalDate.of(2026, 4, 15);
            double vol = surface.getVolatility(100.0, pastExpiry);
            
            // Assert - should use nearest point (100, June 19) = 0.30
            assertEquals(0.30, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should find nearest when surface has exactly 4 points")
        void testNearestNeighbor_MinimalSurface() {
            // Arrange
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.28)
                .addVolatility(100.0, LocalDate.of(2026, 9, 18), 0.26)
                .addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.27)
                .addVolatility(110.0, LocalDate.of(2026, 9, 18), 0.25)
                .build();
            
            // Act - queries outside grid
            double vol1 = surface.getVolatility(80.0, LocalDate.of(2026, 5, 1));
            double vol2 = surface.getVolatility(120.0, LocalDate.of(2026, 12, 1));
            
            // Assert - should find nearest points
            assertNotNull(vol1);
            assertNotNull(vol2);
            assertTrue(vol1 >= 0.25 && vol1 <= 0.28);
            assertTrue(vol2 >= 0.25 && vol2 <= 0.28);
        }
    }
    
    // ============================================
    // Edge Cases
    // ============================================
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle very small volatility values")
        void testEdgeCase_VerySmallVolatility() {
            // Arrange
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.0001)
                .addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.0001)
                .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.0001)
                .addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.0001)
                .build();
            
            // Act & Assert
            assertEquals(0.0001, surface.getVolatility(100.0, LocalDate.of(2026, 6, 19)), 0.00001);
        }
        
        @Test
        @DisplayName("Should handle strikes very close together")
        void testEdgeCase_CloseStrikes() {
            // Arrange
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(100.0, expiry1, 0.25)
                .addVolatility(100.0, expiry2, 0.25)
                .addVolatility(100.01, expiry1, 0.26)
                .addVolatility(100.01, expiry2, 0.26)
                .build();
            
            // Act
            double vol = surface.getVolatility(100.005, expiry1);
            
            // Assert
            assertTrue(vol >= 0.25 && vol <= 0.26, "got vol=" + vol);
            assertEquals(0.255, vol, 0.001);
        }
        
        @Test
        @DisplayName("Should handle expiries very close together")
        void testEdgeCase_CloseExpiries() {
            // Arrange
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 6, 20);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(90.0, expiry1, 0.25)
                .addVolatility(110.0, expiry1, 0.25)
                .addVolatility(90.0, expiry2, 0.26)
                .addVolatility(110.0, expiry2, 0.26)
                .build();
            
            // Act
            double vol = surface.getVolatility(100.0, expiry1);
            
            // Assert
            assertTrue(vol >= 0.25 && vol <= 0.26,
                "Should handle 1-day expiry spacing, got vol=" + vol);
        }
        
        @Test
        @DisplayName("Should handle large volatility surface")
        void testEdgeCase_LargeSurface() {
            // Arrange
            VolatilitySurface.Builder builder = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate);
            
            for (int strike = 60; strike <= 140; strike += 10) {
                for (int month = 6; month <= 12; month++) {
                    LocalDate expiry = LocalDate.of(2026, month, 15);
                    double vol = 0.20 + (Math.random() * 0.20);
                    builder.addVolatility(strike, expiry, vol);
                }
            }
            
            VolatilitySurface surface = builder.build();
            
            // Act
            double vol = surface.getVolatility(95.0, LocalDate.of(2026, 8, 15));
            
            // Assert
            assertTrue(vol >= 0.20 && vol <= 0.40);
        }
        
        @Test
        @DisplayName("Should handle volatility smile pattern, no interpolation")
        void testEdgeCase_VolatilitySmile() {
            // Arrange - typical smile: higher vol at wings
            LocalDate expiry = LocalDate.of(2026, 6, 19);
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                .addVolatility(70.0, expiry, 0.35)
                .addVolatility(80.0, expiry, 0.28)
                .addVolatility(90.0, expiry, 0.25)
                .addVolatility(100.0, expiry, 0.24)
                .addVolatility(110.0, expiry, 0.26)
                .addVolatility(120.0, expiry, 0.30)
                .build();
            
            // Act
            double volATM = surface.getVolatility(100.0, expiry);
            double volOTMPut = surface.getVolatility(70.0, expiry);
            double volOTMCall = surface.getVolatility(120.0, expiry);
            
            // Assert
            assertTrue(volOTMPut > volATM);
            assertTrue(volOTMCall > volATM);
        }
        
        @Test
        @DisplayName("Should handle volatility smile pattern with interpolation")
        void testEdgeCase_VolatilitySmile_Interpolation() {
            // Arrange
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            VolatilitySurface surface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                // June smile
                .addVolatility(70.0, expiry1, 0.35)
                .addVolatility(80.0, expiry1, 0.28)
                .addVolatility(100.0, expiry1, 0.24)
                .addVolatility(110.0, expiry1, 0.26)
                .addVolatility(120.0, expiry1, 0.30)
                // September smile
                .addVolatility(70.0, expiry2, 0.32)
                .addVolatility(80.0, expiry2, 0.27)
                .addVolatility(100.0, expiry2, 0.23)
                .addVolatility(110.0, expiry2, 0.25)
                .addVolatility(120.0, expiry2, 0.28)
                .build();
            
            // Act
            double volATM = surface.getVolatility(100.0, expiry1);
            double volOTMPut = surface.getVolatility(70.0, expiry1);
            double volOTMCall = surface.getVolatility(120.0, expiry1);
            
            // Assert
            assertTrue(volOTMPut > volATM);
            assertTrue(volOTMCall > volATM);
            
            double vol90 = surface.getVolatility(90.0, expiry1);
            assertTrue(vol90 > volATM && vol90 < 0.28);
        }
    }
    
    // ============================================
    // Integration Tests
    // ============================================
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should handle complete workflow")
        void testIntegration_CompleteWorkflow() {
            // Arrange
            VolatilitySurface testSurface = VolatilitySurface.builder()
                .commodity("CRUDE_OIL")
                .valuationDate(valuationDate)
                // June expiry
                .addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.30)
                .addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.26)
                .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25)
                .addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.27)
                // September expiry
                .addVolatility(80.0, LocalDate.of(2026, 9, 18), 0.28)
                .addVolatility(90.0, LocalDate.of(2026, 9, 18), 0.25)
                .addVolatility(100.0, LocalDate.of(2026, 9, 18), 0.24)
                .addVolatility(110.0, LocalDate.of(2026, 9, 18), 0.25)
                .build();
            
            // Act
            double exactMatch = testSurface.getVolatility(100.0, LocalDate.of(2026, 6, 19));
            double strikeInterp = testSurface.getVolatility(95.0, LocalDate.of(2026, 6, 19));
            double timeInterp = testSurface.getVolatility(100.0, LocalDate.of(2026, 8, 1));
            double bilinearInterp = testSurface.getVolatility(95.0, LocalDate.of(2026, 8, 1));
            double nearestNeighbor = testSurface.getVolatility(120.0, LocalDate.of(2026, 12, 31));
            
            // Assert
            assertEquals(0.25, exactMatch, 0.0001);
            assertTrue(strikeInterp >= 0.25 && strikeInterp <= 0.26);
            assertEquals(0.255, strikeInterp, 0.001);
            assertTrue(timeInterp >= 0.24 && timeInterp <= 0.25);
            assertTrue(bilinearInterp >= 0.24 && bilinearInterp <= 0.27);
            assertNotNull(nearestNeighbor);
            assertTrue(nearestNeighbor >= 0.24 && nearestNeighbor <= 0.30);
        }
    }
}