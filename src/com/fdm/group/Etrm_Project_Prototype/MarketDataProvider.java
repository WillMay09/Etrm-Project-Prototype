package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketDataProvider {
	
	
	private final LocalDate valuationDate;
	
	
	private final Map<String, Double> spotPrices;
	
	private final Map<String, PriceCurve> forwardCurves;
	
	private final Map<String, VolatilitySurface> volatilitySurfaces;
	
	
	private final Map<String, TimeSeries> historicalData;
	
	
	

    // ===== CONSTRUCTOR =====
	
	public MarketDataProvider(LocalDate valuationDate) {
		
		this.valuationDate = valuationDate;
		this.spotPrices = new ConcurrentHashMap<>();
		this.forwardCurves = new ConcurrentHashMap<>();
		this.volatilitySurfaces = new ConcurrentHashMap<>();
		this.historicalData = new ConcurrentHashMap<>();
		
		
		
	}
	
    // ===== CORE METHODS =====
	
	
	/**
     * Get current spot price for a commodity
     */
	
	public double getSpotPrice(String commodity) {
		
		if(!spotPrices.containsKey(commodity)) {
			
			//need to implement custom exception
			
			//throw new MarketDataNotFoundException("No spot price for: " + commodity);
			
			
		}
		
		
		return spotPrices.get(commodity);
		
	}
	
	

}
