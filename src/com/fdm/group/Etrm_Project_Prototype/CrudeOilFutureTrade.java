package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class CrudeOilFutureTrade implements Trade {

	/**
	 * A concrete trade in a crude oil vanilla option.
	 *
	 * Structure mirrors OpenGamma's pattern exactly: FraTrade = TradeInfo + Fra
	 * (product) + quantity + price This = TradeInfo + CrudeOilOption + Quantity +
	 * TradedPrice
	 *
	 * This class is the minimum needed to: 1. Identify the trade (TradeInfo) 2.
	 * Know what was traded (CrudeOilOption) 3. Know how much (Quantity) 4. Know
	 * what was paid (TradedPrice — the premium) 5. Feed the pricer (all product
	 * fields available) 6. Feed the Greeks engine (same) 7. Generate cashflows
	 * (premium on T+2, payoff at expiry)
	 */

	private final TradeInfo info;
	private final CrudeOilFuture product;
	private final Quantity quantity;
	private final TradedPrice tradedPrice;

	private CrudeOilFutureTrade(Builder builder) {

		this.info = Objects.requireNonNull(builder.info, "Trade info required");
		this.product = Objects.requireNonNull(builder.product, "Product required");
		this.quantity = Objects.requireNonNull(builder.quantity, "Quantity required");
		this.tradedPrice = Objects.requireNonNull(builder.tradedPrice, "Price required");

	}
	
	
	  // ── Trade interface ───────────────────────────────────────────────
	@Override
	public TradeInfo getInfo() {
		return info;
	}
	
	// ── Product access ────────────────────────────────────────────────

	public CrudeOilFuture getProduct() {
		return product;
	}

	public Quantity getQuantity() {
		return quantity;
	}

	public TradedPrice getTradedPrice() {
		return tradedPrice;
	}
	
	
	// ── Convenience delegations ───────────────────────────────────────

	
	public double getLots() { return quantity.getValue();}
	
	
	/**Entry price */
	public double getEntryPrice() { return tradedPrice.getPrice();}
	
	
	/** Option expiry date */
	
	public LocalDate getDeliveryDate() { return product.getDeliveryDate();}
	
	
	public double getTimeToDelivery() {
		
		long days = ChronoUnit.DAYS.between(LocalDate.now(), product.getDeliveryDate());
		
		return Math.max(0.0, days);
		
	}
	
	
	 /**
     * Mark-to-market P&L at a given current price.
     * (currentPrice - entryPrice) × contractSize × lots
     *
     * e.g. Entry $78.50, current $80.00, long 5 lots
     *   = ($80.00 - $78.50) × 1000 × 5 = +$7,500
     */
	
	public double markToMarketPnl(double currentPrice) {
		
		return (currentPrice - tradedPrice.getPrice())
				*product.getContractSize()
				*quantity.getValue();
	}
	
	/**
     * Daily variation margin.
     * (todayClose - previousClose) × contractSize × lots
     */
	
	public double variationMargin(double previousClose, double todayClose) {
		
		return  (todayClose - previousClose) * 
				product.getContractSize() * quantity.getValue();
	}
	
	

	public static Builder builder() {

		return new Builder();

	}

	public Builder toBuilder() {

		return builder().info(info).product(product).quantity(quantity).tradedPrice(tradedPrice);

	}

	public static class Builder {

		private TradeInfo info;
		private CrudeOilFuture product;
		private Quantity quantity;
		private TradedPrice tradedPrice;

		public Builder() {

		}

		public Builder info(TradeInfo info) {
			this.info = info;
			return this;
		}

		public Builder product(CrudeOilFuture product) {
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

		public CrudeOilFutureTrade Build() {

			return new CrudeOilFutureTrade(this);
		}

	}
	
//	 @Override
//	    public String toString() {
//	        return String.format(
//	            "CrudeOilFutureTrade[%s | %s | %s | entry=%.2f/BBL]",
//	            info.getId().orElse("UNKNOWN"), product, quantity, tradedPrice.getPrice()
//	        );
//	    }

//}
}
