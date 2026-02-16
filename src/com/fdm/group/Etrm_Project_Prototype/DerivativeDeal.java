package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Abstract class for all derivative instruments (futures, options, swaps)
 * Extends EnergyTrade
 */

public abstract class DerivativeDeal extends EnergyTrade {

	// === DERIVATIVE-SPECIFIC DATA ===
	protected String underlyingCommodity; // "CRUDE_OIL", "NATURAL_GAS", "GOLD"
	protected LocalDate expiryDate; // When the derivative expires
	protected double strikePrice; // For options/futures

	public DerivativeDeal(String dealId, LocalDate tradeDate, String counterParty,
            double quantity, String unit, double contractPrice,
            String underlyingCommodity, LocalDate expiryDate,
            double strikePrice) {

		super(dealId, tradeDate, counterParty, quantity, unit, contractPrice);

		if (underlyingCommodity == null || underlyingCommodity.isEmpty()) {

			throw new IllegalArgumentException("underlyingCommodity cannot be null");
		}
		if (expiryDate == null) {
			throw new IllegalArgumentException("Expiry date required");
		}
		if (expiryDate.isBefore(tradeDate)) {
			throw new IllegalArgumentException("Expiry date must be after trade date");
		}

		this.underlyingCommodity = underlyingCommodity;
		this.expiryDate = expiryDate;
		this.strikePrice = strikePrice;
	}

	// === ABSTRACT METHODS (specific to derivatives) ===

	/**
	 * Calculate intrinsic value (payoff if exercised/settled now)
	 */
	public abstract double calculateIntrinsicValue();

	/**
	 * Calculate time value (value from time remaining until expiry)
	 */
	public abstract double calculateTimeValue();

	/**
	 * Check if derivative is in the money
	 */
	public abstract boolean isInTheMoney();

	// === CONCRETE METHODS FROM PARENT ===

	@Override
	public String getInstrumentType() {
		return "DERIVATIVE";
	}

	@Override
	public String getCommodityType() {
		// For derivatives, commodity type is the underlying
		return underlyingCommodity;
	}

	// === DERIVATIVE-SPECIFIC METHODS ===

	/**
	 * Get days until expiry
	 */
	
	public long getDaysUntilExpiry() {
		
		return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
	}
	
	/**
     * Check if derivative is near expiry (within 7 days)
     */

	public boolean isNearExpiry() {

		return getDaysUntilExpiry() <=7;

	}
	
	/**
     * Check if derivative has expired
     */
    public boolean hasExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
    
    
 // === PARENT METHODS
    
    
    /**
     * Override parent's isExpired to use expiry date instead of trade date
     */
    
    @Override
    public boolean isExpired() {
        return hasExpired();
    }

    /**
     * Override parent's validate to add derivative checks
     */
    @Override
    public void validate() {
        super.validate();  // Call parent validation
        
        if (underlyingCommodity == null || underlyingCommodity.trim().isEmpty()) {
            throw new IllegalStateException("Underlying commodity required");
        }
        if (expiryDate == null) {
            throw new IllegalStateException("Expiry date required");
        }
        if (hasExpired()) {
            throw new IllegalStateException("Derivative has expired");
        }
    }
    
    // === GETTERS ===
    
    public String getUnderlyingCommodity() {
        return underlyingCommodity;
    }
    
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
    
    public double getStrikePrice() {
        return strikePrice;
    }
    
    // === SETTERS ===
    
    
    public void setUnderlyingCommodity(String underlyingCommodity) {
    	
    	if(underlyingCommodity == null || underlyingCommodity.trim().isEmpty()) {
    		
    		throw new IllegalArgumentException("underlyingCommodity required");
    		
    	}
    	
    	this.underlyingCommodity = underlyingCommodity;
    }
    
    public void setExpiryDate(LocalDate expiryDate) {
    	
    	if (expiryDate == null) {
            throw new IllegalArgumentException("Expiry date cannot be null");
        }
        if (expiryDate.isBefore(tradeDate)) {
            throw new IllegalArgumentException("Expiry date must be after trade date");
        }
        this.expiryDate = expiryDate;
    }
    
    public void setStrikePrice(double strikePrice) {
        if (strikePrice <= 0) {
            throw new IllegalArgumentException("Strike price must be positive");
        }
        this.strikePrice = strikePrice;
    }
    
    
	

}
