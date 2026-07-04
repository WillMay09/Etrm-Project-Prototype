package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


/**
 * Concrete trade in a crude oil option.
 * TradeInfo (business context) + CrudeOilOption (product)
 * + Quantity (signed lots) + TradedPrice (the premium agreed).
 *
 * Construction: BUILDER, every field is directly specified at trade time.
 */


public final class CrudeOilOptionTrade implements Trade {

	private final TradeInfo tradeInfo;
	private final TradedPrice tradedPrice;
	private final CrudeOilOption product;
	private final Quantity quantity;

	private CrudeOilOptionTrade(Builder builder) {

		this.tradeInfo = Objects.requireNonNull(builder.tradeInfo, "TradeInfo required");
		this.product = Objects.requireNonNull(builder.product, "Product required");
		this.quantity = Objects.requireNonNull(builder.quantity, "Quantity required");
		this.tradedPrice = Objects.requireNonNull(builder.tradedPrice, "TradedPrice required");

	}

	// ── Product access ────────────────────────────────────────────────
	
	
	@Override
	public TradeInfo getInfo() {
		// TODO Auto-generated method stub
		return tradeInfo;
	} 
	
	public CrudeOilOption getProduct() {
		return product;
	}

	public Quantity getQuantity() {
		return quantity;
	}

	public TradedPrice getTradedPrice() {
		return tradedPrice;
	}

	// ── Convenience delegations ───────────────────────────────────────

	/** Signed number of lots — positive = long, negative = short */
	public double getLots() {
		return quantity.getValue();
	}

	/** Strike price in USD/BBL */
	public double getStrike() {
		return product.getStrike();
	}

	/** Option expiry date */
	public LocalDate getExpiryDate() {
		return product.getExpiryDate();
	}
	
	/**time to expiry in years **/
	public double getTimetoExpiry() {

		long days = ChronoUnit.DAYS.between(LocalDate.now(), product.getExpiryDate());
		return Math.max(0.0, days/365.0);

	}
	
	
	  /**
     * Premium paid/received at inception.
     * Long position: negative cashflow (you pay)
     * Short position: positive cashflow (you receive)
     *
     * total premium = |lots| × contractSize × premiumPerUnit
     *               = 10    × 1000          × 3.50
     *               = $35,000
     */
	
	
	public double getTotalPremium() {
		
		double raw = tradedPrice.notionalValue( quantity.getValue(),product.getContractSize());
		return raw;
	}
	
	
	  /**
     * Intrinsic payoff at a given spot price.
     * Used by CashflowEngine at settlement.
     */
	
	public double payoffAt(double spotPrice) {
		
		double intrinsic = product.isCall() ? Math.max(spotPrice-product.getStrike(), 0.0) :
			Math.max(product.getStrike()-spotPrice, 0.0);
		
		
		return intrinsic * product.getContractSize() * quantity.getValue();
	}
	

	public static Builder builder() {

		return new Builder();
	}

	
	
	
	
    // ── Amendment support ─────────────────────────────────────────────

    /**
     * Returns a builder pre-populated with this trade's values.
     * Used by TradeStore.amend() to create corrected versions.
     *
     * Example: finance team corrects settlement date
     *   TradeInfo corrected = trade.getInfo().toBuilder()
     *       .settlementDate(correctedDate)
     *       .build();
     *   CrudeOilOptionTrade v2 = trade.toBuilder().info(corrected).build();
     *   tradeStore.amend(v2, "finance_team", "date correction");
     */
    public Builder toBuilder() {
        return new Builder()
            .info(tradeInfo)
            .product(product)
            .quantity(quantity)
            .tradedPrice(tradedPrice);
    }

	public static class Builder {

		private TradeInfo tradeInfo;
		private TradedPrice tradedPrice;
		private CrudeOilOption product;
		private Quantity quantity;
		private LocalDate expiryDate;

		public Builder() {

		}

		public Builder info(TradeInfo info) {
			this.tradeInfo = info;
			return this;
		}

		public Builder product(CrudeOilOption product) {
			this.product = product;
			return this;
		}

		public Builder quantity(Quantity quantity) {
			this.quantity = quantity;
			return this;
		}

		public Builder tradedPrice(TradedPrice price) {
			this.tradedPrice = price;
			return this;
		}
		
		public Builder longPosition(double lots) {
			
			
			this.quantity = Quantity.longLots(lots);
			return this;
		}
		
		public Builder shortPosition(double lots) {
			
			this.quantity = Quantity.shortLots(lots);
			return this;
		}
		
		public Builder premiumPerUnit(double premium) {
			
			if(tradeInfo == null || tradeInfo.getTradeDate().isEmpty()) {
				
				throw new IllegalArgumentException("Set TradeInfo with tradeDate before calling premiumPerUnit()");
			}
			
			this.tradedPrice = TradedPrice.of(tradeInfo.getTradeDate().get(), premium, "USD", "USD/BBL");
			return this;
			
			
		}
		
		public CrudeOilOptionTrade build() {
			
			return new CrudeOilOptionTrade(this);
		}

	}
	
	@Override
    public String toString() {
        return String.format(
            "CrudeOilOptionTrade[%s | %s | %s | premium=%.4f/unit | total=%,.0f USD]",
            tradeInfo.getStandardId().orElse("UNKNOWN"), product, quantity,
            tradedPrice.getPrice(), Math.abs(getTotalPremium())
        );
}
}
