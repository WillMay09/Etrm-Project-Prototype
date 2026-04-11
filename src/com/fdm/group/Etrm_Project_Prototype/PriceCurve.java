package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Represents forward prices for different delivery dates Used for pricing
 * futures contracts * Stores forward prices at specific delivery dates and
 * provides linear interpolation for prices at arbitrary dates.
 * 
 * Thread-safe: Instances are immutable after construction via Builder.
 */

public class PriceCurve {

	/** Commodity identifier (e.g., "CRUDE_OIL", "NATURAL_GAS") */
	private final String commodity;

	/** Valuation date - curve is valid as of this date */
	private final LocalDate valuationDate;

	/** Forward prices: delivery date → price */
	private final TreeMap<LocalDate, Double> prices;

	/** Optional metadata (e.g., data source, curve type) */
	private final TreeMap<String, Object> metaData;

	/**
	 * Private constructor for Builder pattern
	 * 
	 * @param builder Builder with all configuration
	 */

	private PriceCurve(Builder builder) {

		this.commodity = builder.commodity;
		this.valuationDate = builder.valuationDate;
		this.prices = new TreeMap<>(builder.prices);
		this.metaData = new TreeMap<>(builder.metaData);

	}

	// =========================================================================
	// Static Factory Method
	// =========================================================================

	/**
	 * Creates a new Builder for constructing an immutable PriceCurve
	 * 
	 * @return New Builder instance
	 */

	public static Builder builder() {

		return new Builder();

	}

	// =========================================================================
	// Builder Class
	// =========================================================================

	/**
	 * Builder for creating immutable PriceCurve instances
	 */

	public static class Builder {

		private String commodity;

		private LocalDate valuationDate;

		private TreeMap<LocalDate, Double> prices = new TreeMap<>();

		private TreeMap<String, Object> metaData = new TreeMap<>();

		/**
		 * Private constructor - use PriceCurve.builder()
		 */
		private Builder() {

		}

		/**
		 * Set the commodity
		 * 
		 * @param commodity Commodity identifier (e.g., "CRUDE_OIL")
		 * @return this Builder for chaining
		 */

		public Builder commodity(String commodity) {

			this.commodity = commodity;

			return this;

		}

		/**
		 * Set the valuation date
		 * 
		 * @param valuationDate Date for which curve is valid
		 * @return this Builder for chaining
		 */

		public Builder valuationDate(LocalDate valuationDate) {

			this.valuationDate = valuationDate;
			return this;
		}

		/**
		 * Add a forward price point
		 * 
		 * @param deliveryDate Delivery date
		 * @param price        Forward price
		 * @return this Builder for chaining
		 * @throws IllegalArgumentException if price is negative or date is before
		 *                                  valuation
		 */

		public Builder addPrice(LocalDate deliveryDate, Double price) {

			if (deliveryDate == null) {

				throw new IllegalArgumentException("Delivery date cannot be null");

			}
			if (price < 0) {

				throw new IllegalArgumentException("Price cannot be less than 0, got: " + price);
			}

			if (valuationDate != null && deliveryDate.isBefore(valuationDate)) {

				throw new IllegalArgumentException(
						"Delivery date " + deliveryDate + " is before valuation date " + valuationDate);

			}

			prices.put(deliveryDate, price);
			return this;

		}

		/**
		 * Add metadata
		 * 
		 * @param key   Metadata key
		 * @param value Metadata value
		 * @return this Builder for chaining
		 */

		public Builder metadata(String key, Object value) {

			metaData.put(key, value);

			return this;

		}

		/**
		 * Apply parallel shift to all prices
		 * 
		 * Useful for creating stress scenarios
		 * 
		 * @param shift Amount to add to each price
		 * @return this Builder for chaining
		 */

		public Builder paralleShift(double shift) {

			TreeMap<LocalDate, Double> shifted = new TreeMap<>();
			for (var entry : prices.entrySet()) {

				shifted.put(entry.getKey(), entry.getValue() + shift);
			}
			this.prices = shifted;

			return this;
		}

		/**
		 * Apply percentage bump to all prices
		 * 
		 * @param percentage Percentage to bump (e.g., 0.10 for +10%)
		 * @return this Builder for chaining
		 */

		public Builder percentageBump(double percentage) {

			TreeMap<LocalDate, Double> bumped = new TreeMap<>();

			for (var entry : prices.entrySet()) {

				bumped.put(entry.getKey(), entry.getValue() * (1 + percentage));

			}

			this.prices = bumped;
			return this;
		}

		public PriceCurve build() {

			if (commodity == null || commodity.trim().isEmpty()) {

				throw new IllegalStateException("Commodity is required");
			}
			if (valuationDate == null) {

				throw new IllegalStateException("Valuation date is required");

			}
			if (prices.isEmpty()) {
				throw new IllegalStateException("Must add at least one price point");
			}
			if (prices.size() < 2) {
				throw new IllegalStateException(
						"Need at least 2 price points for interpolation, got: " + prices.size());

			}
			return new PriceCurve(this);

		}

	}

	// =========================================================================
	// Scenario Creation Methods
	// =========================================================================

	/**
	 * Create a new Builder initialized with this curve's data
	 * 
	 * Useful for creating scenario variations
	 * 
	 * @return Builder with this curve's configuration
	 */

	public Builder toBuilder() {

		Builder builder = new Builder();

		builder.commodity = this.commodity;
		builder.valuationDate = this.valuationDate;

		builder.prices = new TreeMap<>(this.prices);

		builder.metaData = new TreeMap<>(this.metaData);

		return builder;
	}

