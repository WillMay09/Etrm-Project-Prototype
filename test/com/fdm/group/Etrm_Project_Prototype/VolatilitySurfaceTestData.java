package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

/**
 * Helper class to create common test data for VolatilitySurface tests
 * Uses Builder pattern for immutable, validated surfaces
 * Makes tests more readable and easier to maintain
 */
public class VolatilitySurfaceTestData {
    
    // Common dates for testing
    public static final LocalDate VALUATION_DATE = LocalDate.of(2026, 3, 21);
    public static final LocalDate JUNE_EXPIRY = LocalDate.of(2026, 6, 19);
    public static final LocalDate SEP_EXPIRY = LocalDate.of(2026, 9, 18);
    public static final LocalDate DEC_EXPIRY = LocalDate.of(2026, 12, 31);
    
    /**
     * Create a simple 2x2 grid surface for basic interpolation testing
     * 
     * Grid structure:
     *     Jun        Sep
     * 80  0.30       0.28
     * 100 0.28       0.26
     */
    public static VolatilitySurface createSimpleGrid() {
        return VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(VALUATION_DATE)
            .addVolatility(80.0, JUNE_EXPIRY, 0.30)
            .addVolatility(100.0, JUNE_EXPIRY, 0.28)
            .addVolatility(80.0, SEP_EXPIRY, 0.28)
            .addVolatility(100.0, SEP_EXPIRY, 0.26)
            .build();
    }
    
    /**
     * Create a surface with realistic market data (volatility smile)
     * Shows typical pattern: higher volatility for OTM options
     */
    public static VolatilitySurface createRealisticSurface() {
        return VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(VALUATION_DATE)
            // June expiry (3 months) - typical smile pattern
            .addVolatility(70.0, JUNE_EXPIRY, 0.35)   // Deep OTM put
            .addVolatility(80.0, JUNE_EXPIRY, 0.30)
            .addVolatility(90.0, JUNE_EXPIRY, 0.26)
            .addVolatility(100.0, JUNE_EXPIRY, 0.25)  // ATM (lowest vol)
            .addVolatility(110.0, JUNE_EXPIRY, 0.27)
            .addVolatility(120.0, JUNE_EXPIRY, 0.30)  // Deep OTM call
            // September expiry (6 months) - flatter smile
            .addVolatility(70.0, SEP_EXPIRY, 0.32)
            .addVolatility(80.0, SEP_EXPIRY, 0.28)
            .addVolatility(90.0, SEP_EXPIRY, 0.25)
            .addVolatility(100.0, SEP_EXPIRY, 0.24)   // ATM
            .addVolatility(110.0, SEP_EXPIRY, 0.25)
            .addVolatility(120.0, SEP_EXPIRY, 0.27)
            // December expiry (9 months) - even flatter
            .addVolatility(70.0, DEC_EXPIRY, 0.30)
            .addVolatility(80.0, DEC_EXPIRY, 0.27)
            .addVolatility(90.0, DEC_EXPIRY, 0.24)
            .addVolatility(100.0, DEC_EXPIRY, 0.23)   // ATM
            .addVolatility(110.0, DEC_EXPIRY, 0.24)
            .addVolatility(120.0, DEC_EXPIRY, 0.26)
            .build();
    }
    
    /**
     * Create a sparse surface (minimal calibrated points)
     * Useful for testing nearest neighbor fallback
     */
    public static VolatilitySurface createSparseSurface() {
        return VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(VALUATION_DATE)
            .addVolatility(80.0, JUNE_EXPIRY, 0.30)
            .addVolatility(120.0, JUNE_EXPIRY, 0.28)
            .addVolatility(80.0, DEC_EXPIRY, 0.26)
            .addVolatility(120.0, DEC_EXPIRY, 0.25)
            .build();
    }
    
    /**
     * Create a surface with only ATM volatilities across time
     * Useful for testing time interpolation
     */
    public static VolatilitySurface createATMCurve() {
        return VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(VALUATION_DATE)
            // All at strike 100 (ATM), different expiries
            // Need at least 4 points - add different strikes at each expiry
            .addVolatility(90.0, JUNE_EXPIRY, 0.30)
            .addVolatility(110.0, JUNE_EXPIRY, 0.30)
            .addVolatility(90.0, SEP_EXPIRY, 0.26)
            .addVolatility(110.0, SEP_EXPIRY, 0.26)
            .addVolatility(90.0, DEC_EXPIRY, 0.24)
            .addVolatility(110.0, DEC_EXPIRY, 0.24)
            .build();
    }
    
