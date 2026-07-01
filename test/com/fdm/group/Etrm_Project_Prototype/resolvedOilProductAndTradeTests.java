package com.fdm.group.Etrm_Project_Prototype;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;



public class resolvedOilProductAndTradeTests {
	
	private LocalDate valuationDate;
	private VolatilitySurface surface;
	
	@BeforeEach
	void setup() {
		valuationDate = LocalDate.of(2026, 3, 21);
		 surface = VolatilitySurface.builder()
	                .commodity("CRUDE_OIL")
	                .valuationDate(valuationDate)
	                .addVolatility(80.0, LocalDate.of(2026, 6, 19), 0.30)
	                .addVolatility(90.0, LocalDate.of(2026, 6, 19), 0.26)
	                .addVolatility(100.0, LocalDate.of(2026, 6, 19), 0.25)
	                .addVolatility(110.0, LocalDate.of(2026, 6, 19), 0.27)
	                .build();
	            
		
	}
	
	@Test
	void resolve_pullsAllFieldsFromCorrectSources() {
		
		MarketDataProvider mockData = MarketDataProvider.builder()
				.addSpotPrice("CRUDE_OIL", 92.50)
				.addVolatilitySurface("CRUDE_OIL", surface)
				.riskFreeRate(0.0553);
				.build();
	}
	
	
	
	
	

}
