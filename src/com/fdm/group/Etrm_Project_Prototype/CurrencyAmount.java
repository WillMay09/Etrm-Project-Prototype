package com.fdm.group.Etrm_Project_Prototype;


import java.util.Objects;

/**
 * An amount of money in a specific currency.
 *
 * Wraps a double and a currency code so that:
 *   - You cannot add USD amounts to GBP amounts without explicit conversion
 *   - Every monetary value in the system carries its currency
 *   - Reports never silently mix currencies
 *
 * OpenGamma equivalent: com.opengamma.strata.basics.currency.CurrencyAmount
 */

public final class CurrencyAmount {

	private final String currency; // ISO-4217: "USD", "GBP", "EUR"
	
	private final double amount;  // positive = you receive, negative = you pay
	
	private CurrencyAmount(String currency, double amount) {
		
		this.currency = currency;
		this.amount = amount;
		if(currency.isBlank()) {
			
			throw new IllegalArgumentException("Currency must be provided");
		}
	}
	
	public static CurrencyAmount of(String currency, double amount) {
		
		return new CurrencyAmount(currency.toUpperCase(), amount);
	}
	
	public static CurrencyAmount zero(String currency) {
		
		return new CurrencyAmount(currency.toUpperCase(), 0.0);
		
		
	}
	
	public String getCurrency() { return currency; }
    public double getAmount()   { return amount; }

    public boolean isZero()     { return amount == 0.0; }
    public boolean isPositive() { return amount > 0.0; }
    public boolean isNegative() { return amount < 0.0; }
    
    
    public CurrencyAmount plus(CurrencyAmount other) {
    	
    	
    	requireSameCurrency(other);
    	
    	return new CurrencyAmount(other.currency,amount + other.amount);
    }
	
	public CurrencyAmount minus(CurrencyAmount other) {
		
		requireSameCurrency(other);
		
		return new CurrencyAmount(other.currency, amount-other.amount);
		
	}
	
	public CurrencyAmount multipliedBy(double factor) {
		
		return new CurrencyAmount(currency, amount *factor);
		
	}
	
	
	public CurrencyAmount negated() {
		
		
		return new CurrencyAmount(currency,-amount);
	}
	
	public CurrencyAmount abs() {
		
		
		return new CurrencyAmount(currency, Math.abs(amount));
	}
	
	
	private void requireSameCurrency(CurrencyAmount other) {
		
		if(!currency.equals(other.currency)) {
			
			throw new IllegalArgumentException("Cannot combine " + currency + " and " + other.currency +
	                " — convert to a common currency first");
		}
		
		
		
	}
	
	   @Override
	    public boolean equals(Object obj) {
	        if (this == obj) return true;
	        if (!(obj instanceof CurrencyAmount)) return false;
	        CurrencyAmount other = (CurrencyAmount) obj;
	        return currency.equals(other.currency) && Double.compare(amount, other.amount) == 0;
	    }

	    @Override
	    public int hashCode() { return Objects.hash(currency, amount); }

	    @Override
	    public String toString() { return String.format("%s %,.2f", currency, amount); }
	
	
	
	
}
