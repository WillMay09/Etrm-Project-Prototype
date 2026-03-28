package com.fdm.group.Etrm_Project_Prototype;

 
import org.junit.jupiter.api.Test;


import com.fdm.group.Etrm_Project_Prototype.VolatilitySurface.VolatilityPoint;

import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

 
import static org.junit.jupiter.api.Assertions.*;
 
/**
 * Unit tests for VolatilityPoint class
 */
@DisplayName("VolatilityPoint Tests")
class VolatilityPointTest {
    
    @Test
    @DisplayName("Should create VolatilityPoint with valid inputs")
    void testConstructor_ValidInputs() {
        // Arrange
        double strike = 100.0;
        LocalDate expiry = LocalDate.of(2026, 6, 19);
        
        // Act
        VolatilityPoint point = new VolatilityPoint(strike, expiry);
        
        // Assert
        assertEquals(100.0, point.getStrike());
        assertEquals(LocalDate.of(2026, 6, 19), point.getExpiry());
    }
    
    @Test
    @DisplayName("Should throw exception when strike is zero")
    void testConstructor_ZeroStrike() {
        // Arrange
        double strike = 0.0;
        LocalDate expiry = LocalDate.of(2026, 6, 19);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new VolatilityPoint(strike, expiry);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when strike is negative")
    void testConstructor_NegativeStrike() {
        // Arrange
        double strike = -50.0;
        LocalDate expiry = LocalDate.of(2026, 6, 19);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new VolatilityPoint(strike, expiry);
        });
    }
    
    @Test
    @DisplayName("Should throw exception when expiry is null")
    void testConstructor_NullExpiry() {
        // Arrange
        double strike = 100.0;
        LocalDate expiry = null;
        
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            new VolatilityPoint(strike, expiry);
        });
    }
    
    @Test
    @DisplayName("Two points with same strike and expiry should be equal")
    void testEquals_SameValues() {
        // Arrange
        VolatilityPoint point1 = new VolatilityPoint(100.0, LocalDate.of(2026, 6, 19));
        VolatilityPoint point2 = new VolatilityPoint(100.0, LocalDate.of(2026, 6, 19));
        
        // Act & Assert
        assertEquals(point1, point2);
    }
    
    @Test
    @DisplayName("Two points with different strikes should not be equal")
    void testEquals_DifferentStrikes() {
        // Arrange
        VolatilityPoint point1 = new VolatilityPoint(100.0, LocalDate.of(2026, 6, 19));
        VolatilityPoint point2 = new VolatilityPoint(110.0, LocalDate.of(2026, 6, 19));
        
        // Act & Assert
        assertNotEquals(point1, point2);
    }
    
    @Test
    @DisplayName("Two points with different expiries should not be equal")
    void testEquals_DifferentExpiries() {
        // Arrange
        VolatilityPoint point1 = new VolatilityPoint(100.0, LocalDate.of(2026, 6, 19));
        VolatilityPoint point2 = new VolatilityPoint(100.0, LocalDate.of(2026, 9, 18));
        
        // Act & Assert
        assertNotEquals(point1, point2);
    }
    
    @Test
    @DisplayName("Equal points should have same hash code")
    void testHashCode_EqualPoints() {
        // Arrange
        VolatilityPoint point1 = new VolatilityPoint(100.0, LocalDate.of(2026, 6, 19));
        VolatilityPoint point2 = new VolatilityPoint(100.0, LocalDate.of(2026, 6, 19));
        
        // Act & Assert
        assertEquals(point1.hashCode(), point2.hashCode());
    }
    
    @Test
    @DisplayName("Can be used as HashMap key")
    void testHashMapKey() {
        // Arrange
        Map<VolatilityPoint, Double> map = new HashMap<>();
        VolatilityPoint point = new VolatilityPoint(100.0, LocalDate.of(2026, 6, 19));
        
        // Act
        map.put(point, 0.25);
        
        // Assert
        assertEquals(0.25, map.get(point));
        
        // Create identical point - should retrieve same value
        VolatilityPoint samePoint = new VolatilityPoint(100.0, LocalDate.of(2026, 6, 19));
        assertEquals(0.25, map.get(samePoint));
    }
    
    @Test
    @DisplayName("toString should return meaningful representation")
    void testToString() {
        // Arrange
        VolatilityPoint point = new VolatilityPoint(100.0, LocalDate.of(2026, 6, 19));
        
        // Act
        String result = point.toString();
        
        // Assert
        assertTrue(result.contains("100.0"));
        assertTrue(result.contains("2026-06-19"));
    }
    
    @Test
    @DisplayName("Should handle very small strike values")
    void testConstructor_SmallStrike() {
        // Arrange & Act
        VolatilityPoint point = new VolatilityPoint(0.01, LocalDate.of(2026, 6, 19));
        
        // Assert
        assertEquals(0.01, point.getStrike(), 0.0001);
    }
}
 