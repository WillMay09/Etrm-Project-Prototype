package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class CallOption extends OptionDeal {

	public CallOption(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double contractPrice, String underlyingCommodity, LocalDate expiryDate, double strikePrice, double premium,
			String position, int numberOfContracts) {

		super(dealId, tradeDate, counterParty, quantity, unit, contractPrice, underlyingCommodity, expiryDate,
				strikePrice, "CALL", premium, position, numberOfContracts);
	}
	
	


	@Override
	public double calculatePayoffAtExpiry() {
		//double futuresPrice = getFuturesPrice();
		double futuresPrice = contractPrice;
		double instrinsic = Math.max(0, futuresPrice-strikePrice);
		return instrinsic * contractSize * numberOfContracts;
	}
	


	@Override
	public boolean shouldExcercise() {
		//if Call option is in the money
	
		double futuresPrice = contractPrice;
		
		return futuresPrice>strikePrice;
	}
}
