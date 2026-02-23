package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public abstract class OptionDeal extends DerivativeDeal {

	// === OPTION-SPECIFIC FIELDS ===

	/**
	 * Type of option: "CALL" or "PUT"
	 */

	protected String optionType;

	/**
	 * Premium paid (for long) or received (for short)
	 */

	protected double premium;

	/**
	 * Position: "LONG" (buyer) or "SHORT" (seller/writer)
	 */

	protected String position;

	/**
	 * Option style: "AMERICAN" (exercise anytime) or "EUROPEAN" (only at expiry)
	 */

	protected String optionStyle;

	/**
	 * Expiration time: "AM" or "PM" - AM: Expires in morning (e.g., S&P 500 using
	 * SOQ) - PM: Expires at market close (most common)
	 */
	protected String expirationTime;

	protected double contractSize;

	protected int numberOfContracts;

	////PRICING ENGINES////////
	/**
	 * Volatility (used in pricing models like Black-Scholes) Implementation later
	 */
	protected double impliedVolatility;

	protected double riskFreeRate;

	/**
	 * Market data service for current prices
	 */

	// protected transient MarketDataService marketData;

	// ===CONSTRUCTOR///

	public OptionDeal(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double contractPrice, String underlyingCommodity, LocalDate expiryDate, double strikePrice,
			String optionType, double premium, String position, int numberOfContracts) {

// Call parent constructor
		super(dealId, tradeDate, counterParty, quantity, unit, contractPrice, underlyingCommodity, expiryDate,
				strikePrice);

// Validate option-specific fields
		if (!optionType.equals("CALL") && !optionType.equals("PUT")) {
			throw new IllegalArgumentException("Option type must be 'CALL' or 'PUT'");
		}
		if (!position.equals("LONG") && !position.equals("SHORT")) {
			throw new IllegalArgumentException("Position must be 'LONG' or 'SHORT'");
		}
		if (premium <= 0) {
			throw new IllegalArgumentException("Premium must be positive");
		}
		if (numberOfContracts <= 0) {
			throw new IllegalArgumentException("Number of contracts must be positive");
		}

		this.optionType = optionType;
		this.premium = premium;
		this.position = position;
		this.numberOfContracts = numberOfContracts;
		this.optionStyle = "AMERICAN"; // Default for CME options
		this.expirationTime = "PM"; // Default

		this.impliedVolatility = 0.20; // Default 20% volatility
		this.riskFreeRate = 0.05; // Default 5%
	}

	// === ABSTRACT METHODS (must be implemented by CallOption/PutOption) ===

	/**
	 * Calculate option payoff at expiration (intrinsic value only)
	 */

	public abstract double calculatePayoffExpiry();

	/**
	 * Determine if option should be exercised at current prices
	 */
	public abstract boolean shouldExcercise();

	// === CONCRETE METHODS FROM DerivativeDeal ===
	// not fully implemented yet

	@Override
	public double calculateIntrinsicValue() {
		// Intrinsic value depends on option type
		// Implemented in CallOption and PutOption subclasses
		// double currentFuturesPrice = getCurrentFuturesPrice();

		if (optionType.equals("CALL")) {
			return Math.max(0, currentFuturesPrice - strikePrice) * contractSize * numberOfContracts;
		} else { // PUT
			return Math.max(0, strikePrice - currentFuturesPrice) * contractSize * numberOfContracts;
		}
	}

	@Override
	public double calculateTimeValue() {
		// Time Value = Option Premium - Intrinsic Value
		double optionValue = calculateMTM();
		double intrinsicValue = calculateIntrinsicValue();
		return Math.max(0, optionValue - intrinsicValue);
	}

	@Override
	public boolean isInTheMoney() {
		return calculateIntrinsicValue() > 0;
	}

	@Override
	public String getInstrumentType() {
		return "OPTION";
	}

	// === OPTION-SPECIFIC CALCULATIONS ===

	/**
	 * Calculate profit/loss including premium
	 */
	public double calculatePnL() {
		double intrinsicValue = calculateIntrinsicValue();
		double totalPremium = premium * contractSize * numberOfContracts;

		if (position.equals("LONG")) {
			// Long: Intrinsic Value - Premium Paid
			return intrinsicValue - totalPremium;
		} else { // SHORT
			// Short: Premium Received - Intrinsic Value
			return totalPremium - intrinsicValue;
		}
	}

	/**
	 * Calculate breakeven price
	 */

	public double getBreakeven() {

		if (optionType.equals("CALL")) {

			return strikePrice + premium;

		} else {

			return strikePrice - premium;

		}

	}

	/**
	 * Calculate maximum profit potential
	 */

	public double getMaxProfit() {

		// Long option: unlimited for calls, strike - premium for puts

		if (position.equals("LONG")) {
			if (optionType.equals("CALL")) {

				return Double.POSITIVE_INFINITY; // Unlimited
			} else {// PUT

				return (strikePrice - premium) * contractSize * numberOfContracts;

			}

		} else {// SHORT

			// short option: premium received

			return premium * contractSize * numberOfContracts;
		}

	}

	/**
	 * Calculate maximum loss potential
	 */

	public double getMaxLoss() {

		if (position.equals("LONG")) {

			return premium * contractSize * numberOfContracts;

		} else {// SHORT

			if (optionType.equals("CALL")) {

				return Double.POSITIVE_INFINITY; // unlimited
			} else {

				// if future goes to zero

				return strikePrice - premium;
			}

		}

	}

	/**
	 * Get moneyness status
	 */

	public String getMoneyness() {

		// NEEDS TO BE IMPLEMENTED////
		// double instrinsicValue = calculateIntrinsicValue();
		// double currentFuturesPrice = getCurrentFuturesPrice();

		// dummy values
		double instrinsicValue = Math.max(0, contractPrice - strikePrice);
		double currentFuturesPrice = contractPrice;

		if (instrinsicValue > 0) {

			return "IN_THE_MONEY";
		} else if (Math.abs(currentFuturesPrice - strikePrice) < 0.01) {

			return "AT_THE_MONEY";
		} else {

			return "OUT_OF_THE_MONEY";
		}

	}
	
	
	

//	In-The-Money (ITM):
//
//		Call: Futures Price > Strike Price
//		Put: Futures Price < Strike Price
//		Has intrinsic value
//		Example: 105 call with futures at 112 (ITM by $7)
//
//		At-The-Money (ATM):
//
//		Strike price closest to current futures price
//		Maximum time value
//		Example: 100 strike with futures at 100
//
//		Out-Of-The-Money (OTM):
//
//		Call: Futures Price < Strike Price
//		Put: Futures Price > Strike Price
//		No intrinsic value (only time value)
//		Example: 105 call with futures at 94 (OTM by $11
	
	  /**
     * Check if option has expired worthless
     */
    public boolean expiredWorthless() {
        return hasExpired() && calculateIntrinsicValue() == 0;
    }
	
	

}
