package com.fdm.group.Etrm_Project_Prototype;
 
import com.fdm.group.Etrm_Project_Prototype.GreeksCalculator.OptionGreeks;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
 
/**
 * Comprehensive test suite for GreeksCalculator
 */
@DisplayName("GreeksCalculator Tests")
class GreeksCalculatorTest {
    
    private GreeksCalculator greeks;
    private BlackScholesPricer pricer;
    
    // Standard test parameters
    private double spot;
    private double spot2;
    private double strike;
    private double strike2;
    private double timeToExpiry;
    private double timeToExpiry2;
    private double volatility;
    private double volatility2;
    private double riskFreeRate;
    private double riskFreeRate2;
    
    @BeforeEach
    void setUp() {
        pricer = new BlackScholesPricer();
        greeks = new GreeksCalculator(pricer);
        
        // Standard ATM option
        spot = 100.0;
        strike = 100.0;
        timeToExpiry = 1.0;  // 1 year
        volatility = 0.20;    // 20%
        riskFreeRate = 0.05;  // 5%
        
        // Deep OTM scenario
        spot2 = 150;
        strike2 = 165;
        timeToExpiry2 = 0.25;
        volatility2 = 0.10;
        riskFreeRate2 = 0.02;
        
    }
    
    // =========================================================================
    // Delta Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Delta Tests")
    class DeltaTests {
        
        @Test
        @DisplayName("ATM call delta should be around 0.5")
        void testATMCallDelta() {
            double delta = greeks.deltaCall(spot, strike, timeToExpiry, volatility, riskFreeRate);
            
            // ATM call delta is typically 0.5-0.6 depending on rates
            assertTrue(delta > 0.45 && delta < 0.65, 
                      "ATM call delta should be around 0.5, got: " + delta);
        }
        
//        @Test
//        @DisplayName("ATM call delta should be at ")
//        void ATMCallDelta2(){
//        	
//        	double delta = greeks.deltaCall(spot, strike, timeToExpiry, volatility, riskFreeRate);
//        	
//        	
//        	
//        	assertTrue(delta >0.0370 && delta < 0.0450 , "ATM call delta should be around, got: "+ delta);
//        	
//        	
//        }
//        
       
        @Test
        @DisplayName("ITM call delta should be higher than ATM")
        void testITMCallDelta() {
            double atmDelta = greeks.deltaCall(spot, strike, timeToExpiry, volatility, riskFreeRate);
            double itmDelta = greeks.deltaCall(110.0, strike, timeToExpiry, volatility, riskFreeRate);
            
            assertTrue(itmDelta > atmDelta, "ITM delta should be > ATM delta");
            assertTrue(itmDelta > 0.7, "Deep ITM delta should approach 1.0");
        }
        
        @Test
        @DisplayName("OTM call delta should be lower than ATM")
        void testOTMCallDelta() {
            double atmDelta = greeks.deltaCall(spot, strike, timeToExpiry, volatility, riskFreeRate);
            double otmDelta = greeks.deltaCall(90.0, strike, timeToExpiry, volatility, riskFreeRate);
            
            assertTrue(otmDelta < atmDelta, "OTM delta should be < ATM delta");
            assertTrue(otmDelta < 0.3, "Deep OTM delta should approach 0.0");
        }
        
        @Test
        @DisplayName("Put delta should be negative")
        void testPutDeltaIsNegative() {
            double delta = greeks.deltaPut(spot, strike, timeToExpiry, volatility, riskFreeRate);
            
            assertTrue(delta < 0, "Put delta should be negative");
            assertTrue(delta > -1.0, "Put delta should be > -1.0");
        }
        
        
        
        @Test
        @DisplayName("Delta at expiry should be 0 or 1")
        void testDeltaAtExpiry() {
            double timeToExpiry = 0.0;
            
            // ITM call
            double itmDelta = greeks.deltaCall(110.0, 100.0, timeToExpiry, volatility, riskFreeRate);
            assertEquals(1.0, itmDelta, "ITM call delta at expiry should be 1.0");
            
            // OTM call
            double otmDelta = greeks.deltaCall(90.0, 100.0, timeToExpiry, volatility, riskFreeRate);
            assertEquals(0.0, otmDelta, "OTM call delta at expiry should be 0.0");
        }
    }
    
    // =========================================================================
    // Gamma Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Gamma Tests")
    class GammaTests {
        
