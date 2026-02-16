package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public class FutureContract extends DerivativeDeal {

	// === FUTURES-SPECIFIC DATA ===
	private String contractSymbol; // "CLZ24" (Crude Oil Dec 2024)
	private String exchange; // "NYMEX", "CME", "ICE"
	private String commodityGrade; // "Light Sweet", "WTI"
	private String deliveryLocation; // "Cushing, Oklahoma"
	private String deliveryMonth; // "DEC24"
	private LocalDate lastTradingDay; // When trading stops

	// Contract specifications
	private double contractSize; // 1000 barrels
	private int numberOfContracts; // How many contracts
	private double tickSize; // 0.01
	private double tickValue; // $10

	// Margin
	private double initialMargin;
	private double maintenanceMargin;

	// Position
	private String positionType; // "LONG" or "SHORT"
	private double entryPrice; // Price entered at
	private String settlementType; // "PHYSICAL" or "CASH"

	// Market data service
	// private transient MarketDataService marketData;

	// === CONSTRUCTOR ===
	public FutureContract(String dealId, LocalDate tradeDate, String counterParty, String underlyingCommodity,
			double contractPrice, LocalDate expiryDate, String contractSymbol, String exchange, double contractSize,
			int numberOfContracts, String deliveryLocation, String positionType, double initialMargin,
			double maintenanceMargin) {

		// Call parent constructor
		// quantity = contractSize × numberOfContracts
		super(dealId, tradeDate, counterParty, contractSize * numberOfContracts, // total quantity
				"units", // unit (varies by commodity)
				contractPrice, // contract price
				underlyingCommodity, // underlying
				expiryDate, // expiry
				contractPrice); // strike price = contract price for futures

		this.contractSymbol = contractSymbol;
		this.exchange = exchange;
		this.contractSize = contractSize;
		this.numberOfContracts = numberOfContracts;
		this.deliveryLocation = deliveryLocation;
		this.positionType = positionType;
		this.entryPrice = contractPrice;
		this.initialMargin = initialMargin;
		this.maintenanceMargin = maintenanceMargin;

		// Set delivery month from expiry date
		this.deliveryMonth = expiryDate.getMonth() + "" + expiryDate.getYear();
	}

	// === IMPLEMENT ABSTRACT METHODS FROM DerivativeDeal ===

	@Override
	public double calculateIntrinsicValue() {

		return 0.00;
	}

	@Override
	public double calculateTimeValue() {

		return 0.00;
	}

	@Override
	public boolean isInTheMoney() {

		return true;
	}

	@Override
	public String getInstrumentType() {

		return "FUTURE";
	}

	// === FUTURES-SPECIFIC METHODS ===

	public double getPnL() {
		return calculateIntrinsicValue();
	}

//	    public double getContractValue() {
//	        return contractSize * numberOfContracts * getMarketPrice();
//	    }

//	    public double getLeverage() {
//	        return getNotionalValue() / getInitialMargin();
//	    }

	// === GETTERS ===

	public String getContractSymbol() {
		return contractSymbol;
	}

	public String getExchange() {
		return exchange;
	}

	public String getCommodityGrade() {
		return commodityGrade;
	}

	public String getDeliveryLocation() {
		return deliveryLocation;
	}

	public String getDeliveryMonth() {
		return deliveryMonth;
	}

	public LocalDate getLastTradingDay() {
		return lastTradingDay;
	}

	public double getContractSize() {
		return contractSize;
	}

	public int getNumberOfContracts() {
		return numberOfContracts;
	}

	public double getTickSize() {
		return tickSize;
	}

	public double getTickValue() {
		return tickValue;
	}

	public double getInitialMargin() {
		return initialMargin;
	}

	public double getMaintenanceMargin() {
		return maintenanceMargin;
	}

	public String getPositionType() {
		return positionType;
	}

	public double getEntryPrice() {
		return entryPrice;
	}

	public String getSettlementType() {
		return settlementType;
	}

}
