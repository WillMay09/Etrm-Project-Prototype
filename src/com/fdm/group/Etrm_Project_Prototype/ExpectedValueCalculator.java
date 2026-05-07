package com.fdm.group.Etrm_Project_Prototype;

public class ExpectedValueCalculator {

	/**
     * Simple expected value calculation
     * 
     * @param outcomes Array of possible outcomes
     * @param probabilities Array of probabilities (must sum to 1.0)
     * @return Expected value
     */
	private static double calculateExpectedValue(double[] outcomes, double[] probabilites) {
		
		if(outcomes.length != probabilites.length) {
			
			throw new IllegalArgumentException("outcomes and probabilities must be the same lenght");
			
		}
		
		double expectedValue = 0.0;
		
		for(int i = 0; i<outcomes.length;i++) {
			
			expectedValue += outcomes[i] * probabilites[i];
			
		}
		
		return expectedValue;
	}
	
	
	
	/*Option Payoff Scenario */
	
	
	public static void main(String[] args) {
		
		
		double[] stockPrices = {105.0, 107.0, 110.5};
		
		double[] probabilities = {0.50, 0.30, 0.20};
		
		double strike = 100.0;
		
		double[] callPayoffs = new double[stockPrices.length];
		
		for(int i =0; i<stockPrices.length;i++) {
			
			callPayoffs[i] = Math.max(stockPrices[i]-strike, 0.0);
		}
		
		
		double ExpectedPayoff = calculateExpectedValue(callPayoffs, probabilities);
		
		
		System.out.println("expected Payoff" + ExpectedPayoff);
		
	}
}
