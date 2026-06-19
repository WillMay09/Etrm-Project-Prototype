package com.fdm.group.Etrm_Project_Prototype;
 
import static org.junit.jupiter.api.Assertions.*;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
 
/**
 * Comprehensive test suite for BlackScholesPricer
 * 
 * Tests include:
 * - Hull's textbook examples (known values)
 * - Put-call parity verification
 * - Edge cases (zero time, zero vol, extreme strikes)
 * - Numerical accuracy
 * - Error function approximation
 */
@DisplayName("BlackScholesPricer Tests")
class BlackScholesPricerTest {
    
    private BlackScholesPricer pricer;
    
    // Standard test parameters
    private double spot;
    private double strike;
    private double timeToExpiry;
    private double volatility;
    private double riskFreeRate;
    
    @BeforeEach
    void setUp() {
        pricer = new BlackScholesPricer();
        
        // Standard ATM option (Hull's typical example)
        spot = 100.0;
        strike = 100.0;
        timeToExpiry = 1.0;  // 1 year
        volatility = 0.20;    // 20%
        riskFreeRate = 0.05;  // 5%
    }
    
    // =========================================================================
    // Call Option Pricing Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Call Option Pricing")
    class CallPricingTests {
        
        @Test
        @DisplayName("Should price ATM call (Hull's textbook example)")
        void testATMCallPrice() {
            double callPrice = pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, volatility);
            
            // Expected from Hull Chapter 15: approximately $10.45
            assertEquals(10.45, callPrice, 0.01, 
                        "ATM call should be approximately $10.45");
        }
        @Test
        @DisplayName("Should price ITM call higher than ATM")
        void testITMCallHigher() {
            double atmCall = pricer.priceCall(100.0, 100.0, timeToExpiry, riskFreeRate, volatility);
            double itmCall = pricer.priceCall(110.0, 100.0, timeToExpiry, riskFreeRate, volatility);
            
            assertTrue(itmCall > atmCall, "ITM call should be worth more than ATM");
            
            // ITM call should be at least intrinsic value
            double intrinsicValue = 110.0 - 100.0;
            assertTrue(itmCall >= intrinsicValue, "ITM call should be >= intrinsic value");
        }
        
        @Test
        @DisplayName("Should price OTM call lower than ATM")
        void testOTMCallLower() {
            double atmCall = pricer.priceCall(100.0, 100.0, timeToExpiry, riskFreeRate, volatility);
            double otmCall = pricer.priceCall(90.0, 100.0, timeToExpiry, riskFreeRate, volatility);
            
            assertTrue(otmCall < atmCall, "OTM call should be worth less than ATM");
        }
        
