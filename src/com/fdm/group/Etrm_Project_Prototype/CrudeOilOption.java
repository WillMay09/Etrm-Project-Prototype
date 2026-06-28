package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.Objects;


/**
 * Pure instrument definition - what the pricer needs, nothing else.
 * No TradeInfo, no quantity, no traded price. Just the mathematical shape
 * of the option: strike, expiry, direction, contract size.
 *
 * Construction: BUILDER pattern - every field here is something a trader
 * directly specifies when structuring the deal.
 */
public final class CrudeOilOption {

	public enum PutCall {
		PUT, CALL
	}

	// ── Instrument definition — all the pricer needs ───────────────

	private final String underlying;
	private final double strike;

	private final LocalDate expiryDate;
	private final PutCall putCall;
	private final double contractSize;

	private final String currency;

	private CrudeOilOption(Builder builder) {

		this.underlying = Objects.requireNonNull(builder.underlying, "underlying required");
		this.strike = builder.strike;
		this.expiryDate = Objects.requireNonNull(builder.expiryDate, "expiryDate required");
		this.putCall = Objects.requireNonNull(builder.putCall, "putCall required");
		this.contractSize = builder.contractSize;
		this.currency = builder.currency;

		if (strike <= 0) {

			throw new IllegalArgumentException("Strike must be a positive number");
		}

		if (contractSize <= 0) {

			throw new IllegalArgumentException("Contact size cannot be negative");
		}
	}

	public String getUnderlying() {
		return underlying;
	}

	public double getContractSize() {
		return contractSize;
	}

	public double getStrike() {
		return strike;
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public PutCall getPutCall() {
		return putCall;
	}

	public String getCurrency() {
		return currency;
	}

	public boolean isCall() {
		return putCall == PutCall.CALL;
	}

	public boolean isPut() {
		return putCall == PutCall.PUT;
	}

	public static Builder builder() {

		return new Builder();

	}

	public static class Builder {

		private String underlying = "CRUDE_OIL";
		private double strike;

		private LocalDate expiryDate;
		private PutCall putCall;
		private double contractSize = 1_000.0;

		private String currency = "USD";

		public Builder() {

		}

		public Builder underlying(String underlying) {

			this.underlying = underlying;

			return this;

		}

		public Builder strike(double strike) {
			this.strike = strike;
			return this;
		}

		public Builder expiryDate(LocalDate expiryDate) {
			this.expiryDate = expiryDate;
			return this;
		}

		public Builder call() {
			this.putCall = PutCall.CALL;
			return this;
		}

		public Builder put() {
			this.putCall = PutCall.PUT;
			return this;
		}

		public Builder contractSize(double contractSize) {
			this.contractSize = contractSize;
			return this;
		}

		public Builder currency(String currency) {
			this.currency = currency;
			return this;
		}

		public CrudeOilOption build() {

			return new CrudeOilOption(this);
		}

	}
	
	 @Override
	    public String toString() {
	        return String.format("CrudeOilOption[%s %s strike=%.2f expiry=%s size=%.0f]",
	            underlying, putCall, strike, expiryDate, contractSize);
	    }

}