    /**
     * Create a surface with same expiry, different strikes
     * Useful for testing strike interpolation
     */
    public static VolatilitySurface createSingleExpirySlice() {
        return VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(VALUATION_DATE)
            // All for June expiry, different strikes
            // Need 4+ points - add second expiry with same vols
            .addVolatility(70.0, JUNE_EXPIRY, 0.35)
            .addVolatility(80.0, JUNE_EXPIRY, 0.30)
            .addVolatility(90.0, JUNE_EXPIRY, 0.26)
            .addVolatility(100.0, JUNE_EXPIRY, 0.25)
            .addVolatility(110.0, JUNE_EXPIRY, 0.27)
            .addVolatility(120.0, JUNE_EXPIRY, 0.30)
            // Add second expiry to meet 4-point minimum
            .addVolatility(70.0, SEP_EXPIRY, 0.35)
            .addVolatility(120.0, SEP_EXPIRY, 0.30)
            .build();
    }
    
    /**
     * Create a flat surface (same volatility everywhere)
     * Useful for testing that interpolation returns consistent values
     */
    public static VolatilitySurface createFlatSurface(double volatility) {
        VolatilitySurface.Builder builder = VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(VALUATION_DATE);
        
        for (double strike = 80; strike <= 120; strike += 20) {
            builder.addVolatility(strike, JUNE_EXPIRY, volatility);
            builder.addVolatility(strike, SEP_EXPIRY, volatility);
            builder.addVolatility(strike, DEC_EXPIRY, volatility);
        }
        
        return builder.build();
    }
    
    /**
     * Create a minimal surface with exactly 4 points (minimum for bilinear interpolation)
     * Useful for testing edge cases
     */
    public static VolatilitySurface createMinimalSurface() {
        return VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(VALUATION_DATE)
            .addVolatility(80.0, JUNE_EXPIRY, 0.30)
            .addVolatility(100.0, JUNE_EXPIRY, 0.28)
            .addVolatility(80.0, SEP_EXPIRY, 0.28)
            .addVolatility(100.0, SEP_EXPIRY, 0.26)
            .build();
    }
    
    /**
     * Create a surface with metadata for testing metadata retrieval
     */
    public static VolatilitySurface createSurfaceWithMetadata() {
        return VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(VALUATION_DATE)
            .addVolatility(80.0, JUNE_EXPIRY, 0.30)
            .addVolatility(100.0, JUNE_EXPIRY, 0.28)
            .addVolatility(80.0, SEP_EXPIRY, 0.28)
            .addVolatility(100.0, SEP_EXPIRY, 0.26)
            .metadata("source", "Bloomberg")
            .metadata("calibrationTime", "2026-03-21T09:30:00")
            .metadata("interpolationType", "BILINEAR")
            .metadata("dayCount", "ACT_365")
            .build();
    }
    
    /**
     * Create a surface with volatility smile pattern at multiple expiries
     * Useful for realistic testing
     */
    public static VolatilitySurface createVolatilitySmileSurface() {
        return VolatilitySurface.builder()
            .commodity("CRUDE_OIL")
            .valuationDate(VALUATION_DATE)
            // June smile (pronounced)
            .addVolatility(70.0, JUNE_EXPIRY, 0.35)
            .addVolatility(80.0, JUNE_EXPIRY, 0.28)
            .addVolatility(90.0, JUNE_EXPIRY, 0.25)
            .addVolatility(100.0, JUNE_EXPIRY, 0.24)  // ATM lowest
            .addVolatility(110.0, JUNE_EXPIRY, 0.26)
            .addVolatility(120.0, JUNE_EXPIRY, 0.30)
            // September smile (flatter)
            .addVolatility(70.0, SEP_EXPIRY, 0.32)
            .addVolatility(80.0, SEP_EXPIRY, 0.27)
            .addVolatility(90.0, SEP_EXPIRY, 0.24)
            .addVolatility(100.0, SEP_EXPIRY, 0.23)   // ATM lowest
            .addVolatility(110.0, SEP_EXPIRY, 0.25)
            .addVolatility(120.0, SEP_EXPIRY, 0.28)
            .build();
    }
}
 