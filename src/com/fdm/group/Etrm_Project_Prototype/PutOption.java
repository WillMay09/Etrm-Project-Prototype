package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class PutOption extends OptionDeal {

	public PutOption(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double contractPrice, String underlyingCommodity, LocalDate expiryDate, double strikePrice,
			String optionType, double premium, String position, int numberOfContracts) {
		super(dealId, tradeDate, counterParty, quantity, unit, contractPrice, underlyingCommodity, expiryDate, strikePrice,
				optionType, premium, position, numberOfContracts);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double calculatePayoffAtExpiry() {
		//double futuresPrice = getFuturesPrice();
		double futuresPrice = contractPrice;
		double instrinsic = Math.max(0, strikePrice-futuresPrice);
		return instrinsic * contractSize * numberOfContracts;
	}
	


	@Override
	public boolean shouldExcercise() {
		//if Call option is in the money
	
		double futuresPrice = contractPrice;
		
		return futuresPrice<strikePrice;
	}
	
	
	
	

}
