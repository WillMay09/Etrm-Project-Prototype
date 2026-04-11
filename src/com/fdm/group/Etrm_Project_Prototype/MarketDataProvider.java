package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataProvider {
	
	
	private final LocalDate valuationDate;
	
	/** Current spot prices by commodity */
	private final Map<String, Double> spotPrices;
	
	/** Forward price curves by commodity */
	private final Map<String, PriceCurve> forwardCurves;
	
	/** Volatility surfaces by commodity */
	private final Map<String, VolatilitySurface> volatilitySurfaces;
	
	/** Historical price time series by commodity */

	private final Map<String, TimeSeries> historicalData;
	 
	private final Map<String, Object> metaData;
	

    // ===== CONSTRUCTOR =====

	
	
	private MarketDataProvider(Builder builder) {
		
		this.valuationDate = builder.valuationDate;
		this.spotPrices = Map.copyOf(builder.spotPrices);
		this.forwardCurves = Map.copyOf(builder.forwardCurves);
		this.volatilitySurfaces = Map.copyOf(builder.volatilitySurfaces);
		this.historicalData = Map.copyOf(builder.historicalData);
		this.metaData = Map.copyOf(builder.metaData);
		
	}
	
	
	 // =========================================================================
    // Static Factory
    // =========================================================================
    
	
	/**
     * Creates a new Builder for MarketDataProvider
     * 
     * @return New Builder instance
     */
	
	public static Builder builder(){
		
		return new Builder();
		
	}
	
	
	

		
		 // =========================================================================
	    // Core Getters
	    // =========================================================================
	    
	    /**
	     * Get valuation date
	     * 
	     * @return Valuation date
	     */
		
	public LocalDate getValuationDate() {
		
		
		return valuationDate;
		
	}
	
	
	/**
     * Get spot price for a commodity
     * 
     * @param commodity Commodity identifier
     * @return Spot price
     * @throws IllegalArgumentException if commodity not found
     */
	
	
	public double getSpotPrice(String commodity) {
		
		
		if(!spotPrices.containsKey(commodity)) {
			
			throw new IllegalArgumentException("No spot price for commodity" + commodity);
			
		}
		
		return spotPrices.get(commodity);
		
	}
	
	/**
     * Get forward price curve for a commodity
     * 
     * @param commodity Commodity identifier
     * @return Forward price curve
     * @throws IllegalArgumentException if commodity not found
     */
    public PriceCurve getForwardCurve(String commodity) {
        if (!forwardCurves.containsKey(commodity)) {
            throw new IllegalArgumentException("No forward curve for commodity: " + commodity);
        }
        return forwardCurves.get(commodity);
    }
	
	  /**
     * Get volatility surface for a commodity
     * 
     * @param commodity Commodity identifier
     * @return Volatility surface
     * @throws IllegalArgumentException if commodity not found
     */
    public VolatilitySurface getVolatilitySurface(String commodity) {
        if (!volatilitySurfaces.containsKey(commodity)) {
            throw new IllegalArgumentException("No volatility surface for commodity: " + commodity);
        }
        return volatilitySurfaces.get(commodity);
    }
    
    /**
     * Get historical time series for a commodity
     * 
     * @param commodity Commodity identifier
     * @return Historical time series
     * @throws IllegalArgumentException if commodity not found
     */
    public TimeSeries getHistoricalData(String commodity) {
        if (!historicalData.containsKey(commodity)) {
            throw new IllegalArgumentException("No historical data for commodity: " + commodity);
        }
        return historicalData.get(commodity);
    }
		

    // =========================================================================
    // Convenience Methods
    // =========================================================================
    
    /**
     * Get forward price for a specific delivery date
     * 
     * @param commodity Commodity identifier
     * @param deliveryDate Delivery date
     * @return Forward price
     */
    
    public double getForwardPrice(String commodity, LocalDate deliveryDate) {
    	
    	return getForwardCurve(commodity).getPrice(deliveryDate);
    	
    }
		
    /**
     * Get implied volatility for a specific strike and expiry
     * 
     * @param commodity Commodity identifier
     * @param expiry Option expiry date
     * @param strike Strike price
     * @return Implied volatility
     */
    
    public double getVolatility(String commodity, LocalDate expiryDate, double strikePrice) {
    	
    	
    	VolatilitySurface volSurf = volatilitySurfaces.get(commodity);
    	
    	double volatility = volSurf.getVolatility(strikePrice, expiryDate);
    	
    	return volatility;
    }
    
    /**
     * Check if spot price exists for commodity
     * 
     * @param commodity Commodity identifier
     * @return true if spot price exists
     */
    public boolean hasSpotPrice(String commodity) {
        return spotPrices.containsKey(commodity);
    }
    
    /**
     * Check if forward curve exists for commodity
     * 
     * @param commodity Commodity identifier
     * @return true if forward curve exists
     */
    public boolean hasForwardCurve(String commodity) {
        return forwardCurves.containsKey(commodity);
    }
    
    /**
     * Check if volatility surface exists for commodity
     * 
     * @param commodity Commodity identifier
     * @return true if volatility surface exists
     */
    public boolean hasVolatilitySurface(String commodity) {
        return volatilitySurfaces.containsKey(commodity);
    }
    
    /**
     * Check if historical data exists for commodity
     * 
     * @param commodity Commodity identifier
     * @return true if historical data exists
     */
    public boolean hasHistoricalData(String commodity) {
        return historicalData.containsKey(commodity);
    }
		
		
	
	
    // ===== CORE METHODS =====
	
	
	public static class Builder{
		
		private LocalDate valuationDate;
		private Map<String, Double> spotPrices = new HashMap<>();
		private Map<String, PriceCurve> forwardCurves = new HashMap<>();
		
		private Map<String, VolatilitySurface> volatilitySurfaces = new HashMap<>();
		private Map<String, TimeSeries> historicalData = new HashMap<>();
		private Map<String, Object> metaData = new HashMap<>();
		
		 private Builder() {
				
				
			}
		
		 /**
         * Set valuation date
         * 
         * @param valuationDate Date for which market data is valid
         * @return this Builder
         */
		public Builder valuationDate(LocalDate valuationDate) {
			
			this.valuationDate = valuationDate;
			
			return this;
			
			
		}
		
		/**
         * Add spot price for a commodity
         * 
         * @param commodity Commodity identifier
         * @param price Spot price
         * @return this Builder
         */
		
		public Builder addSpotPrice(String commodity, double spotPrice) {
			
			
			if(commodity == null) {
				
				throw new IllegalArgumentException("Commodity must not be null");
			}
			if(spotPrice < 0) {
				
				throw new IllegalArgumentException("spotPrice cannot be negative" + spotPrice);
			}
			
			
			this.spotPrices.put(commodity, spotPrice);
			
			return this;
			
		}
		
		
		/**
         * Add forward curve for a commodity
         * 
         * @param commodity Commodity identifier
         * @param curve Forward price curve
         * @return this Builder
         */
		
		
		public Builder addForwardCurve(String commodity, PriceCurve curve) {
			
			Objects.requireNonNull(curve, "Forward curve cannot be null");
			
			this.forwardCurves.put(commodity, curve);
			
			return this;
		}
		
		   /**
         * Add volatility surface for a commodity
         * 
         * @param commodity Commodity identifier
         * @param surface Volatility surface
         * @return this Builder
         */
        public Builder addVolatilitySurface(String commodity, VolatilitySurface surface) {
            Objects.requireNonNull(surface, "Volatility surface cannot be null");
            this.volatilitySurfaces.put(commodity, surface);
            return this;
        }
        
        /**
         * Add historical time series for a commodity
         * 
         * @param commodity Commodity identifier
         * @param timeSeries Historical price data
         * @return this Builder
         */
        public Builder addHistoricalData(String commodity, TimeSeries timeSeries) {
            Objects.requireNonNull(timeSeries, "Time series cannot be null");
            this.historicalData.put(commodity, timeSeries);
            return this;
        }
        
        /**
         * Add metadata
         * 
         * @param key Metadata key
         * @param value Metadata value
         * @return this Builder
         */
        public Builder metadata(String key, Object value) {
            this.metaData.put(key, value);
            return this;
        }
        
        // =====================================================================
        // Bulk Setters (for toBuilder())
        // =====================================================================
        
        
        private Builder spotPrices(Map<String, Double> prices) {
        	
        	
        	this.spotPrices.putAll(prices);
        	
        	
        	return this;
        }
        
        
        private Builder forwardCurves(Map<String, PriceCurve> curves) {
        	
        	
        	this.forwardCurves.putAll(curves);
        	
        	
        	return this;
        }
        
        private Builder historicalData(Map<String, TimeSeries> data) {
            this.historicalData.putAll(data);
            return this;
        }
        
        private Builder metadata(Map<String, Object> meta) {
            this.metaData.putAll(meta);
            return this;
        }
        
        
        
       
        
        
     // =====================================================================
        // Build
        // =====================================================================
        
        /**
         * Build immutable MarketDataProvider
         * 
         * @return MarketDataProvider instance
         * @throws IllegalStateException if validation fails
         */
        
        public MarketDataProvider build() {
    		
    		if(valuationDate == null) {
    			
    			throw new IllegalStateException("Valuation date is required");
    		}
    		
    		if(spotPrices.isEmpty() && forwardCurves.isEmpty() && volatilitySurfaces.isEmpty() && historicalData.isEmpty()) {
    			
    			throw new IllegalStateException("Must contain at least one type of market Data");
    		}
    		
    		
    		return new MarketDataProvider(this);
    		
    		
    	}
	}
	
//	@Override
//    public String toString() {
//        return String.format(
//            "MarketDataProvider[valuationDate=%s, commodities=%d, spots=%d, curves=%d, surfaces=%d, history=%d]",
//            valuationDate,
//            getCommodities().size(),
//            spotPrices.size(),
//            forwardCurves.size(),
//            volatilitySurfaces.size(),
//            historicalData.size()
//        );
//    }
//	
	
	
	
	
	
	
	

}
