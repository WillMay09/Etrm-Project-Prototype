package com.fdm.group.Etrm_Project_Prototype;

import java.util.Objects;

public class GreeksCalculator {

	private final BlackScholesPricer pricer;

	// Default bump sizes for finite difference calculations
	private static final double SPOT_BUMP = 0.01; // $0.01 for delta/gamma
	private static final double VOL_BUMP = 0.0001; // 0.01% for vega
	private static final double TIME_BUMP = 1.0 / 365.0; // 1 day for theta
	private static final double RATE_BUMP = 0.0001; // 0.01% for rho

	/**
	 * Constructor with BlackScholesPricer dependency
	 */

	public GreeksCalculator(BlackScholesPricer pricer) {

		this.pricer = Objects.requireNonNull(pricer, "BlackScholesPricer cannot be null");

	}

	/**
	 * Default constructor - creates own pricer
	 */

	public GreeksCalculator() {

		this.pricer = new BlackScholesPricer();
	}

	// =========================================================================
	// DELTA - Sensitivity to underlying price
	// =========================================================================

	/**
	 * Calculate delta for a call option (analytical formula)
	 * 
	 * Delta = N(d1)
	 * 
	 * Interpretation: - Delta of 0.6 means option gains $0.60 per $1 increase in
	 * stock - Range: 0 to 1 for calls - Deep ITM calls approach 1.0 - Deep OTM
	 * calls approach 0 - ATM calls around 0.5
	 * 
	 * @return Delta value
	 */

	public double deltaCall(double spot, double strike, double timeToExpiry, double volatility, double riskFreeRate) {

		// Delta is 1 if ITM or 0 if OTM at expiry
		if (timeToExpiry <= 0.0) {

			return spot >= strike ? 1.0 : 0.0;
		}

		double d1 = pricer.calculateD1(spot, strike, riskFreeRate, volatility, riskFreeRate);

		return pricer.normalCDF(d1);

	}

	/**
	 * Calculate delta for a put option (analytical formula)
	 * 
	 * Delta = N(d1) - 1
	 * 
	 * Interpretation: - Range: -1 to 0 for puts - Deep ITM puts approach -1.0 -
	 * Deep OTM puts approach 0 - ATM puts around -0.5
	 * 
	 * @return Delta value (negative for puts)
	 */

	public double deltaPut(double spot, double strike, double timeToExpiry, double volatility, double riskFreeRate) {

		if (timeToExpiry <= 0.0) {

			return spot <= strike ? -1.0 : 0.0;
		}

		double d1 = pricer.calculateD1(spot, strike, riskFreeRate, volatility, riskFreeRate);

		return pricer.normalCDF(d1) - 1.0;

	}

	/**
	 * Calculate delta using finite differences (for verification)
	 * 
	 * Delta ≈ (Price(S+ΔS) - Price(S-ΔS)) / (2ΔS)
	 * 
	 * @param isCall true for call, false for put
	 * @return Delta value
	 */

	/**
	 * Calculate gamma (analytical formula)
	 * 
	 * Gamma = N'(d1) / (S × σ × √T)
	 * 
	 * Where N'(d1) is the standard normal PDF
	 * 
	 * Interpretation: - Gamma measures how fast delta changes - Same for calls and
	 * puts - Highest for ATM options near expiry - Important for hedging: high
	 * gamma = frequent rehedging needed
	 * 
	 * @return Gamma value (same for call and put)
	 */

	public double Gamma(double spot, double strike, double timeToExpiry, double volatility, double riskFreeRate) {

		if (timeToExpiry <= 0.0) {

			return 0.0;
		}

		double d1 = pricer.calculateD1(spot, strike, riskFreeRate, volatility, riskFreeRate);

		double pdf = pricer.normalPDF(d1);

		double sigmaRootT = volatility * Math.sqrt(timeToExpiry);

		return pdf / (spot * sigmaRootT);

	}

	/**
	 * Calculate gamma using finite differences (for verification)
	 * 
	 * Gamma ≈ (Price(S+ΔS) - 2×Price(S) + Price(S-ΔS)) / (ΔS)²
	 * 
	 * Or equivalently: Gamma ≈ (Delta(S+ΔS) - Delta(S-ΔS)) / (2ΔS)
	 * 
	 * @param isCall true for call, false for put
	 * @return Gamma value
	 */

// =========================================================================
// VEGA - Sensitivity to volatility
// =========================================================================