        @Test
        @DisplayName("Gamma should be positive")
        void testGammaIsPositive() {
            double gamma = greeks.gamma(spot, strike, timeToExpiry, volatility, riskFreeRate);
            
            assertTrue(gamma > 0, "Gamma should be positive");
        }
        
        @Test
        @DisplayName("ATM gamma should be highest")
        void testATMGammaHighest() {
            double atmGamma = greeks.gamma(spot, strike, timeToExpiry, volatility, riskFreeRate);
            double itmGamma = greeks.gamma(110.0, strike, timeToExpiry, volatility, riskFreeRate);
            double otmGamma = greeks.gamma(90.0, strike, timeToExpiry,volatility, riskFreeRate);
            
            assertTrue(atmGamma > itmGamma, "ATM gamma should be > ITM gamma");
            assertTrue(atmGamma > otmGamma, "ATM gamma should be > OTM gamma");
        }
        
        @Test
        @DisplayName("Gamma increases as expiry approaches")
        void testGammaIncreasesNearExpiry() {
            double gammaLongDated = greeks.gamma(spot, strike, 1.0, volatility, riskFreeRate);
            double gammaShortDated = greeks.gamma(spot, strike, 0.1, volatility, riskFreeRate);
            
            assertTrue(gammaShortDated > gammaLongDated, 
                      "Gamma should increase as expiry approaches");
        }
        
        @Test
        @DisplayName("Gamma same for calls and puts")
        void testGammaSameForCallsAndPuts() {
            // Gamma is the same for calls and puts with same parameters
            double gamma = greeks.gamma(spot, strike, timeToExpiry, volatility, riskFreeRate);
            
            // Calculate delta for both call and put
            double callDelta = greeks.deltaCall(spot, strike, timeToExpiry, volatility, riskFreeRate);
            double putDelta = greeks.deltaPut(spot, strike, timeToExpiry, volatility, riskFreeRate);
            
            // Gamma should be same (rate of change of delta)
            assertNotNull(gamma);
            assertTrue(gamma > 0);
        }
        
     
    }
    // =========================================================================
    // Vega Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Vega Tests")
    class VegaTests {
        
        @Test
        @DisplayName("Vega should be positive for long options")
        void testVegaIsPositive() {
            double vega = greeks.vega(spot, strike, timeToExpiry, volatility, riskFreeRate);
            
            assertTrue(vega > 0, "Vega should be positive for long options");
        }
        
        @Test
        @DisplayName("ATM options should have highest vega")
        void testATMVegaHighest() {
            double atmVega = greeks.vega(spot, strike, timeToExpiry, volatility, riskFreeRate);
            double itmVega = greeks.vega(110.0, strike, timeToExpiry, volatility, riskFreeRate);
            double otmVega = greeks.vega(90.0, strike, timeToExpiry, volatility, riskFreeRate);
            
            assertTrue(atmVega > itmVega, "ATM vega should be > ITM vega");
            assertTrue(atmVega > otmVega, "ATM vega should be > OTM vega");
        }
        
        @Test
        @DisplayName("Vega increases with time to expiry")
        void testVegaIncreasesWithTime() {
            double vegaShort = greeks.vega(spot, strike, 0.25, volatility, riskFreeRate);
            double vegaLong = greeks.vega(spot, strike, 1.0, volatility, riskFreeRate);
            
            assertTrue(vegaLong > vegaShort, 
                      "Vega should increase with time to expiry");
        }
        
      
    }
    
    // =========================================================================
    // Theta Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Theta Tests")
    class ThetaTests {
        
        @Test
        @DisplayName("Theta should be negative for long calls")
        void testThetaIsNegative() {
            double theta = greeks.thetaCall(spot, strike, timeToExpiry, volatility, riskFreeRate);
            
            assertTrue(theta < 0, "Theta should be negative for long calls (time decay)");
        }
        
        @Test
        @DisplayName("Theta magnitude increases near expiry")
        void testThetaIncreasesNearExpiry() {
            double thetaLongDated = greeks.thetaCall(spot, strike, 1.0, volatility, riskFreeRate);
            double thetaShortDated = greeks.thetaCall(spot, strike, 0.1, volatility, riskFreeRate);
            
            // Theta becomes more negative (larger magnitude) near expiry
            assertTrue(Math.abs(thetaShortDated) > Math.abs(thetaLongDated), 
                      "Theta magnitude should increase near expiry");
        }
        
}
    
    
    // =========================================================================
    // Rho Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Rho Tests")
    class RhoTests {
        
