package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.fdm.group.Etrm_Project_Prototype.CrudeOilFuture.SettlementType;
import com.fdm.group.Etrm_Project_Prototype.CrudeOilOption.PutCall;


/**
 * Resolved form of a crude oil futures position.
 * A market snapshot for ONE valuation date — the equivalent of
 * ResolvedCrudeOilOption but for futures.
 *
 * Futures pricing is fundamentally different from options:
 *   Options:  require a model (Black-76) to compute fair value from vol, forward, strike
 *   Futures:  P&L = (currentPrice - entryPrice) × scaledQuantity
 *             No vol, no model, no optionality — linear and deterministic
 *
 * OpenGamma equivalent: ResolvedEtdFutureTrade
 * The pricer (DiscountingEtdFutureTradePricer) calculates based on the
 * difference between the current model price and the last settlement price,
 * or the trade price if traded on the valuation date.
 */


public final class ResolvedCrudeOilFuture {

	

	// ── From the product (instrument definition) ──────────────────────────
	private final String underlying;
	private final LocalDate deliveryDate;
	private final double contractSize;
	private final String currency;
	private final SettlementType settlementType; // from CrudeOilFuture, not redefined
	
	
	// ── From the trade (business context) ────────────────────────────────
	private final double entryPrice; // agreed price at trade time (TradedPrice)
	private final double scaledQuantity; // signed: lots × contractSize
	
	
	// ── Resolved from market data at this valuation date ─────────────────
	private final double currentForwardPrice; // from PriceCurve for deliveryDate
    private final double timeToDelivery;      // years from valuationDate to deliveryDate
	private final double riskFreeRate;        // from MarketDataProvider
	private final double discountFactor;      // e^(-r × T), via DiscountFactor.of()
	
	
	//Private constructor - reserved for factory method
	  private ResolvedCrudeOilFuture(
		        String underlying, LocalDate deliveryDate, double contractSize,
		        String currency, SettlementType settlementType,
		        double entryPrice, double scaledQuantity,
		        double currentForwardPrice, double timeToDelivery,
		        double riskFreeRate, double discountFactor
		    ) {
		        this.underlying           = underlying;
		        this.deliveryDate         = deliveryDate;
		        this.contractSize         = contractSize;
		        this.currency             = currency;
		        this.settlementType       = settlementType;
		        this.entryPrice           = entryPrice;
		        this.scaledQuantity       = scaledQuantity;
		        this.currentForwardPrice  = currentForwardPrice;
		        this.timeToDelivery       = timeToDelivery;
		        this.riskFreeRate         = riskFreeRate;
		        this.discountFactor       = discountFactor;
		    }
	
	 

	// ── Getters ───────────────────────────────────────────────────────────
    public String          getUnderlying()          { return underlying; }
    public LocalDate       getDeliveryDate()         { return deliveryDate; }
    public double          getContractSize()          { return contractSize; }
    public String          getCurrency()              { return currency; }
    public SettlementType  getSettlementType()        { return settlementType; }
    public double          getEntryPrice()            { return entryPrice; }
    public double          getScaledQuantity()        { return scaledQuantity; }
    public double          getCurrentForwardPrice()   { return currentForwardPrice; }
    public double          getTimeToDelivery()        { return timeToDelivery; }
    public double          getRiskFreeRate()          { return riskFreeRate; }
    public double          getDiscountFactor()        { return discountFactor; }
    
    
    /**
     * Factory method — resolves a trade against live market data.
     * Static, private constructor — the only door in.
     */
   

	public static ResolvedCrudeOilFuture of(MarketDataProvider marketData, CrudeOilFutureTrade trade, LocalDate valuationDate) {
		
		CrudeOilFuture product = trade.getProduct();
		
		//Time to delivery in years
		long days = ChronoUnit.DAYS.between(valuationDate, product.getDeliveryDate());
		double timeToDelivery = Math.max(0.0, days/365.0);
		
		double rate = marketData.getRiskFreeRate();
		
		
		double currentForwardPrice = marketData.getForwardPrice(product.getUnderlying(), product.getDeliveryDate());
		
		double size = product.getContractSize();
		double lots = trade.getLots();
		
		double scaledQuantity = size * lots;
		
		double discountFactor = DiscountFactor.of(rate, timeToDelivery);
		
		 return new ResolvedCrudeOilFuture(
		            product.getUnderlying(),
		            product.getDeliveryDate(),
		            product.getContractSize(),
		            product.getCurrency(),
		            product.getSettlementType(),
		            trade.getEntryPrice(),       // from TradedPrice
		            scaledQuantity,
		            currentForwardPrice,
		            timeToDelivery,
		            rate,
		            discountFactor
		        );
	}

}