	public double Vega(double spot, double strike, double timeToExpiry, double volatility, double riskFreeRate) {

		if (timeToExpiry <= 0.0) {

			return 0.0; // No vega at expiry
		}

		double d1 = pricer.calculateD1(spot, strike, riskFreeRate, volatility, riskFreeRate);
		double rootT = Math.sqrt(timeToExpiry);

		double pdf = pricer.normalPDF(d1);

		return spot * pdf * rootT / 100.0;

	}

// =========================================================================
// THETA - Time decay
// =========================================================================

	/**
	 * Calculate theta for a call option (analytical formula)
	 * 
	 * Theta = -(S × N'(d1) × σ) / (2√T) - rK × e^(-rT) × N(d2)
	 * 
	 * Interpretation: - Theta measures time decay (change in value per day) -
	 * Negative for long options (lose value each day) - Positive for short options
	 * (gain value each day) - Accelerates as expiry approaches - Usually expressed
	 * as per-day change
	 * 
	 * @return Theta value (typically negative for long calls)
	 */

	public double thetaCall(double spot, double strike, double timeToExpiry, double volatility, double riskFreeRate) {

		double d1 = pricer.calculateD1(spot, strike, riskFreeRate, volatility, riskFreeRate);
		double d2 = pricer.calculateD2(spot, strike, riskFreeRate, volatility, d1);

		double rootT = Math.sqrt(timeToExpiry);
		double nd2 = pricer.normalCDF(d2);

		double pdf = pricer.normalPDF(d1);

		double discountFactor = Math.exp(-riskFreeRate * timeToExpiry);

		// -(S × N'(d1) × σ) / (2√T)
		double firstTerm = -(spot * pdf * volatility) / (2 * rootT);

		// -rK × e^(-rT) × N(d2)
		double secondTerm = -(riskFreeRate * strike * discountFactor * nd2);

		// theta per day
		return (firstTerm + secondTerm) / 365.0;

	}

	/**
	 * Calculate theta for a put option (analytical formula)
	 * 
	 * Theta = -(S × N'(d1) × σ) / (2√T) + rK × e^(-rT) × N(-d2)
	 * 
	 * @return Theta value
	 */

	public double thetaPut(double spot, double strike, double timeToExpiry, double volatility, double riskFreeRate) {

		if (timeToExpiry <= 0.0) {
			return 0.0; // No theta at expiry
		}

		double d1 = pricer.calculateD1(spot, strike, riskFreeRate, volatility, timeToExpiry);
		double d2 = pricer.calculateD2(spot, strike, riskFreeRate, volatility, timeToExpiry);

		double pdf = pricer.normalPDF(d1);
		double nMinusD2 = pricer.normalCDF(-d2);

		double rootT = Math.sqrt(timeToExpiry);
		double discountFactor = Math.exp(-riskFreeRate * timeToExpiry);

		// First term: -(S × N'(d1) × σ) / (2√T)
		double term1 = -(spot * pdf * volatility) / (2.0 * rootT);

		// Second term: +rK × e^(-rT) × N(-d2)
		double term2 = riskFreeRate * strike * discountFactor * nMinusD2;

		// Return theta per day (divide by 365)
		return (term1 + term2) / 365.0;

	}

	/**
	 * Calculate theta using finite differences (for verification)
	 * 
	 * Theta ≈ (Price(T) - Price(T-ΔT)) / ΔT
	 * 
	 * Note: Sign convention - this gives negative theta for long options
	 * 
	 * @param isCall true for call, false for put
	 * @return Theta value
	 */

// =========================================================================
// RHO - Sensitivity to interest rate
// =========================================================================

	/**
	 * Calculate rho for a call option (analytical formula)
	 * 
	 * Rho = K × T × e^(-rT) × N(d2)
	 * 
	 * Interpretation: - Rho measures sensitivity to interest rate changes -
	 * Positive for calls (benefit from rate increase) - Negative for puts (hurt by
	 * rate increase) - Usually the least important Greek - Expressed as change per
	 * 1% change in rate
	 * 
	 * @return Rho value
	 */

