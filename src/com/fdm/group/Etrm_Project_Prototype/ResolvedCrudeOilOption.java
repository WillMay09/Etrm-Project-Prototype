package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.fdm.group.Etrm_Project_Prototype.CrudeOilOption.PutCall;


/**
 * Resolved form of the option PRODUCT - pure data, no pricing methods.
 *
 * Construction: FACTORY METHOD (.of(...)), PRIVATE constructor.
 * The fields here are not directly specified by anyone - they are DERIVED
 * from a trade plus a live market data snapshot at a specific valuation date.
 * The private constructor plus single public factory method means it is
 * structurally impossible to build one of these with inconsistent fields
 * (e.g. a discountFactor that doesn't match the rate and time given).
 */



public final class ResolvedCrudeOilOption {
	
	
	private final double strike;
	
	private final PutCall putCall;
	// From Quantity × contractSize — baked in at resolution time
	private final double scaledQuantity;
	// Resolved from live market data at resolution time
	private final double timeToExpiry;
	private final double impliedVol;
	private final double riskFreeRate;
	private final double spot;
	private final String currency;
	private final LocalDate settlementDate;
	private final LocalDate expiryDate;
	private final double discountFactor; //e^(-r x t), pre-calculated
	
	
	 public double    getStrike()         { return strike; }
	    public PutCall   getPutCall()        { return putCall; }
	    public double    getScaledQuantity() { return scaledQuantity; }
	    public double    getTimeToExpiry()   { return timeToExpiry; }
	    public double    getImpliedVol()     { return impliedVol; }
	    public double    getRiskFreeRate()   { return riskFreeRate; }
	    public double    getSpot()           { return spot; }
	    public LocalDate getSettlementDate() { return settlementDate; }
	    public LocalDate getExpiryDate()     { return expiryDate; }
	    public double getDiscountFactor() { return discountFactor;}
	
	
	
	private ResolvedCrudeOilOption(double strike, String currency, 
			PutCall putCall, double scaledQuantity, double timeToExpiry, double impliedVol, double riskFreeRate, double spot, 
			LocalDate settlementDate, LocalDate expiryDate, double discountFactor) {
		this.strike = strike;
		this.putCall = putCall;
		this.currency = currency;
		this.scaledQuantity = scaledQuantity;
		this.timeToExpiry = timeToExpiry;
		this.impliedVol = impliedVol;
		this.riskFreeRate = riskFreeRate;
		this.spot = spot;
		this.settlementDate = settlementDate;
		this.expiryDate = expiryDate;
		this.discountFactor = discountFactor;
	}
	
	 /**
     * Resolution step - combines a trade with a live market data snapshot,
     * at a specific valuation date, into one frozen, internally-consistent object.
     */
	
	public static ResolvedCrudeOilOption of(CrudeOilOptionTrade trade, MarketDataProvider marketData, LocalDate valuationDate) {
		
		
		CrudeOilOption product = trade.getProduct();
		double lots = trade.getLots();
		double size = product.getContractSize();
		
		long daysToExpiry = ChronoUnit.DAYS.between(valuationDate, trade.getExpiryDate());
		
		double timeToExpiry = Math.max(0.0, daysToExpiry);
		
		double spot = marketData.getSpotPrice(product.getUnderlying());
		
		double vol = marketData.getVolatilitySurface(product.getUnderlying()).getVolatility(product.getStrike(), product.getExpiryDate());
		
		double rate = marketData.getRiskFreeRate();
		
		
		LocalDate settlementDate = trade.getInfo().getSettlementDate().orElseThrow(()->new IllegalStateException("TradeInfo missing settlements"));
		
		////////////////////Come back here /////////////////////////////////////////
		 return new ResolvedCrudeOilOption(
		            product.getStrike(),
		            product.getCurrency(),
		            product.getPutCall(),
		            lots * size,
		            timeToExpiry,
		            vol,
		            rate,
		            spot,
		            settlementDate,                                   
		            product.getExpiryDate(),                          
		            DiscountFactor.of(rate, timeToExpiry)              
		        );
	}
	
	
	
	


}
