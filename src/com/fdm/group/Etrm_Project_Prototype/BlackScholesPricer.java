package com.fdm.group.Etrm_Project_Prototype;

public class BlackScholesPricer {
	
	
	private final double SQRT_TWO = Math.sqrt(2);
	// Constants for approximation
    private static final double A1 = 0.254829592;
    private static final double A2 = -0.284496736;
    private static final double A3 = 1.421413741;
    private static final double A4 = -1.453152027;
    private static final double A5 = 1.061405429;
    private static final double P = 0.3275911;
	
	
	
	//standard normal distribution 
	public double normalCDF(double x) {
		
		return 0.5*(1+erf(x/Math.sqrt(2.0)));
	}
	
	
	
	public double normalPDF(double x) {
		
		
		return (1/Math.sqrt(2*Math.PI)) * Math.exp(x*x*-0.5);
		
	}
	
	
	 /**
     * Error function: erf(x)
     * 
     * @param x Input value
     * @return erf(x)
     */
	
	private double erf(double x) {
		
		int sign = (x >=0) ? 1 : -1;
		x = Math.abs(x);
		
		
		// A&S formula 7.1.26
        double t = 1.0 / (1.0 + P * x);
        double y = 1.0 - (((((A5 * t + A4) * t) + A3) * t + A2) * t + A1) * t * Math.exp(-x * x);
        
        
        return sign * y;
		
	}
	
	
	/**
     * Calculate d1 parameter 
     * N(d1) = Risk-adjusted probability (used for calculating expected value)
     * d1 = [ln(S/K) + (r + σ²/2)T] / (σ√T)
     * 
     * @return d1 value
     */
	
	public double calculateD1(double spotPrice, double strikePrice, double riskFreeRate, double volatility, double timeToMaturity) {
		
		
		if(timeToMaturity <= 0.0) {
			
			if(spotPrice >= strikePrice) {
				
				return Double.POSITIVE_INFINITY;
			}else {
				
				return Double.NEGATIVE_INFINITY;
			}
		
			
		}
		
		double sigmaRootT = volatility * Math.sqrt(timeToMaturity);
		
		double d1 = (Math.log(spotPrice/strikePrice) + ((riskFreeRate + 0.5 * volatility * volatility)*timeToMaturity))/ sigmaRootT;
		
		
		return d1;
		
	}
	
	
	
	/**
     * Calculate d2 parameter (useful for Greeks calculation)
     * 
     * d2 = d1 - σ√T
     * N(d2) = Probability the option will expire in-the-money
     * @return d2 value
     */
	
	
	public double calculateD2(double spotPrice, double strikePrice, double riskFreeRate, double volatility, double timeToMaturity) {
		
		
		double sigmaRootT = volatility * Math.sqrt(timeToMaturity);
		
		
		double d1 = calculateD1(spotPrice,strikePrice, riskFreeRate, volatility, timeToMaturity);
		
		
			double d2 = d1-sigmaRootT;
			
			return d2;
			
		
	}
	
	
	
	
	
	
	// =========================================================================
    // Normal Distribution Functions
    // =========================================================================
    
    /**
     * Standard normal cumulative distribution function (CDF)
     * 
     * Calculates N(x) = P(X ≤ x) where X ~ N(0,1)
     * Uses error function approximation
     * 
     * @param x Value at which to evaluate CDF
     * @return Probability that standard normal variable ≤ x
     */
	
	// N(x) = 0.5 * [1 + erf(x/√2)]
	
	
	private double calculateCDF(double x) {
		
		double Nx = 0.5 * (1+erf(x/SQRT_TWO));
		
		return Nx;
		
	}
	
	
	public double priceCall(double spot, double strike, double timeToExpiry, double riskFreeRate, double volatility) {
		
		double d1 = calculateD1(spot, strike, riskFreeRate, volatility, timeToExpiry);
		
		
		
		double d2 = calculateD2(spot,strike, riskFreeRate, volatility, timeToExpiry);
		
		
		double callPrice = spot * normalCDF(d1) - strike * Math.exp(-riskFreeRate * timeToExpiry)* normalCDF(d2);
		
		return callPrice;
	}
	
	
	
	public double pricePut(double spot, double strike, double timeToExpiry, double riskFreeRate, double volatility) {
		
		//Put-Call parity:
		//Put = Call - Spot + strike x e^(-rT)
		
		
		double callPrice = priceCall(spot,strike, timeToExpiry, riskFreeRate, volatility);
		
		
		double putPrice = (callPrice-spot) + strike * Math.exp(-riskFreeRate * timeToExpiry);
		
		return putPrice;
	}
	
	

	
	
	

}