	public double rhoCall(double spot, double strike, double timeToExpiry, double volatility, double riskFreeRate) {

		double discountFactor = Math.sqrt(-riskFreeRate * timeToExpiry);

		double d2 = pricer.calculateD2(spot, strike, riskFreeRate, volatility, discountFactor);

		double nd2 = pricer.normalCDF(d2);

		double Rho = strike * timeToExpiry * discountFactor * nd2;

		return Rho / 100.0;

	}

	/**
	 * Calculate rho for a put option (analytical formula)
	 * 
	 * Rho = -K × T × e^(-rT) × N(-d2)
	 * 
	 * @return Rho value (negative for puts)
	 */

	public double rhoPut(double spot, double strike, double timeToExpiry, double volatility, double riskFreeRate) {

		double discountFactor = Math.sqrt(-riskFreeRate * timeToExpiry);
		double d2 = pricer.calculateD2(spot, strike, riskFreeRate, volatility, discountFactor);

		double nMinusD2 = pricer.normalCDF(d2);

		double Rho = -strike * timeToExpiry * discountFactor * nMinusD2;

		return Rho / 100.0;
	}
// =========================================================================
// Helper Methods
// =========================================================================

	/**
	 * Error function (same as in BlackScholesPricer)
	 */
	private double erf(double x) {
		// Constants for approximation
		final double A1 = 0.254829592;
		final double A2 = -0.284496736;
		final double A3 = 1.421413741;
		final double A4 = -1.453152027;
		final double A5 = 1.061405429;
		final double P = 0.3275911;

		int sign = (x >= 0) ? 1 : -1;
		x = Math.abs(x);

		double t = 1.0 / (1.0 + P * x);
		double y = 1.0 - (((((A5 * t + A4) * t) + A3) * t + A2) * t + A1) * t * Math.exp(-x * x);

		return sign * y;
	}

// =========================================================================
// Convenience Method - Calculate All Greeks
// =========================================================================

	/**
	 * Calculate all Greeks at once for efficiency
	 * 
	 * @param isCall true for call, false for put
	 * @return OptionGreeks object with all Greeks
	 */

	public OptionGreeks calculateAllGreeks(double spot, double strike, double timeToExpiry, double volatility,
			double riskFreeRate, boolean isCall) {

		double delta = isCall ? deltaCall(spot, strike, timeToExpiry, volatility, riskFreeRate)
				: deltaPut(spot, strike, timeToExpiry, volatility, riskFreeRate);

		double gamma = Gamma(spot, strike, timeToExpiry, volatility, riskFreeRate);

		double vega = Vega(spot, strike, timeToExpiry, volatility, riskFreeRate);

		double theta = isCall ? thetaCall(spot, strike, timeToExpiry, volatility, riskFreeRate)
				: thetaPut(spot, strike, timeToExpiry, volatility, riskFreeRate);
		
		double rho = isCall ? rhoCall(spot,strike,timeToExpiry,volatility,riskFreeRate) : rhoPut(spot,strike,timeToExpiry,volatility,riskFreeRate);

		
		
		return new OptionGreeks(delta, gamma, vega, theta, rho);
	}

	/**
	 * Immutable container for all Greeks
	 */

	public static class OptionGreeks {

		private final double delta;

		private final double gamma;

		private final double vega;

		private final double theta;

		private final double rho;

		public OptionGreeks(double delta, double gamma, double vega, double theta, double rho) {

			this.delta = delta;
			this.gamma = gamma;
			this.vega = vega;
			this.theta = theta;
			this.rho = rho;

		}

		public double getDelta() {
			return delta;
		}

		public double getGamma() {
			return gamma;
		}

		public double getVega() {
			return vega;
		}

		public double getTheta() {
			return theta;
		}

		public double getRho() {
			return rho;
		}

		@Override
		public String toString() {
			return String.format("Greeks[Delta=%.4f, Gamma=%.4f, Vega=%.4f, Theta=%.4f, Rho=%.4f]", delta, gamma, vega,
					theta, rho);
		}

	}

}