        @Test
        @DisplayName("Call price increases with volatility")
        void testCallIncreasesWithVol() {
            double call20Vol = pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, 0.20);
            double call40Vol = pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, 0.40);
            
            assertTrue(call40Vol > call20Vol, 
                      "Higher volatility should increase call value");
        }
        
        @Test
        @DisplayName("Call price increases with time to expiry")
        void testCallIncreasesWithTime() {
            double call3Month = pricer.priceCall(spot, strike, 0.25, riskFreeRate, volatility);
            double call1Year = pricer.priceCall(spot, strike, 1.0, riskFreeRate, volatility);
            
            assertTrue(call1Year > call3Month, 
                      "Longer time should increase call value");
        }
        
        @Test
        @DisplayName("Call cannot exceed spot price")
        void testCallBoundedBySpot() {
            double callPrice = pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, volatility);
            
            assertTrue(callPrice <= spot, 
                      "Call price cannot exceed spot price");
        }
        
        @Test
        @DisplayName("Deep ITM call approaches intrinsic value for short time")
        void testDeepITMCallNearExpiry() {
            double deepITMCall = pricer.priceCall(120.0, 100.0, 0.01, riskFreeRate, volatility);
            double intrinsicValue = 20.0;
            
            assertEquals(intrinsicValue, deepITMCall, 0.5, 
                        "Deep ITM call near expiry should be close to intrinsic value");
        }
    }
    
    // =========================================================================
    // Put Option Pricing Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Put Option Pricing")
    class PutPricingTests {
        
        @Test
        @DisplayName("Should price ATM put")
        void testATMPutPrice() {
            double putPrice = pricer.pricePut(spot, strike, timeToExpiry, riskFreeRate, volatility);
            
            // ATM put should be positive
            assertTrue(putPrice > 0, "ATM put should have positive value");
            
            // Should be less than strike (can't be worth more than exercising)
            assertTrue(putPrice < strike, "Put price should be less than strike");
        }
        
        @Test
        @DisplayName("Should price ITM put higher than ATM")
        void testITMPutHigher() {
            double atmPut = pricer.pricePut(100.0, 100.0, timeToExpiry, riskFreeRate, volatility);
            double itmPut = pricer.pricePut(90.0, 100.0, timeToExpiry, riskFreeRate, volatility);
            
            assertTrue(itmPut > atmPut, "ITM put should be worth more than ATM");
            
            // ITM put should be at least intrinsic value
            double intrinsicValue = 100.0 - 90.0;
            assertTrue(itmPut >= intrinsicValue, "ITM put should be >= intrinsic value");
        }
        
        @Test
        @DisplayName("Put price increases with volatility")
        void testPutIncreasesWithVol() {
            double put20Vol = pricer.pricePut(spot, strike, timeToExpiry, riskFreeRate, 0.20);
            double put40Vol = pricer.pricePut(spot, strike, timeToExpiry, riskFreeRate, 0.40);
            
            assertTrue(put40Vol > put20Vol, 
                      "Higher volatility should increase put value");
        }
        
        @Test
        @DisplayName("Deep ITM put approaches intrinsic value for short time")
        void testDeepITMPutNearExpiry() {
            double deepITMPut = pricer.pricePut(80.0, 100.0, 0.01, riskFreeRate, volatility);
            double intrinsicValue = 20.0;
            
            assertEquals(intrinsicValue, deepITMPut, 0.5, 
                        "Deep ITM put near expiry should be close to intrinsic value");
        }
    }
    
    // =========================================================================
    // Put-Call Parity Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Put-Call Parity")
    class PutCallParityTests {
        
        @Test
        @DisplayName("Should satisfy put-call parity for ATM options")
        void testPutCallParityATM() {
            double call = pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, volatility);
            double put = pricer.pricePut(spot, strike, timeToExpiry, riskFreeRate, volatility);
            
            // Put-Call Parity: C - P = S - K*e^(-rT)
            double leftSide = call - put;
            double rightSide = spot - strike * Math.exp(-riskFreeRate * timeToExpiry);
            
            assertEquals(rightSide, leftSide, 0.001, 
                        "Put-call parity should hold for ATM options");
        }
        
        @Test
        @DisplayName("Should satisfy put-call parity for ITM options")
        void testPutCallParityITM() {
            double spotITM = 110.0;
            double call = pricer.priceCall(spotITM, strike, timeToExpiry, riskFreeRate, volatility);
            double put = pricer.pricePut(spotITM, strike, timeToExpiry, riskFreeRate, volatility);
            
            double leftSide = call - put;
            double rightSide = spotITM - strike * Math.exp(-riskFreeRate * timeToExpiry);
            
            assertEquals(rightSide, leftSide, 0.001, 
                        "Put-call parity should hold for ITM options");
        }
        
        @Test
        @DisplayName("Should satisfy put-call parity for OTM options")
        void testPutCallParityOTM() {
            double spotOTM = 90.0;
            double call = pricer.priceCall(spotOTM, strike, timeToExpiry, riskFreeRate, volatility);
            double put = pricer.pricePut(spotOTM, strike, timeToExpiry, riskFreeRate, volatility);
            
            double leftSide = call - put;
            double rightSide = spotOTM - strike * Math.exp(-riskFreeRate * timeToExpiry);
            
            assertEquals(rightSide, leftSide, 0.001, 
                        "Put-call parity should hold for OTM options");
        }
        
        @Test
        @DisplayName("Should satisfy put-call parity across different maturities")
        void testPutCallParityDifferentMaturities() {
            double[] maturities = {0.1, 0.25, 0.5, 1.0, 2.0};
            
            for (double maturity : maturities) {
                double call = pricer.priceCall(spot, strike, maturity, riskFreeRate, volatility);
                double put = pricer.pricePut(spot, strike, maturity, riskFreeRate, volatility);
                
                double leftSide = call - put;
                double rightSide = spot - strike * Math.exp(-riskFreeRate * maturity);
                
                assertEquals(rightSide, leftSide, 0.001, 
                            "Put-call parity should hold for maturity " + maturity);
            }
        }
    }
    
    // =========================================================================
    // Edge Cases and Boundary Conditions
    // =========================================================================
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("At expiry, call should equal intrinsic value")
        void testCallAtExpiry() {
            // ITM at expiry
            double itmCall = pricer.priceCall(110.0, 100.0, 0.0, riskFreeRate, volatility);
            assertEquals(10.0, itmCall, 0.001, "ITM call at expiry should equal intrinsic value");
            
            // OTM at expiry
            double otmCall = pricer.priceCall(90.0, 100.0, 0.0, riskFreeRate, volatility);
            assertEquals(0.0, otmCall, 0.001, "OTM call at expiry should be worthless");
        }
        
        @Test
        @DisplayName("At expiry, put should equal intrinsic value")
        void testPutAtExpiry() {
            // ITM at expiry
            double itmPut = pricer.pricePut(90.0, 100.0, 0.0, riskFreeRate, volatility);
            assertEquals(10.0, itmPut, 0.001, "ITM put at expiry should equal intrinsic value");
            
            // OTM at expiry
            double otmPut = pricer.pricePut(110.0, 100.0, 0.0, riskFreeRate, volatility);
            assertEquals(0.0, otmPut, 0.001, "OTM put at expiry should be worthless");
        }
        
