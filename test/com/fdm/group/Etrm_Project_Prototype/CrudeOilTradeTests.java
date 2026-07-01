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

@DisplayName("CrudeOilOption Trade level tests")
public class CrudeOilTradeTests {

	
	@BeforeEach
	void setup() {
		
		

		CrudeOilOption option1 = CrudeOilOption.builder().call().contractSize(2_000).currency("EURO")
				.expiryDate(LocalDate.now().plusMonths(6)).build();
	}

	@Test
	void getInfo_returnsTradeInfo_notNull() {

		TradeInfo optionInfo1 = TradeInfo.builder().standardId("TRD-001").tradeDate(LocalDate.now()).build();

		CrudeOilOptionTrade optionTrade1 = CrudeOilOptionTrade.builder().info(optionInfo1).build();

		assertNotNull(optionTrade1.getInfo());

		assertEquals("TRD-001", optionTrade1.getInfo().getStandardId().orElseThrow());

	}

	@Test
	void getTotalPremium_longPosition_isNegative() {
		// Regression test for the inverted sign bug

	}

	@Test
	void getTotalPremium_shortPosition_isPositive() {

	}

	@Test
	void getTimeToExpiry_isInYears_NotDays() {
		CrudeOilOption option2 = CrudeOilOption.builder().call().strike(80).expiryDate(LocalDate.now().plusDays(365))
				.build();

		CrudeOilOptionTrade optionTrade2 = CrudeOilOptionTrade.builder()
				.info(TradeInfo.builder().tradeDate(LocalDate.now()).build())
				.product(option2).longPosition(1).premiumPerUnit(1.0).build();
		
		
		assertEquals(1.0, optionTrade2.getTimetoExpiry(), 0.01, "365 days should be ~1.0 years, not 365.0");
		

	}
	
	
	@Test
	
	void payoffAt_call_inTheMoney() {
		
		
		
		
	}
}
