package com.fdm.group.Etrm_Project_Prototype;

public final class CrudeOilFutureProductPricer {
	
	public static final CrudeOilFutureProductPricer DEFAULT = new CrudeOilFutureProductPricer();
	
	
	 /**
     * Present value of the futures position.
     *
     * PV = (currentForwardPrice - entryPrice) × scaledQuantity × discountFactor
     *
     * The discount factor brings the future P&L back to today's dollars.
     * At delivery, discountFactor = 1.0 so PV = raw P&L exactly.
     *
     * Sign: positive = unrealised gain (long position, price has risen)
     *       negative = unrealised loss
     */
	
	public CurrencyAmount presentValue(ResolvedCrudeOilFuture resolved) {
		
		double pnlPerBarrel = (resolved.getCurrentForwardPrice() - resolved.getEntryPrice());
		
		double pv = pnlPerBarrel * resolved.getScaledQuantity() * resolved.getDiscountFactor();
		
		return CurrencyAmount.of(resolved.getCurrency(), pv);
		
		
	}
	
	 /**
     * Forecast value — undiscounted P&L (what cash would move at delivery).
     * PV = forecastValue × discountFactor
     */
	
	public CurrencyAmount forecastValue(ResolvedCrudeOilFuture resolved) {
		
		double pnlPerBarrel = (resolved.getCurrentForwardPrice() - resolved.getEntryPrice());
		
		double fv = pnlPerBarrel * resolved.getScaledQuantity();
		
		return CurrencyAmount.of(resolved.getCurrency(), fv);
		
	}
	
	/**
     * Delta — sensitivity of PV to a $1 move in the forward price.
     *
     * For a futures contract, delta = scaledQuantity × discountFactor.
     * It is always linear — no N(d1), no optionality.
     *
     * e.g. Long 10 lots of 1000 barrels = delta of 10,000 barrels
     * A $1 move in forward price changes PV by approximately $10,000
     */
	
	
	public CurrencyAmount delta(ResolvedCrudeOilFuture resolved) {
		
		double delta = resolved.getScaledQuantity() * resolved.getDiscountFactor();
		
		return CurrencyAmount.of(resolved.getCurrency(), delta);
	}
	
	/**
     * Current cash — variation margin due today.
     *
     * In practice this requires yesterday's settlement price, which lives
     * in MarketDataProvider.getHistoricalPrice(). Passed as a parameter here.
     *
     * variationMargin = (todayPrice - yesterdayPrice) × scaledQuantity
     * No discounting — this is actual cash, not a future value.
     */
	
	public CurrencyAmount currentCash(ResolvedCrudeOilFuture resolved, double previousSettlementPrice) {
		
		double margin = (resolved.getCurrentForwardPrice() - previousSettlementPrice) * resolved.getScaledQuantity();
		
		return CurrencyAmount.of(resolved.getCurrency(), margin);
		
	}

	
	
	
	
	
	
	
	
	
	

}
