package com.fdm.group.Etrm_Project_Prototype;

public class PresentValueCalculator {

	
	
	public PresentValueCalculator() {
		
		
	}
	
	 /**
     * Discrete compounding
     */
	
	

	public static double calculatePresentValueDiscrete(double futureValue, double interestRate,double years) {
		
		
		double presentValue = futureValue / Math.pow(1+interestRate, years);
		
		
		return presentValue;
		
		
		
	}
	/**
     * Continuous compounding (used in Black-Scholes)
     */
	public static double calculatePresentValueContinuous(double futureValue, double interestRate,double years) {
		
		
		double presentValue = futureValue * Math.exp(-interestRate * years);
		
		
		return presentValue;
		
		
		
	}
	
	
	 /**
     * Discount factor (the e^(-rt) part)
     */
	
	public static double discountFactor(double rate, double time) {
		
		
		return Math.exp(-rate * time);
	}
	
	
	
	public static void main(String[] args) {
		
		 // Expected payoff at expiry: $5.50
        double expectedPayoff = 5.50;
        
        // Time to expiry: 0.25 years (3 months)
        double timeToExpiry = 0.25;
        
        // Risk-free rate: 5%
        double riskFreeRate = 0.05;
        
        //Present Value
        
        double optionValue = calculatePresentValueContinuous(expectedPayoff, riskFreeRate, timeToExpiry);
		
        System.out.printf("Expected payoff: $%.2f%n", expectedPayoff);
        System.out.printf("Present value: $%.2f%n", optionValue);
        
        // Output:
        // Expected payoff: $5.50
        // Present value: $5.43
        
        //we take into account present value when evaluating black scholes
		
	}
}
