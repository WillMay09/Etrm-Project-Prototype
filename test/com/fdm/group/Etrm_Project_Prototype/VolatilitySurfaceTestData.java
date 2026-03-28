package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

/**
 * Helper class to create common test data for VolatilitySurface tests
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
        VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", VALUATION_DATE);
        
        surface.addVolatility(80.0, JUNE_EXPIRY, 0.30);
        surface.addVolatility(100.0, JUNE_EXPIRY, 0.28);
        surface.addVolatility(80.0, SEP_EXPIRY, 0.28);
        surface.addVolatility(100.0, SEP_EXPIRY, 0.26);
        
        return surface;
    }
    
    /**
     * Create a surface with realistic market data (volatility smile)
     * Shows typical pattern: higher volatility for OTM options
     */
    public static VolatilitySurface createRealisticSurface() {
        VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", VALUATION_DATE);
        
        // June expiry (3 months) - typical smile pattern
        surface.addVolatility(70.0, JUNE_EXPIRY, 0.35);   // Deep OTM put
        surface.addVolatility(80.0, JUNE_EXPIRY, 0.30);
        surface.addVolatility(90.0, JUNE_EXPIRY, 0.26);
        surface.addVolatility(100.0, JUNE_EXPIRY, 0.25);  // ATM (lowest vol)
        surface.addVolatility(110.0, JUNE_EXPIRY, 0.27);
        surface.addVolatility(120.0, JUNE_EXPIRY, 0.30);  // Deep OTM call
        
        // September expiry (6 months) - flatter smile
        surface.addVolatility(70.0, SEP_EXPIRY, 0.32);
        surface.addVolatility(80.0, SEP_EXPIRY, 0.28);
        surface.addVolatility(90.0, SEP_EXPIRY, 0.25);
        surface.addVolatility(100.0, SEP_EXPIRY, 0.24);   // ATM
        surface.addVolatility(110.0, SEP_EXPIRY, 0.25);
        surface.addVolatility(120.0, SEP_EXPIRY, 0.27);
        
        // December expiry (9 months) - even flatter
        surface.addVolatility(70.0, DEC_EXPIRY, 0.30);
        surface.addVolatility(80.0, DEC_EXPIRY, 0.27);
        surface.addVolatility(90.0, DEC_EXPIRY, 0.24);
        surface.addVolatility(100.0, DEC_EXPIRY, 0.23);   // ATM
        surface.addVolatility(110.0, DEC_EXPIRY, 0.24);
        surface.addVolatility(120.0, DEC_EXPIRY, 0.26);
        
        return surface;
    }
    
    /**
     * Create a sparse surface (few calibrated points)
     * Useful for testing nearest neighbor fallback
     */
    public static VolatilitySurface createSparseSurface() {
        VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", VALUATION_DATE);
        
        surface.addVolatility(80.0, JUNE_EXPIRY, 0.30);
        surface.addVolatility(120.0, DEC_EXPIRY, 0.25);
        
        return surface;
    }
    
    /**
     * Create a surface with only ATM volatilities across time
     * Useful for testing time interpolation
     */
    public static VolatilitySurface createATMCurve() {
        VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", VALUATION_DATE);
        
        // All at strike 100 (ATM), different expiries
        surface.addVolatility(100.0, JUNE_EXPIRY, 0.30);
        surface.addVolatility(100.0, SEP_EXPIRY, 0.26);
        surface.addVolatility(100.0, DEC_EXPIRY, 0.24);
        
        return surface;
    }
    
    /**
     * Create a surface with same expiry, different strikes
     * Useful for testing strike interpolation
     */
    public static VolatilitySurface createSingleExpirySlice() {
        VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", VALUATION_DATE);
        
        // All for June expiry, different strikes
        surface.addVolatility(70.0, JUNE_EXPIRY, 0.35);
        surface.addVolatility(80.0, JUNE_EXPIRY, 0.30);
        surface.addVolatility(90.0, JUNE_EXPIRY, 0.26);
        surface.addVolatility(100.0, JUNE_EXPIRY, 0.25);
        surface.addVolatility(110.0, JUNE_EXPIRY, 0.27);
        surface.addVolatility(120.0, JUNE_EXPIRY, 0.30);
        
        return surface;
    }
    
    /**
     * Create an empty surface
     */
    public static VolatilitySurface createEmptySurface() {
        return new VolatilitySurface("CRUDE_OIL", VALUATION_DATE);
    }
    
    /**
     * Create a flat surface (same volatility everywhere)
     * Useful for testing that interpolation returns consistent values
     */
    public static VolatilitySurface createFlatSurface(double volatility) {
        VolatilitySurface surface = new VolatilitySurface("CRUDE_OIL", VALUATION_DATE);
        
        for (double strike = 80; strike <= 120; strike += 20) {
            surface.addVolatility(strike, JUNE_EXPIRY, volatility);
            surface.addVolatility(strike, SEP_EXPIRY, volatility);
            surface.addVolatility(strike, DEC_EXPIRY, volatility);
        }
        
        return surface;
    }
}