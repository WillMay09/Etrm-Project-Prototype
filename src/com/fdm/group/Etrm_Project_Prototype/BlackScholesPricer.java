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

	// standard normal distribution
	public double normalCDF(double x) {

		return 0.5 * (1 + erf(x / Math.sqrt(2.0)));
	}

	// probability density function
	public double normalPDF(double x) {

		return (1 / Math.sqrt(2 * Math.PI)) * Math.exp(x * x * -0.5);

	}

	/**
	 * Error function: erf(x)
	 * 
	 * @param x Input value
	 * @return erf(x)
	 */

	private double erf(double x) {

		int sign = (x >= 0) ? 1 : -1;
		x = Math.abs(x);

		// A&S formula 7.1.26
		double t = 1.0 / (1.0 + P * x);
		double y = 1.0 - (((((A5 * t + A4) * t) + A3) * t + A2) * t + A1) * t * Math.exp(-x * x);

		return sign * y;

	}

	/**
	 * Calculate d1 parameter N(d1) = Risk-adjusted probability (used for
	 * calculating expected value) d1 = [ln(S/K) + (r + σ²/2)T] / (σ√T)
	 * 
	 * @return d1 value
	 */

	public double calculateD1(double spotPrice, double strikePrice, double riskFreeRate, double volatility,
			double timeToMaturity) {

		if (timeToMaturity <= 0.0) {

			if (spotPrice >= strikePrice) {

				return Double.POSITIVE_INFINITY;
			} else {

				return Double.NEGATIVE_INFINITY;
			}

		}

		double sigmaRootT = volatility * Math.sqrt(timeToMaturity);

		double d1 = (Math.log(spotPrice / strikePrice)
				+ ((riskFreeRate + 0.5 * volatility * volatility) * timeToMaturity)) / sigmaRootT;
		return d1;
	}

	/**
	 * Calculate d1 parameter - forward pricing N(d1) = Risk-adjusted probability
	 * (used for calculating expected value) d1 = [ln(F/K) + (σ²/2)T] / (σ√T) spot
	 * drifts at the risk free rate under risk neutral pricing forward pricing = F =
	 * S x e^(rT)
	 * 
	 * @return d1 value
	 */

	/**
	 * Calculate d2 parameter (useful for Greeks calculation)
	 * 
	 * d2 = d1 - σ√T N(d2) = Probability the option will expire in-the-money
	 * 
	 * @return d2 value
	 */

	public double calculateD2(double spotPrice, double strikePrice, double riskFreeRate, double volatility,
			double timeToMaturity) {

		double sigmaRootT = volatility * Math.sqrt(timeToMaturity);

		double d1 = calculateD1(spotPrice, strikePrice, riskFreeRate, volatility, timeToMaturity);

		double d2 = d1 - sigmaRootT;

		return d2;
	}

	public double priceCall(double spot, double strike, double timeToExpiry, double riskFreeRate, double volatility) {

		double d1 = calculateD1(spot, strike, riskFreeRate, volatility, timeToExpiry);

		double d2 = calculateD2(spot, strike, riskFreeRate, volatility, timeToExpiry);

		double callPrice = spot * normalCDF(d1) - strike * Math.exp(-riskFreeRate * timeToExpiry) * normalCDF(d2);
		return callPrice;
	}

	// Put call using forward value

	public double black76Call(double forward, double strike, double timeToExpiry, double riskFreeRate,
			double volatility) {

		if (timeToExpiry <= 0) {
			return Math.max(forward - strike, 0.0);
		}
		if (volatility == 0.0) {
			return Math.max(forward - strike, 0.0) * DiscountFactor.of(riskFreeRate, timeToExpiry);
		}

		double d1 = (Math.log(forward / strike) + (0.5 * volatility * volatility) * timeToExpiry)
				/ (volatility * Math.sqrt(timeToExpiry));

		double d2 = d1 - volatility * Math.sqrt(timeToExpiry);

		double forecastValue = forward * normalCDF(d1) - strike * normalCDF(d2);

		return DiscountFactor.of(riskFreeRate, timeToExpiry) * forecastValue;

	}

	// Put using forward value

	public double black76Put(double forward, double strike, double timeToExpiry, double riskFreeRate,
			double volatility) {

		if (timeToExpiry <= 0) {
			return Math.max(strike - forward, 0.0);
		}
		if (volatility == 0) {
			return Math.max(strike - forward, 0.0) * DiscountFactor.of(riskFreeRate, timeToExpiry);
		}

		double d1 = (Math.log(forward / strike) + (0.5 * volatility * volatility) * timeToExpiry)
				/ (volatility * Math.sqrt(timeToExpiry));
		double d2 = d1 - volatility * Math.sqrt(timeToExpiry);

		double forecastValue = strike * normalCDF(-d2) - forward * normalCDF(-d1);

		return DiscountFactor.of(riskFreeRate, timeToExpiry) * forecastValue;
	}

	// Forecast value using forward pricing

	public double black76ForecastValue(double forward, double strike, double timeToExpiry, double volatility) {

		if (timeToExpiry <= 0.0) {
			return Math.max(forward - strike, 0.0);
		}
		if (volatility == 0.0) {
			return Math.max(forward - strike, 0.0);

		}

		double sigmaRoot = volatility * Math.sqrt(timeToExpiry);

		double d1 = (Math.log(forward / strike) + 0.5 * volatility * volatility * timeToExpiry) / sigmaRoot;

		double d2 = d1 - sigmaRoot;

		return forward * normalCDF(d1) - strike * normalCDF(d2);
	}

	public double black76ForecastValuePut(double forward, double strike, double timeToExpiry, double volatility) {

		if (timeToExpiry <= 0)
			return Math.max(strike - forward, 0.0);
		if (volatility == 0.0)
			return Math.max(strike - forward, 0.0);

		double sigmaRootT = volatility * Math.sqrt(timeToExpiry);

		double d1 = (Math.log(forward / strike) + 0.5 * volatility * volatility * timeToExpiry) / sigmaRootT;

		double d2 = d1 - sigmaRootT;

		return strike * normalCDF(-d2) - forward * normalCDF(-d1);
	}

	public double pricePut(double spot, double strike, double timeToExpiry, double riskFreeRate, double volatility) {

		// Put-Call parity:
		// Put = Call - Spot + strike x e^(-rT)

		double callPrice = priceCall(spot, strike, timeToExpiry, riskFreeRate, volatility);

		double putPrice = (callPrice - spot) + strike * Math.exp(-riskFreeRate * timeToExpiry);

		return putPrice;
	}

}
