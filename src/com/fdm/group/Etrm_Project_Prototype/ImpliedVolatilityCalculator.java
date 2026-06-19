
package com.fdm.group.Etrm_Project_Prototype;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fdm.group.Etrm_Project_Prototype.GreeksCalculator.OptionGreeks;
/**
 * Implied Volatility Calculator
 * 
 * Given market price, solves for volatility using Newton-Raphson method
 * 
 * This is THE KEY use case for Black-Scholes in practice!
 */

public class ImpliedVolatilityCalculator {

	
	
	private final GreeksCalculator greeks;
	private final BlackScholesPricer pricer;
	
	private static final double TOLERANCE = 0.0001; //0.01% accuracy
	private static final int MAX_ITERATIONS = 100; 
	private static final double INITIAL_GUESS = 0.20;// start at 20% vol
	
	
	
	public ImpliedVolatilityCalculator(GreeksCalculator greeks, BlackScholesPricer pricer) {
		
		this.greeks = greeks;
		this.pricer = pricer;
		
	}
	
	public ImpliedVolatilityCalculator() {
		
		this.greeks = new GreeksCalculator();
		this.pricer = new BlackScholesPricer();
		
	}
	
	
	 /**
     * Calculate implied volatility from market price
     * 
     * Uses Newton-Raphson iteration:
     * vol_new = vol_old + (marketPrice - theoreticalPrice) / vega
     * 
     * @param marketPrice Observed market price
     * @param spot Current spot price
     * @param strike Strike price
     * @param timeToExpiry Time to expiry in years
     * @param riskFreeRate Risk-free rate
     * @param isCall true for call, false for put
     * @return Implied volatility (as decimal, e.g., 0.25 for 25%)
     */
	
	public double calculateImpliedVol(double marketPrice, double spot, double strike, double timeToExpiry, double riskFreeRate, boolean isCall) {
		
		
		if(marketPrice <= 0 ) {
			throw new IllegalArgumentException("Market price must be greater than 0");
		}
		
		double instrinsicValue = isCall ? Math.max(spot-strike, 0.0) : Math.max(strike-spot, 0.0);
		
		if(marketPrice < instrinsicValue) {
			throw new IllegalArgumentException("Market price below instrinsic value - arbitrage exists");
		}
		
		//Initial Guess
		double volatility = INITIAL_GUESS;
		
		
		for(int i = 0; i<MAX_ITERATIONS; i++) {
			
			double theoreticalPrice = isCall ? pricer.priceCall(spot, strike, timeToExpiry, riskFreeRate, volatility):
				pricer.pricePut(spot, strike, timeToExpiry, riskFreeRate, volatility);
			
			//Calculate difference
			double priceDiff = marketPrice - theoreticalPrice;
			//check convergence
			if(Math.abs(priceDiff) < TOLERANCE) {
				return volatility;
			}
			//Calculate vega
			double vega = greeks.vega(spot, strike, timeToExpiry, volatility, riskFreeRate);
			
			if(Math.abs(vega)< 1e-10) {
				
				throw new RuntimeException("Vega too small - unable to converge");
				
			}
			
			//Newton-Raphson step
			volatility +=priceDiff / vega;
			
			//Keep vol in resonable bounds
			
			if(volatility < 0.001) {
				
				volatility = 0.001;
			}
			if(volatility > 5.0) {
				
				volatility = 5.0;
			}
			
		}
		
		throw new RuntimeException("Implied volatility did not converage after " + MAX_ITERATIONS);
	
	}
	
	   /**
     * Calculate implied volatility surface
     * 
     * @param marketPrices Map of (strike, expiry) -> market price
     * @return Map of (strike, expiry) -> implied vol
     */
	
	public Map<StrikeExpiry, Double> calculateImpliedVolSurface(Map<StrikeExpiry, Double> marketPrices, double spot, double riskFreeRate, boolean isCall){
		
		Map<StrikeExpiry, Double> impliedVols = new HashMap<>();
		
		for(Map.Entry<StrikeExpiry, Double> entry : marketPrices.entrySet()) {
			
			StrikeExpiry key = entry.getKey();
			
			double marketPrice = entry.getValue();
			
			try {
				double iv = calculateImpliedVol(marketPrice, spot, key.strike, key.timeToExpiry, riskFreeRate,isCall);
				impliedVols.put(key, iv);
			}catch(Exception e) {
				System.err.println("Failed to calculate IV for " + key + " : " + e.getMessage());
			}
		}
		
		return impliedVols;
		
	}
	
	
	
	
	 /**
     * Container for strike/expiry pair
     */
	
	public static class StrikeExpiry{
		
		public final double strike;
		public final double timeToExpiry;
		
		
		public StrikeExpiry(double strike, double timeToExpiry) {
			
			this.strike = strike;
			this.timeToExpiry = timeToExpiry;
		}
		
		  @Override
	        public boolean equals(Object o) {
	            if (!(o instanceof StrikeExpiry)) return false;
	            StrikeExpiry other = (StrikeExpiry) o;
	            return Double.compare(strike, other.strike) == 0 &&
	                   Double.compare(timeToExpiry, other.timeToExpiry) == 0;
	        }
	        
	        @Override
	        public int hashCode() {
	            return Objects.hash(strike, timeToExpiry);
	        }
	        
	        @Override
	        public String toString() {
	            return String.format("Strike=%.2f, Time=%.2f", strike, timeToExpiry);
	        }
		
		
		
		
	}
	
	
	
}
