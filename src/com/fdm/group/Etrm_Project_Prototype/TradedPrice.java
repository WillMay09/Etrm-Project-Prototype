package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.Objects;

/**
 * The price agreed when a trade was executed.
 *
 * This is a historical fact — it never changes after the trade.
 * It is separate from TradeInfo because:
 *   - Options:  the price is the premium paid (e.g. $3.50/barrel)
 *   - Futures:  the price is the entry price locked in (e.g. $78.50)
 *   - Physical: the price is the agreed forward price
 *
 * It is separate from current market price, which changes constantly.
 *
 * OpenGamma source: TradedPrice holds tradeDate + price.
 * We extend this slightly for ETRM by adding currency and priceUnit.
 *
 * Note: Position does NOT have a TradedPrice because a position
 * aggregates multiple trades at different prices — there is no
 * single agreed price at the position level.
 */

public final class TradedPrice {
	
	private final LocalDate tradeDate;// when execution occurred
    private final double    price;         // agreed price at execution
    private final String    currency;      // "USD"
    private final String    priceUnit;     // "USD/BBL", "USD/MMBTU"

    
    private TradedPrice(LocalDate tradeDate, double price, String currency, String priceUnit) {
    	
    	this.tradeDate = Objects.requireNonNull(tradeDate, "tradeDate required");
    	this.price = price;
    	this.currency = currency;
    	this.priceUnit = priceUnit;
    	
    	if(price<= 0 ) {
    		
    		throw new IllegalArgumentException("Price must be greater than 0");
    	}
    	
    	
    }
    
    public static TradedPrice of(LocalDate tradeDate, double price, String currency, String priceUnit) {
    	
    	
    	return new TradedPrice(tradeDate, price, currency, priceUnit);
    	
    }
    
    
    public LocalDate getTradeDate()  { return tradeDate; }
    public double    getPrice()      { return price; }
    public String    getCurrency()   { return currency; }
    public String    getPriceUnit()  { return priceUnit; }
    
    public double notionalValue(double quantity, double contractSize) {
    	
    	double notional = quantity * price * contractSize;
    	
    	return notional;
    }
    
    @Override
    public String toString() {
        return String.format("TradedPrice[%s @ %.4f %s]", tradeDate, price, priceUnit);
    }
    
    
}
