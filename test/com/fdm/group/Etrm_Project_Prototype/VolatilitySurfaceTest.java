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
 */
@DisplayName("VolatilitySurface Tests")
class VolatilitySurfaceTest {
    
    private LocalDate valuationDate;
    private VolatilitySurface surface;
    
    @BeforeEach
    void setUp() {
        valuationDate = LocalDate.of(2026, 3, 21);
        surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
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
            VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            
            // Assert
            assertNotNull(surface);
            assertEquals("CRUDE_OIL", surface.getCommodity());
            assertEquals(valuationDate, surface.getValuationDate());
        }
        
        @Test
        @DisplayName("Should throw exception for null commodity")
        void testConstructor_NullCommodity() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                new VolatilitySurface(null, valuationDate);
            });
        }
        
        @Test
        @DisplayName("Should throw exception for null valuation date")
        void testConstructor_NullValuationDate() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                new VolatilitySurface("CRUDE_OIL", null);
            });
        }
        
        @Test
        @DisplayName("Should create empty surface")
        void testConstructor_EmptySurface() {
            // Arrange & Act
            VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            
            // Assert - initially empty
            assertNotNull(surface);
        }
    }
    
    // ============================================
    // Add Volatility Tests
    // ============================================
    
    @Nested
    @DisplayName("Add Volatility Tests")
    class AddVolatilityTests {
        
        @Test
        @DisplayName("Should add single volatility point")
        void testAddVolatility_SinglePoint() {
            // Arrange
            VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            
            // Act
            surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25);
            
            // Assert
            double vol = surface.getVolatility(100.0, LocalDate.of(2026, 6, 19));
            assertEquals(0.25, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should add multiple volatility points")
        void testAddVolatility_MultiplePoints() {
            // Arrange
            VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            
            // Act
            surface.addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.30);
            surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25);
            surface.addVolatility(120.0, LocalDate.of(2026, 6, 19), 0.28);
            
            // Assert
            assertEquals(0.30, surface.getVolatility(80.0, LocalDate.of(2026, 6, 19)), 0.0001);
            assertEquals(0.25, surface.getVolatility(100.0, LocalDate.of(2026, 6, 19)), 0.0001);
            assertEquals(0.28, surface.getVolatility(120.0, LocalDate.of(2026, 6, 19)), 0.0001);
        }
        
        @Test
        @DisplayName("Should throw exception for negative volatility")
        void testAddVolatility_NegativeVolatility() {
            // Arrange
            VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), -0.25);
            });
        }
        
        @Test
        @DisplayName("Should throw exception for volatility > 10.0 (1000%)")
        void testAddVolatility_VolatilityTooHigh() {
            // Arrange
            VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 15.0);
            });
        }
        
        @Test
        @DisplayName("Should accept zero volatility")
        void testAddVolatility_ZeroVolatility() {
            // Arrange
            VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.0);
            });
        }
        
        @Test
        @DisplayName("Should accept volatility of 1.0 (100%)")
        void testAddVolatility_MaxVolatility() {
            // Arrange
            VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            
            // Act & Assert
            assertDoesNotThrow(() -> {
                surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 1.0);
            });
        }
        
        @Test
        @DisplayName("Should overwrite existing volatility at same point")
        void testAddVolatility_Overwrite() {
            // Arrange
            VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25);
            
            // Act - add different volatility at same point
            surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.30);
            
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
            surface.addVolatility(100.0, expiry, 0.25);
            
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
            
            surface.addVolatility(80.0, expiry1, 0.30);
            surface.addVolatility(100.0, expiry1, 0.25);
            surface.addVolatility(80.0, expiry2, 0.28);
            
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
        @DisplayName("Should interpolate in strike dimension only(bilinear test with flat time profile")
        void testInterpolation_StrikeOnly() {
        	 // Arrange - 4 points where vol varies only by strike, not time
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            // Vol varies by strike but SAME across time
            surface.addVolatility(80.0, expiry1, 0.30);   // Strike 80: 0.30 at both times
            surface.addVolatility(80.0, expiry2, 0.30);
            surface.addVolatility(100.0, expiry1, 0.26);  // Strike 100: 0.26 at both times
            surface.addVolatility(100.0, expiry2, 0.26);
            
            // Act - Query at strike=90 (midpoint), any time
            LocalDate midExpiry = LocalDate.of(2026, 7, 19);
            double vol = surface.getVolatility(90.0, midExpiry);
            
            // Assert - Should be midpoint of 0.30 and 0.26
            assertEquals(0.28, vol, 0.01);  // Allow small tolerance for time interpolation
        }
        
        @Test
        @DisplayName("Should interpolate in time dimension only(bilinear with flat strike profile")
        void testInterpolation_TimeOnly() {
            // Arrange
        	// Arrange - 4 points where vol varies only by time
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            surface.addVolatility(80.0, expiry1, 0.30);   // Jun: 0.30 at both strikes
            surface.addVolatility(120.0, expiry1, 0.30);  
            surface.addVolatility(80.0, expiry2, 0.26);   // Sep: 0.26 at both strikes
            surface.addVolatility(120.0, expiry2, 0.26);
            
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
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);  // Jun 19
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);  // Sep 18
            
            surface.addVolatility(80.0, expiry1, 0.30);   // Lower-left
            surface.addVolatility(100.0, expiry1, 0.28);  // Lower-right
            surface.addVolatility(80.0, expiry2, 0.28);   // Upper-left
            surface.addVolatility(100.0, expiry2, 0.26);  // Upper-right
            
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
            
            surface.addVolatility(70.0, expiry1, 0.35);
            surface.addVolatility(120.0, expiry1, 0.28);
            surface.addVolatility(70.0, expiry2, 0.32);
            surface.addVolatility(120.0, expiry2, 0.25);
            
            // Act
            LocalDate midExpiry = LocalDate.of(2026, 9, 15);
            double vol = surface.getVolatility(95.0, midExpiry);
            
            // Assert
            assertTrue(vol >= 0.25 && vol <= 0.35, 
                "Vol should be between 0.25 and 0.35, but was " + vol);
        }
        
        @Test
        @DisplayName("Should interpolate at quarter points correctly(25% and 75% positions)")
        void testInterpolation_QuarterPoints() {
        	// Arrange - 4 points with flat time profile
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            // Same volatilities at both times (flat time)
            surface.addVolatility(80.0, expiry1, 0.32);
            surface.addVolatility(80.0, expiry2, 0.32);   // Same vol at different time
            surface.addVolatility(100.0, expiry1, 0.24);
            surface.addVolatility(100.0, expiry2, 0.24);  // Same vol at different time
            
            // Act - Query at 25% and 75% points, same expiry
            double vol85 = surface.getVolatility(85.0, expiry1);  // 25% point
            double vol95 = surface.getVolatility(95.0, expiry1);  // 75% point
            
            // Assert
            // Linear interpolation: vol = 0.32 + (0.24-0.32) * (strike-80) / (100-80)
            // At strike=85: vol = 0.32 + (-0.08) * 5/20 = 0.32 - 0.02 = 0.30
            assertEquals(0.30, vol85, 0.0001);
            // At strike=95: vol = 0.32 + (-0.08) * 15/20 = 0.32 - 0.06 = 0.26
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
            surface.addVolatility(80.0, expiry, 0.30);
            surface.addVolatility(100.0, expiry, 0.25);
            
            // Act - strike 120 is outside grid (max is 100)
            double vol = surface.getVolatility(120.0, expiry);
            
            // Assert - should use nearest point (100, which has vol=0.25)
            assertEquals(0.25, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should use nearest neighbor when strike is below grid")
        void testNearestNeighbor_StrikeBelowGrid() {
            // Arrange
            LocalDate expiry = LocalDate.of(2026, 6, 19);
            surface.addVolatility(80.0, expiry, 0.30);
            surface.addVolatility(100.0, expiry, 0.25);
            
            // Act - strike 60 is outside grid (min is 80)
            double vol = surface.getVolatility(60.0, expiry);
            
            // Assert - should use nearest point (80, which has vol=0.30)
            assertEquals(0.30, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should use nearest neighbor when expiry is after grid")
        void testNearestNeighbor_ExpiryAfterGrid() {
            // Arrange
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            surface.addVolatility(100.0, expiry1, 0.30);
            surface.addVolatility(100.0, expiry2, 0.26);
            
            // Act - Dec 31 is after Sep 18 (outside grid)
            LocalDate futureExpiry = LocalDate.of(2026, 12, 31);
            double vol = surface.getVolatility(100.0, futureExpiry);
            
            // Assert - should use nearest point (Sep 18, which has vol=0.26)
            assertEquals(0.26, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should use nearest neighbor when expiry is before grid")
        void testNearestNeighbor_ExpiryBeforeGrid() {
            // Arrange
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            surface.addVolatility(100.0, expiry1, 0.30);
            surface.addVolatility(100.0, expiry2, 0.26);
            
            // Act - April is before June (outside grid)
            LocalDate pastExpiry = LocalDate.of(2026, 4, 15);
            double vol = surface.getVolatility(100.0, pastExpiry);
            
            // Assert - should use nearest point (Jun 19, which has vol=0.30)
            assertEquals(0.30, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should return default when surface is empty")
        void testNearestNeighbor_EmptySurface() {
            // Arrange
            VolatilitySurface emptySurface = new VolatilitySurface("CRUDE_OIL", valuationDate);
            
            // Act
            double vol = emptySurface.getVolatility(100.0, LocalDate.of(2026, 6, 19));
            
            // Assert - should return default (0.25 = 25%)
            assertEquals(0.25, vol, 0.0001);
        }
        
        @Test
        @DisplayName("Should find nearest when only one point exists")
        void testNearestNeighbor_SinglePoint() {
            // Arrange
            surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.28);
            
            // Act - any query should return this point's volatility
            double vol1 = surface.getVolatility(80.0, LocalDate.of(2026, 5, 1));
            double vol2 = surface.getVolatility(120.0, LocalDate.of(2026, 9, 1));
            
            // Assert
            assertEquals(0.28, vol1, 0.0001);
            assertEquals(0.28, vol2, 0.0001);
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
            surface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.0001);
            
            // Act & Assert
            assertEquals(0.0001, surface.getVolatility(100.0, LocalDate.of(2026, 6, 19)), 0.00001);
        }
        
        @Test
        @DisplayName("Should handle strikes very close together")
        void testEdgeCase_CloseStrikes() {
        	
        	System.out.println("Very close strikes test");
        	// Arrange
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            surface.addVolatility(100.0, expiry1, 0.25);
            surface.addVolatility(100.0, expiry2, 0.25);
            surface.addVolatility(100.01, expiry1, 0.26);
            surface.addVolatility(100.01, expiry2, 0.26);
            
            // Act
            double vol = surface.getVolatility(100.005, expiry1);
            
            // Assert
            assertTrue(vol >= 0.25 && vol <= 0.26, "got vol=" + vol);
            
            // More precise check: should be close to midpoint
            assertEquals(0.255, vol, 0.001);  // 0.255 is midpoint between 0.25 and 0.26
        }
        
        @Test
        @DisplayName("Should handle expiries very close together")
        void testEdgeCase_CloseExpiries() {
        	System.out.println("Very close expires test");
        	System.out.println();
        	// Arrange - 4 points with flat strike profile
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 6, 20);  // 1 day later
            
            // Flat strike profile (same vol at different strikes)
            surface.addVolatility(90.0, expiry1, 0.25);
            surface.addVolatility(110.0, expiry1, 0.25);
            surface.addVolatility(90.0, expiry2, 0.26);
            surface.addVolatility(110.0, expiry2, 0.26);
            
            // Act - Interpolate between very close dates
            double vol = surface.getVolatility(100.0, expiry1);
            
            // Assert - Should interpolate correctly even with 1-day spacing
            assertTrue(vol >= 0.25 && vol <= 0.26,
                "Should handle 1-day expiry spacing, got vol=" + vol);
        }
        
        @Test
        @DisplayName("Should handle large volatility surface")
        void testEdgeCase_LargeSurface() {
            // Arrange - add many points
            for (int strike = 60; strike <= 140; strike += 10) {
                for (int month = 6; month <= 12; month++) {
                    LocalDate expiry = LocalDate.of(2026, month, 15);
                    double vol = 0.20 + (Math.random() * 0.20);  // 20% to 40%
                    surface.addVolatility(strike, expiry, vol);
                }
            }
            
            // Act - interpolate in middle
            double vol = surface.getVolatility(95.0, LocalDate.of(2026, 8, 15));
            
            // Assert
            assertTrue(vol >= 0.20 && vol <= 0.40);
        }
        
        @Test
        @DisplayName("Should handle volatility smile pattern, no interpolation")
        void testEdgeCase_VolatilitySmile() {
            // Arrange - typical smile: higher vol at wings
            LocalDate expiry = LocalDate.of(2026, 6, 19);
            surface.addVolatility(70.0, expiry, 0.35);   // OTM put
            surface.addVolatility(80.0, expiry, 0.28);
            surface.addVolatility(90.0, expiry, 0.25);
            surface.addVolatility(100.0, expiry, 0.24);  // ATM (lowest vol)
            surface.addVolatility(110.0, expiry, 0.26);
            surface.addVolatility(120.0, expiry, 0.30);  // OTM call
            
            // Act
            double volATM = surface.getVolatility(100.0, expiry);
            double volOTMPut = surface.getVolatility(70.0, expiry);
            double volOTMCall = surface.getVolatility(120.0, expiry);
            
            // Assert - smile: wings higher than ATM
            assertTrue(volOTMPut > volATM);
            assertTrue(volOTMCall > volATM);
        }
        
        @Test
        @DisplayName("Should handle volatility smile pattern with interpolation")
        void testEdgeCase_VolatilitySmile_Interpolation() {
            // Arrange - Create smile at two different expiries
            LocalDate expiry1 = LocalDate.of(2026, 6, 19);
            LocalDate expiry2 = LocalDate.of(2026, 9, 18);
            
            // Smile at Jun
            surface.addVolatility(70.0, expiry1, 0.35);
            surface.addVolatility(80.0, expiry1, 0.28);
            surface.addVolatility(100.0, expiry1, 0.24);  // ATM
            surface.addVolatility(110.0, expiry1, 0.26);
            surface.addVolatility(120.0, expiry1, 0.30);
            
            // Flatter smile at Sep
            surface.addVolatility(70.0, expiry2, 0.32);
            surface.addVolatility(80.0, expiry2, 0.27);
            surface.addVolatility(100.0, expiry2, 0.23);  // ATM
            surface.addVolatility(110.0, expiry2, 0.25);
            surface.addVolatility(120.0, expiry2, 0.28);
            
            // Act - Test exact matches
            double volATM = surface.getVolatility(100.0, expiry1);
            double volOTMPut = surface.getVolatility(70.0, expiry1);
            double volOTMCall = surface.getVolatility(120.0, expiry1);
            
            // Assert - Smile pattern holds
            assertTrue(volOTMPut > volATM, "Put wing should be higher than ATM");
            assertTrue(volOTMCall > volATM, "Call wing should be higher than ATM");
            
            // Additional test: Interpolate in the smile
            double vol90 = surface.getVolatility(90.0, expiry1);  // Between 80 and 100
            assertTrue(vol90 > volATM && vol90 < 0.28, 
                "Vol at 90 should be between ATM (0.24) and strike 80 (0.28)");
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
    	        // Arrange - build realistic surface
    	        VolatilitySurface testSurface = new VolatilitySurface("CRUDE_OIL", valuationDate);
    	        
    	        // Add June expiry (3 months) - Volatility smile pattern
    	        testSurface.addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.30);
    	        testSurface.addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.26);
    	        testSurface.addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25);  // ATM
    	        testSurface.addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.27);
    	        
    	        // Add September expiry (6 months) - Flatter smile
    	        testSurface.addVolatility(80.0, LocalDate.of(2026, 9, 18), 0.28);
    	        testSurface.addVolatility(90.0, LocalDate.of(2026, 9, 18), 0.25);
    	        testSurface.addVolatility(100.0, LocalDate.of(2026, 9, 18), 0.24);  // ATM
    	        testSurface.addVolatility(110.0, LocalDate.of(2026, 9, 18), 0.25);
    	        
    	        // Act - Test various query types
    	        
    	        // 1. Exact match (no interpolation)
    	        double exactMatch = testSurface.getVolatility(100.0, LocalDate.of(2026, 6, 19));
    	        System.out.println("exactMatch no interploation" + exactMatch);
    	        
    	        // 2. Strike interpolation only (same expiry)
    	        double strikeInterp = testSurface.getVolatility(95.0, LocalDate.of(2026, 6, 19));
    	        System.out.println("Strike interpolation only" + strikeInterp);
    	        
    	        // 3. Time interpolation only (same strike)
    	        double timeInterp = testSurface.getVolatility(100.0, LocalDate.of(2026, 8, 1));
    	        System.out.println("Time interpolation only" + timeInterp);
    	        
    	        // 4. Bilinear interpolation (both dimensions)
    	        double bilinearInterp = testSurface.getVolatility(95.0, LocalDate.of(2026, 8, 1));
    	        System.out.println("bilinear interpolation" + bilinearInterp);
    	        
    	        // 5. Nearest neighbor fallback (outside grid)
    	        double nearestNeighbor = testSurface.getVolatility(120.0, LocalDate.of(2026, 12, 31));
    	        System.out.println("Nearest neighbor fallback" + nearestNeighbor);
    	        
    	        // Assert - Verify each query type
    	        
    	        // Exact match should return exact value
    	        assertEquals(0.25, exactMatch, 0.0001, 
    	            "Exact match should return calibrated value");
    	        
    	        // Strike interpolation: between 90 (0.26) and 100 (0.25) at Jun
    	        assertTrue(strikeInterp >= 0.25 && strikeInterp <= 0.26,
    	            "Strike interp should be between 0.25 and 0.26, got: " + strikeInterp);
    	        assertEquals(0.255, strikeInterp, 0.001,
    	            "At strike 95 (midpoint), vol should be ~0.255");
    	        
    	        // Time interpolation: between Jun (0.25) and Sep (0.24) at strike 100
    	        assertTrue(timeInterp >= 0.24 && timeInterp <= 0.25,
    	            "Time interp should be between 0.24 and 0.25, got: " + timeInterp);
    	        
    	        // Bilinear: should be in overall range
    	        assertTrue(bilinearInterp >= 0.24 && bilinearInterp <= 0.27,
    	            "Bilinear interp should be in range [0.24, 0.27], got: " + bilinearInterp);
    	        
    	        // Nearest neighbor: should return one of the calibrated vols
    	        assertNotNull(nearestNeighbor);
    	        assertTrue(nearestNeighbor >= 0.24 && nearestNeighbor <= 0.30,
    	            "Nearest neighbor should be within calibrated range, got: " + nearestNeighbor);
    	    }
    }
}