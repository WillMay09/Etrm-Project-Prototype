package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PortfolioGreeksEngine {
	
	private final BlackScholesPricer  pricer;
	
	private final GreeksCalculator greeks;
	
	private final MarketDataProvider marketData;
	
	
	
	
	public PortfolioGreeks calculate(Portfolio portfolio) {
		
		double totalDelta = 0; 
		double totalGamma = 0;
		double totalVega = 0;
		double totalTheta = 0;
		double totalRho = 0;
		
		
		for( Position position: portfolio.getPositions()) {
			
			
			double spot = marketData.getSpotPrice(position.getUnderlying);
			
			if(position.isPhysical()) {
				
				totalDelta += position.getQuantity();
				continue;
			}
			
			// Pull vol from the surface for THIS strike and expiry
            // This is why you built VolatilitySurface
			double vol = marketData.getVolatilitySurface(position.underlying())
					.getVolatility(position.getExpiry(), position.getStrike());
			
			double rate = 0.05;
			double time = ChronoUnit.DAYS.between(LocalDate.now(), position.getExpiry()) / 365.0;
			
			
			 GreeksCalculator.OptionGreeks g = greeksCalc.calculateAllGreeks(
		                spot, position.getStrike(), time, vol, rate, position.isCall()
		            );
			 double qty = position.getQuantity();
	            totalDelta += qty * g.getDelta();
	            totalGamma += qty * g.getGamma();
	            totalVega  += qty * g.getVega();
	            totalTheta += qty * g.getTheta();
	            totalRho   += qty * g.getRho();
		}
		
		  return new PortfolioGreeks(totalDelta, totalGamma,
                  totalVega,  totalTheta, totalRho);
		
		
		
	}
	
	
	
	
	private static class portfolio {
		
		public portfolio() {
			
			
		}
	}
	

}
