package com.fdm.group.Etrm_Project_Prototype;

/**
 * The ONE place discount factors are computed in this codebase.
 *
 * Every other class - BlackScholesPricer, ResolvedCrudeOilOption,
 * CashflowEngine - calls into this rather than computing e^(-rT) inline.
 *
 * This is the structural fix for "discount factor drift": if every
 * call site computes Math.exp(-r*T) independently, a typo or a stale
 * rate in ONE call site silently disagrees with all the others.
 * Centralizing it here means there is exactly one formula to get right.
 */



public final class DiscountFactor {
	
	private DiscountFactor() {
		
		//utility class
		
	}
	
	
	 /**
     * Continuously-compounded discount factor.
     * df = e^(-r x T)
     *
     * @param riskFreeRate  annualized risk-free rate, as decimal (0.05 = 5%)
     * @param timeToPayment time in years until the cashflow/expiry occurs
     * @return discount factor, in (0, 1] for non-negative rate and time
     */
	
	public static double of(double riskFreeRate, double timeToPayment) {
		
		if(timeToPayment <0.0) {
			
			throw new IllegalArgumentException("Time to payment cannot be negative: "+ timeToPayment);
		};
		
		return Math.exp(-riskFreeRate * timeToPayment);
	}
	
	/**
     * Inverse - given a discount factor and a rate, recover the time.
     * Rarely needed, but exists so the relationship is symmetric and testable.
     */
	
	public static double impliedTime(double discountFactor, double riskFreeRate) {
		
		if(riskFreeRate == 0.0) {
			
			throw new IllegalArgumentException("Cannot imply Time from a 0.0 rate");
		}
		
		return -Math.log(discountFactor)/ riskFreeRate;
	}
	
	

}
