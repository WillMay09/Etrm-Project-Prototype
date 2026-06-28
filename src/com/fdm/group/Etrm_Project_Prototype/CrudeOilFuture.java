package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.Objects;

public final class CrudeOilFuture {

	public enum SettlementType {
		CASH, PHYSICAL
	}

	private final String underlying;

	private final LocalDate deliveryDate;

	private final double contractSize;

	private final String currency;

	private final SettlementType settlementType;

	
	private CrudeOilFuture(Builder builder) {

		this.underlying = Objects.requireNonNull(builder.underlying, "underlying required");
		this.deliveryDate = Objects.requireNonNull(builder.deliveryDate, "deliveryDate required");
		this.contractSize = builder.contractSize;
		this.currency = builder.currency;
		this.settlementType = builder.settlementType;

		if (contractSize <= 0) {

			throw new IllegalArgumentException("Contract size cannot be negative");
		}
	}

	public String getUnderlying() {
		return underlying;
	}

	public double getContractSize() {
		return contractSize;
	}

	public LocalDate getDeliveryDate() {
		return deliveryDate;
	}

	public String getCurrency() {
		return currency;
	}

	public SettlementType getSettlementType() {
		return settlementType;
	}

	public boolean isCashSettled() {
		return settlementType == SettlementType.CASH;
	}

	public static Builder builder() {

		return new Builder();

	}

	public static class Builder {

		private String underlying = "CRUDE_OIL";

		private LocalDate deliveryDate;

		private double contractSize = 1_000.0;

		private String currency = "USD";

		private SettlementType settlementType = SettlementType.CASH;

		public Builder() {

		}

		public Builder underlying(String underlying) {
			this.underlying = underlying;
			return this;
		}

		public Builder deliveryDate(LocalDate deliveryDate) {
			this.deliveryDate = deliveryDate;
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

		public Builder cashSettled() {

			this.settlementType = SettlementType.CASH;
			return this;
		}

		public Builder physicalDelivery() {

			this.settlementType = SettlementType.PHYSICAL;
			return this;
		}

		public CrudeOilFuture Build() {

			return new CrudeOilFuture(this);
		}
		
		
		 @Override
		    public String toString() {
		        return String.format("CrudeOilFuture[%s delivery=%s size=%.0f %s]",
		            underlying, deliveryDate, contractSize, settlementType);
		    }

	}

}
