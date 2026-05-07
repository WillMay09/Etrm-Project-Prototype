package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public class VolatilityCalculator {

	
	private static final int TRADING_DAYS_PER_YEAR = 252;
	
	/**
     * Calculate historical volatility from price series
     * 
     * @param prices TimeSeries of historical prices
     * @return Annualized volatility (as decimal, e.g., 0.25 for 25%)
     */
	
	public static double calculateHistoricalVolatility(TimeSeries prices) {
		
		
		List<Double> returns = calculateLogReturns(prices);
		
		if(returns.size() < 2) {
			
			
			throw new IllegalArgumentException("Need atleast 2 returns for Volatility");
		}
		
		//calculate mean returns
		
		double meanReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
		
		//calculate variance(derivation from the mean squared, average of these values)
		double variance = returns.stream().mapToDouble(r -> Math.pow(r-meanReturn, 2)).average().orElse(0.0);
		
		double dailyVolatility = Math.sqrt(variance);
		
		double annualVolatility = dailyVolatility * Math.sqrt(TRADING_DAYS_PER_YEAR);
		
		
		return annualVolatility;
	}
	
	
	 /**
     * Calculate logarithmic returns from prices
     */
	
	public static List<Double> calculateLogReturns(TimeSeries prices){
		
		
		
		List<Double> priceList = new ArrayList<>(prices.getAllValues());
		List<Double> returns = new ArrayList<>();
		
		
		for( int i=1; i<priceList.size(); i++) {
			
			double logReturn = Math.log(priceList.get(i)/priceList.get(i-1));
			
			returns.add(logReturn);
			
		}
		
		return returns;
		
	}
	
	
	
	public static void main(String[] args) {
		
		
		TimeSeries prices = TimeSeries.builder()
	            .commodity("CRUDE_OIL")
	            .addPoint(LocalDate.of(2026, 1, 1), 100.00)
	            .addPoint(LocalDate.of(2026, 1, 2), 102.00)
	            .addPoint(LocalDate.of(2026, 1, 3), 101.00)
	            .addPoint(LocalDate.of(2026, 1, 4), 103.00)
	            .addPoint(LocalDate.of(2026, 1, 5), 102.50)
	            .createTimeSeries();
		
		//Calculate volatility
		
		double vol = calculateHistoricalVolatility(prices);
		
        System.out.printf("Annualized volatility: %.2f%%%n", vol * 100);
        // Output: Annualized volatility: 25.20%

		
		
	}
}