	/**
	 * Create a curve with parallel shift applied
	 * 
	 * Example: curve.withParallelShift(5.0) adds $5 to every price
	 * 
	 * @param shift Amount to shift all prices (can be negative)
	 * @return New curve with shifted prices
	 */

	public PriceCurve withParallelShift(double shift) {

		return toBuilder().paralleShift(shift).metadata("scenario", "Parallel shift " + (shift >= 0 ? "+" : "") + shift)
				.build();
	}

	/**
	 * Create a curve with percentage bump applied
	 * 
	 * Example: curve.withBump(0.10) increases all prices by 10%
	 * 
	 * @param percentage Percentage to bump (e.g., 0.10 for +10%)
	 * @return New curve with bumped prices
	 */

	public PriceCurve withBump(double percentage) {

		return toBuilder().percentageBump(percentage)
				.metadata("scenario", "Bumped " + (percentage >= 0 ? "+" : "") + (percentage * 100) + "%").build();

	}

	/**
	 * Create a curve with updated price at specific date
	 * 
	 * @param deliveryDate Delivery date
	 * @param newPrice     New price
	 * @return New curve with updated price
	 */

	public PriceCurve withPrice(LocalDate deliveryDate, double price) {

		return toBuilder().addPrice(deliveryDate, price).metadata("modfication", "Update Price at" + deliveryDate)
				.build();

	}

	// =========================================================================
	// Main Price Retrieval Method
	// =========================================================================

	/**
	 * Get forward price for a specific delivery date
	 * 
	 * Uses linear interpolation between calibrated points. Uses nearest point for
	 * dates outside calibrated range.
	 * 
	 * Algorithm: 1. Check for exact match 2. Find dates before and after target 3.
	 * If target before all dates: use earliest 4. If target after all dates: use
	 * latest 5. Otherwise: linear interpolation
	 * 
	 * @param deliveryDate Delivery date
	 * @return Forward price
	 * @throws NullPointerException if deliveryDate is null
	 */

	public double getPrice(LocalDate targetDate) {

		Objects.requireNonNull(targetDate, "Delivery date cannot be null");

		// Exact Match

		if (prices.containsKey(targetDate)) {

			return prices.get(targetDate);

		}

		// Find surrounding dates
		LocalDate before = prices.floorKey(targetDate);
		LocalDate after = prices.ceilingKey(targetDate);
		//System.out.println(before);
		//System.out.println(after);

		// Find surrounding dates
		if (before == null) {

			return prices.firstEntry().getValue();
		}
		if (after == null) {

			return prices.lastEntry().getValue();
		}

		// Interpolation case
		return interpolateLinear(targetDate, before, after);

	}

	/**
	 * Linear interpolation between two dates
	 * 
	 * Formula: price = priceBefore + (priceAfter - priceBefore) * weight where
	 * weight = (days from before to target) / (days from before to after)
	 * 
	 * Example: Before: June 19 → $92 After: Sept 18 → $90 Target: July 15
	 * 
	 * Days from June 19 to July 15: 26 days Days from June 19 to Sept 18: 91 days
	 * Weight: 26/91 = 0.286 Price: 92 + (90-92) * 0.286 = 92 - 0.57 = $91.43
	 * 
	 * @param target Target date
	 * @param before Date before target
	 * @param after  Date after target
	 * @return Interpolated price
	 */

	// =========================================================================
	// Interpolation Logic
	// =========================================================================

	private double interpolateLinear(LocalDate target, LocalDate before, LocalDate after) {

		double priceBefore = prices.get(before);

		double priceAfter = prices.get(after);

		long daysBefore = ChronoUnit.DAYS.between(before, target);
		long daysTotal = ChronoUnit.DAYS.between(before, after);
		
		//System.out.println("daysBefore: " + daysBefore);
		//System.out.println("daysTotal: " + daysTotal);
		
		

		// Handle degenerate case(should not happen with TreeMap)

		if (daysTotal == 0) {

			return priceBefore;

		}
		
		double weight = (double) daysBefore / daysTotal;
		
		//System.out.println("Weight: " + weight);
		return priceBefore + weight * (priceAfter - priceBefore);

	}

	// =========================================================================
	// Getters
	// =========================================================================
	public String getCommodity() {

		return commodity;
	}

	public LocalDate getValuationDate() {

		return valuationDate;
	}

	public int size() {

		return prices.size();
	}

	/**
	 * Get metadata value
	 * 
	 * @param <T>  Expected type
	 * @param key  Metadata key
	 * @param type Expected class
	 * @return Metadata value or null if not found
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMetadata(String key, Class<T> type) {
		Object value = metaData.get(key);
		return value != null ? (T) value : null;
	}

	/**
	 * Get all metadata (defensive copy)
	 * 
	 * @return Unmodifiable map of metadata
	 */

	public Map<String, Object> getAllMetaData() {

		return Collections.unmodifiableMap(new TreeMap<>(metaData));
	}

	/**
	 * Get all calibrated prices (defensive copy)
	 * 
	 * @return Unmodifiable map of prices
	 */

	public Map<LocalDate, Double> getAllPrices() {

		return Collections.unmodifiableMap(new TreeMap<>(prices));
	}

	@Override
	public String toString() {
		return String.format("PriceCurve[commodity=%s, valuationDate=%s, points=%d]", commodity, valuationDate,
				prices.size());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PriceCurve that = (PriceCurve) o;
		return Objects.equals(commodity, that.commodity) && Objects.equals(valuationDate, that.valuationDate)
				&& Objects.equals(prices, that.prices);
	}

	@Override
	public int hashCode() {
		return Objects.hash(commodity, valuationDate, prices);
	}

}
