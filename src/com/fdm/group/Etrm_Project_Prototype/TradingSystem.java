package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class TradingSystem {

	public static void main(String args[]) {

		TradeRepository tradeRepository = new TradeRepository();

		PowerDeal powerDeal1 = new PowerDeal("341343", // dealId
				LocalDate.of(2026, 2, 5), // tradeDate (NOT 02/5/2026)
				"Truist", // counterParty
				700, // quantity
				50.0, // contractPrice (YOU WERE MISSING THIS!)
				"MWh", // unit (changed from "MU")
				"Houston", // deliveryLocation
				LocalDate.of(2026, 2, 25), // deliveryStartDate
				LocalDate.of(2026, 2, 29), // deliveryEndDate
				"Freight", // transportMethod
				16 // deliveryHour
		);

		tradeRepository.addDeals(powerDeal1);

	}

}
