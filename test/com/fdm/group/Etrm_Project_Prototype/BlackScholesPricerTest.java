package com.fdm.group.Etrm_Project_Prototype;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.OptionalDouble;
import java.util.Set;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class BlackScholesPricerTest {
	
	
	
	
	@Test
	void testBlackScholesCallPrices() {
		
		BlackScholesPricer pricer = new BlackScholesPricer();
		
		double spot = 100.0;
	    double strike = 100.0;
	    double timeToExpiry = 1.0;  // 1 year
	    double volatility = 0.20;    // 20% vol
	    double riskFreeRate = 0.05;  // 5% rate
	    
	    
	    double callPrice = pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, volatility);
	    
	    
	    assertEquals(10.45, callPrice, 0.01);
		
	}

}