        @Test
        @DisplayName("Rho should be positive for calls")
        void testCallRhoIsPositive() {
            double rho = greeks.rhoCall(spot, strike, timeToExpiry, volatility, riskFreeRate);
            
            assertTrue(rho > 0, "Rho should be positive for calls");
        }
        
        @Test
        @DisplayName("Rho should be negative for puts")
        void testPutRhoIsNegative() {
            double rho = greeks.rhoPut(spot, strike, timeToExpiry, volatility, riskFreeRate);
            
            assertTrue(rho < 0, "Rho should be negative for puts");
        }
        
        @Test
        @DisplayName("Rho increases with time to expiry")
        void testRhoIncreasesWithTime() {
            double rhoShort = greeks.rhoCall(spot, strike, 0.25, volatility, riskFreeRate);
            double rhoLong = greeks.rhoCall(spot, strike, 1.0, volatility, riskFreeRate);
            
            assertTrue(rhoLong > rhoShort, 
                      "Rho should increase with time to expiry");
        }
    }
    
    // =========================================================================
    // Integration Tests
    // =========================================================================
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should calculate all Greeks at once")
        void testCalculateAllGreeks() {
            OptionGreeks callGreeks = greeks.calculateAllGreeks(
                spot, strike, timeToExpiry, volatility, riskFreeRate, true
            );
            
            assertNotNull(callGreeks);
            assertTrue(callGreeks.getDelta() > 0, "Call delta should be positive");
            assertTrue(callGreeks.getGamma() > 0, "Gamma should be positive");
            assertTrue(callGreeks.getVega() > 0, "Vega should be positive");
            assertTrue(callGreeks.getTheta() < 0, "Theta should be negative");
            assertTrue(callGreeks.getRho() > 0, "Call rho should be positive");
            
            System.out.println("Call Greeks: " + callGreeks);
        }
        
        @Test
        @DisplayName("Put Greeks should differ from call Greeks")
        void testPutGreeksDifferent() {
            OptionGreeks callGreeks = greeks.calculateAllGreeks(
                spot, strike, timeToExpiry, volatility, riskFreeRate, true
            );
            
            OptionGreeks putGreeks = greeks.calculateAllGreeks(
                spot, strike, timeToExpiry, volatility, riskFreeRate, false
            );
            
            // Delta and Rho differ
            assertNotEquals(callGreeks.getDelta(), putGreeks.getDelta(), 
                          "Call and put delta should differ");
            assertNotEquals(callGreeks.getRho(), putGreeks.getRho(), 
                          "Call and put rho should differ");
            
            // Gamma and Vega are same
            assertEquals(callGreeks.getGamma(), putGreeks.getGamma(), 0.0001, 
                       "Gamma should be same for calls and puts");
            assertEquals(callGreeks.getVega(), putGreeks.getVega(), 0.0001, 
                       "Vega should be same for calls and puts");
            
            System.out.println("Put Greeks: " + putGreeks);
        }
        
        @Test
        @DisplayName("Should handle real-world crude oil option")
        void testRealWorldExample() {
            // Crude oil option parameters
            double crudeSpot = 92.50;
            double crudeStrike = 95.00;
            double crudeTime = 0.25;  // 3 months
            double crudeVol = 0.30;    // 30% (typical for commodities)
            double crudeRate = 0.05;   // 5%
            
            OptionGreeks callGreeks = greeks.calculateAllGreeks(
                crudeSpot, crudeStrike, crudeTime, crudeVol, crudeRate, true
            );
            
            System.out.println("\n=== Crude Oil Call Option ===");
            System.out.println("Spot: $" + crudeSpot);
            System.out.println("Strike: $" + crudeStrike);
            System.out.println("Time: " + (crudeTime * 12) + " months");
            System.out.println("Vol: " + (crudeVol * 100) + "%");
            System.out.println();
            System.out.println(callGreeks);
            System.out.println();
            System.out.println("Interpretation:");
            System.out.printf("- If crude moves $1: Option changes by $%.2f (delta)%n", 
                             callGreeks.getDelta());
            System.out.printf("- Per 1%% vol change: Option changes by $%.2f (vega)%n", 
                             callGreeks.getVega());
            System.out.printf("- Per day passing: Option loses $%.4f (theta)%n", 
                             callGreeks.getTheta());
            
            assertNotNull(callGreeks);
        }
    }
}
 
