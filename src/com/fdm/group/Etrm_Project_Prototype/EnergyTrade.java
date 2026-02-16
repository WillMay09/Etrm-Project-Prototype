package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;

public abstract class EnergyTrade implements Tradeable {

	// === CORE TRADE DATA ===
	protected String dealId;
	protected LocalDate tradeDate;
	protected String counterParty;
	protected double quantity;
	protected String unit; // "MWh", "MMBtu", "barrels", "troy oz"
	protected double contractPrice; // Price agreed when trade was made

	// === CONSTRUCTOR ===
	public EnergyTrade(String dealId, LocalDate tradeDate, String counterParty, double quantity, String unit,
			double contractPrice) {

		// validation
		if (dealId == null || dealId.trim().isEmpty()) {

			throw new IllegalArgumentException("Deal Id required");
		}

		if (counterParty == null || counterParty.trim().isEmpty()) {

			throw new IllegalArgumentException("Counterparty is required");

		}
		if (quantity < 0) {

			throw new IllegalArgumentException("quantity must be greater than 0");
		}
		if (contractPrice <= 0) {

			throw new IllegalArgumentException("contractPrice cannot be negative");
		}

		this.dealId = dealId;
		this.tradeDate = tradeDate;
		this.counterParty = counterParty;
		this.quantity = quantity;
		this.unit = unit;
		this.contractPrice = contractPrice;
	}

	// === ABSTRACT METHODS (must be implemented by subclasses) ===

	/**
	 * Returns the commodity type: "POWER", "NATURAL_GAS", "CRUDE_OIL", "GOLD", etc.
	 */
	public abstract String getCommodityType();

	/**
	 * Returns the instrument type: "PHYSICAL", "FUTURE", "OPTION", "SWAP"
	 */
	public abstract String getInstrumentType();

	// === INTERFACE METHODS (from Tradeable)

	@Override
	public String getDealId() {
		return dealId;
	}

	@Override
	public LocalDate getTradeDate() {
		return tradeDate;
	}

	@Override
	public String getCounterParty() {
		return counterParty;
	}

	@Override
	public double getNotionalValue() {
		// Notional = quantity × contract price
		return quantity * contractPrice;
	}

	// === CONCRETE METHODS ===

	/**
	 * Check if trade is expired (older than 1 year)
	 */

	public boolean isExpired() {

		return tradeDate.isBefore(LocalDate.now().minusYears(1));

	}

	/**
	 * Validate the deal data
	 */
	public void validate() {
		if (dealId == null || dealId.trim().isEmpty()) {
			throw new IllegalStateException("Deal ID cannot be empty");
		}
		if (counterParty == null || counterParty.trim().isEmpty()) {
			throw new IllegalStateException("Counterparty cannot be null");
		}
		if (quantity <= 0) {
			throw new IllegalStateException("Quantity must be positive");
		}
		if (contractPrice <= 0) {
			throw new IllegalStateException("Contract price must be positive");
		}
	}

	protected void recordChange(String change) {

		System.out.println(dealId + ": " + change + "on" + LocalDate.now());
	}

	// === GETTERS ===

	public double getQuantity() {
		return quantity;
	}

	public String getUnit() {
		return unit;
	}

	public double getContractPrice() {
		return contractPrice;
	}

	public void setQuantity(double quantity) {

		if (quantity <= 0) {

			throw new IllegalArgumentException("quantity must be greater than 0");
		}
		this.quantity = quantity;
		recordChange("Quantity updated to : " + quantity);
	}

	public void setUnit(String unit) {

		if (unit == null || unit.trim().isEmpty()) {

			throw new IllegalArgumentException("unit cannot be empty");

		}
		this.unit = unit;
		recordChange("Units have been updated to" + unit);
	}
	
	// Note: No setter for dealId, tradeDate, counterParty, contractPrice
    // These should be immutable after creation (common in trading systems)

}