//        @Test
//        @DisplayName("Zero volatility call should be discounted payoff")
//        void testZeroVolatilityCall() {
//            // With zero volatility, outcome is deterministic
//            // ITM call with zero vol
//            double itmCall = pricer.priceCall(110.0, 100.0, 1.0, riskFreeRate, 0.0);
//            double expectedValue = (110.0 - 100.0) * Math.exp(-riskFreeRate * 1.0);
//            
//            assertEquals(expectedValue, itmCall, 0.01, 
//                        "Zero vol ITM call should be discounted intrinsic value");
//            
//            // OTM call with zero vol should be worthless
//            double otmCall = pricer.priceCall(90.0, 100.0, 1.0, riskFreeRate, 0.0);
//            assertEquals(0.0, otmCall, 0.001, 
//                        "Zero vol OTM call should be worthless");
//        }
        
        @Test
        @DisplayName("Very high volatility should increase option value significantly")
        void testHighVolatility() {
            double lowVolCall = pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, 0.10);
            double highVolCall = pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, 1.00);
            
            assertTrue(highVolCall > lowVolCall * 2, 
                      "100% vol should give much higher value than 10% vol");
        }
        
        @Test
        @DisplayName("Negative interest rate should still work")
        void testNegativeInterestRate() {
            // Some markets have negative rates
            double negativeRateCall = pricer.priceCall(spot, strike, timeToExpiry, -0.01, volatility);
            double positiveRateCall = pricer.priceCall(spot, strike, timeToExpiry, 0.05, volatility);
            
            assertNotNull(negativeRateCall);
            assertTrue(negativeRateCall > 0, "Call with negative rate should still have value");
            
            // Negative rate makes call less valuable (reduces forward)
            assertTrue(negativeRateCall < positiveRateCall);
        }
    }
    
    // =========================================================================
    // Normal Distribution Function Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Normal Distribution Functions")
    class NormalDistributionTests {
        
        @Test
        @DisplayName("Normal CDF should give correct values")
        void testNormalCDF() {
            // Test known values
            assertEquals(0.5000, pricer.normalCDF(0.0), 0.0001, "N(0) should be 0.5");
            assertEquals(0.8413, pricer.normalCDF(1.0), 0.0001, "N(1) should be 0.8413");
            assertEquals(0.9772, pricer.normalCDF(2.0), 0.0001, "N(2) should be 0.9772");
            assertEquals(0.1587, pricer.normalCDF(-1.0), 0.0001, "N(-1) should be 0.1587");
            
            // Important values for VaR
            assertEquals(0.9500, pricer.normalCDF(1.645), 0.0001, "N(1.645) should be 0.95");
            assertEquals(0.9900, pricer.normalCDF(2.326), 0.001, "N(2.326) should be 0.99");
        }
        
        @Test
        @DisplayName("Normal PDF should give correct values")
        void testNormalPDF() {
            // PDF at mean should be highest
            double pdfAtZero = pricer.normalPDF(0.0);
            assertEquals(0.3989, pdfAtZero, 0.0001, "PDF at 0 should be ~0.3989");
            
            // PDF should be symmetric
            double pdfAt1 = pricer.normalPDF(1.0);
            double pdfAtNeg1 = pricer.normalPDF(-1.0);
            assertEquals(pdfAt1, pdfAtNeg1, 0.0001, "PDF should be symmetric");
            
            // PDF should decrease as we move away from mean
            assertTrue(pricer.normalPDF(0) > pricer.normalPDF(1));
            assertTrue(pricer.normalPDF(1) > pricer.normalPDF(2));
        }
        
        @Test
        @DisplayName("CDF should be monotonically increasing")
        void testCDFMonotonic() {
            for (double x = -3.0; x < 3.0; x += 0.1) {
                double cdfCurrent = pricer.normalCDF(x);
                double cdfNext = pricer.normalCDF(x + 0.1);
                
                assertTrue(cdfNext >= cdfCurrent, 
                          "CDF should be monotonically increasing");
            }
        }
    }
    
    // =========================================================================
    // d1 and d2 Calculation Tests
    // =========================================================================
    
    @Nested
    @DisplayName("d1 and d2 Calculations")
    class D1D2Tests {
        
        @Test
        @DisplayName("d1 and d2 relationship should be correct")
        void testD1D2Relationship() {
            double d1 = pricer.calculateD1(spot, strike, riskFreeRate, volatility, timeToExpiry);
            double d2 = pricer.calculateD2(spot, strike, riskFreeRate, volatility, timeToExpiry);
            
            double sigmaRootT = volatility * Math.sqrt(timeToExpiry);
            
            assertEquals(d1 - sigmaRootT, d2, 0.0001, 
                        "d2 should equal d1 - σ√T");
        }
        
        @Test
        @DisplayName("For ATM option, d1 should be around σ√T/2")
        void testATMd1() {
            // For ATM (S = K), ln(S/K) = 0
            // So d1 ≈ (r + σ²/2)T / (σ√T)
            double d1 = pricer.calculateD1(spot, strike, riskFreeRate, volatility, timeToExpiry);
            
            double expectedD1 = (riskFreeRate + 0.5 * volatility * volatility) * timeToExpiry / 
                               (volatility * Math.sqrt(timeToExpiry));
            
            assertEquals(expectedD1, d1, 0.0001, "d1 calculation should match formula");
        }
        
        @Test
        @DisplayName("At expiry, d1 should be +/- infinity")
        void testD1AtExpiry() {
            // ITM at expiry
            double d1ITM = pricer.calculateD1(110.0, 100.0, riskFreeRate, volatility, 0.0);
            assertEquals(Double.POSITIVE_INFINITY, d1ITM, "d1 should be +∞ for ITM at expiry");
            
            // OTM at expiry
            double d1OTM = pricer.calculateD1(90.0, 100.0, riskFreeRate, volatility, 0.0);
            assertEquals(Double.NEGATIVE_INFINITY, d1OTM, "d1 should be -∞ for OTM at expiry");
        }
    }
    
    // =========================================================================
    // Real-World Examples
    // =========================================================================
    
    @Nested
    @DisplayName("Real-World Examples")
    class RealWorldExamples {
        
        @Test
        @DisplayName("Crude oil option example")
        void testCrudeOilOption() {
            double crudeSpot = 92.50;
            double crudeStrike = 95.00;
            double crudeTime = 0.25;  // 3 months
            double crudeVol = 0.30;    // 30% (typical for commodities)
            double crudeRate = 0.05;   // 5%
            
            double callPrice = pricer.priceCall(crudeSpot, crudeStrike, crudeTime, crudeRate, crudeVol);
            double putPrice = pricer.pricePut(crudeSpot, crudeStrike, crudeTime, crudeRate, crudeVol);
            
            System.out.println("\n=== Crude Oil Option ===");
            System.out.println("Spot: $" + crudeSpot);
            System.out.println("Strike: $" + crudeStrike);
            System.out.println("Call: $" + String.format("%.2f", callPrice));
            System.out.println("Put: $" + String.format("%.2f", putPrice));
            
            assertTrue(callPrice > 0 && callPrice < crudeSpot);
            assertTrue(putPrice > 0 && putPrice < crudeStrike);
            
            // Verify put-call parity
            double leftSide = callPrice - putPrice;
            double rightSide = crudeSpot - crudeStrike * Math.exp(-crudeRate * crudeTime);
            assertEquals(rightSide, leftSide, 0.001);
        }
        
        @Test
        @DisplayName("Multiple strikes - volatility smile")
        void testMultipleStrikes() {
            double[] strikes = {80, 90, 95, 100, 105, 110, 120};
            
            System.out.println("\n=== Option Prices Across Strikes ===");
            System.out.println("Spot: $" + spot);
            System.out.println("Strike\tCall\tPut");
            
            for (double k : strikes) {
                double call = pricer.priceCall(spot, k, timeToExpiry, riskFreeRate, volatility);
                double put = pricer.pricePut(spot, k, timeToExpiry, riskFreeRate, volatility);
                
                System.out.printf("%.0f\t%.2f\t%.2f%n", k, call, put);
                
                // Verify put-call parity
                double leftSide = call - put;
                double rightSide = spot - k * Math.exp(-riskFreeRate * timeToExpiry);
                assertEquals(rightSide, leftSide, 0.001, 
                            "Parity should hold for strike " + k);
            }
        }
    }
    
    // =========================================================================
    // Performance and Accuracy Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should price 10,000 options quickly")
        void testPricingPerformance() {
            long startTime = System.nanoTime();
            
            for (int i = 0; i < 10000; i++) {
                pricer.priceCall(100.0, 100.0, 1.0, 0.05, 0.20);
            }
            
            long endTime = System.nanoTime();
            double milliseconds = (endTime - startTime) / 1_000_000.0;
            
            System.out.println("\nPriced 10,000 options in " + 
                             String.format("%.2f", milliseconds) + " ms");
            
            assertTrue(milliseconds < 1000, 
                      "Should price 10,000 options in under 1 second");
        }
        
        @Test
        @DisplayName("Results should be consistent across multiple calls")
        void testConsistency() {
            double[] results = new double[100];
            
            for (int i = 0; i < 100; i++) {
                results[i] = pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, volatility);
            }
            
            // All results should be identical
            for (int i = 1; i < 100; i++) {
                assertEquals(results[0], results[i], 0.0000001, 
                            "Pricing should give consistent results");
            }
        }
    }
}
 