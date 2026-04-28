package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a simplified implied volatility surface for option pricing.
 * Volatility varies by strike price and time to expiration. Represents a
 * snapshot in time
 */

public class VolatilitySurface {

	/** Underlying commodity */
	private final String commodity;

	/**
	 * reference point = todays date - all time calculations are relative to this
	 * date
	 */
	private final LocalDate valuationDate;

	/** Calibrated volatility points: (strike, expiry) -> implied volatility */
	private final Map<VolatilityPoint, Double> calibratedPoints;
	/**
	 * Optional metadata (e.g., data source, calibration time, interpolation type)
	 */
	private final Map<String, Object> metaData;

	// =========================================================================
	// Constructors
	// =========================================================================
	
	
	/**
	 * Constructor for Builder pattern (private)
	 * 
	 * @param builder Builder with all configuration
	 */
	private VolatilitySurface(Builder builder) {

		this.commodity = builder.commodity;
		this.valuationDate = builder.valuationDate;
		// immutable copies, uses collections wrappers
		this.calibratedPoints = Collections.unmodifiableMap(new HashMap<>(builder.calibratedPoints));
		this.metaData = Collections.unmodifiableMap(new HashMap<>(builder.metaData));
	}
	/**
	 * Legacy constructor (deprecated - use Builder instead)
	 * 
	 * @param commodity     Underlying commodity
	 * @param valuationDate Valuation date
	 * @deprecated Use {@link #builder()} instead for immutability and validation
	 */
	@Deprecated
	public VolatilitySurface(String commodity, LocalDate valuationDate) {

		if (commodity == null) {

			throw new NullPointerException("commodity cannot be null");

		} else if (valuationDate == null) {

			throw new NullPointerException("valuationDate must be a valid date");
		}
		this.commodity = commodity;
		this.valuationDate = valuationDate;
		calibratedPoints = new HashMap<>();
		metaData = new HashMap<>();

	}

	// =========================================================================
	// Static Factory Method
	// =========================================================================

	/**
	 * Creates a new Builder for constructing an immutable VolatilitySurface
	 * 
	 * @return New Builder instance
	 */

	public static Builder builder() {

		return new Builder();
	}

	// =========================================================================
	// Builder Pattern
	// =========================================================================

	/**
	 * Builder for creating immutable VolatilitySurface instances
	 * 
	 * Provides fluent API for configuration and validates at build time.
	 */

	public static class Builder {

		private String commodity;
		private LocalDate valuationDate;
		private Map<VolatilityPoint, Double> calibratedPoints = new HashMap<>();
		private Map<String, Object> metaData = new HashMap<>();

		/**
		 * Private constructor - use VolatilitySurface.builder()
		 */

		private Builder() {
		}

		/**
		 * Set the commodity
		 * 
		 * @param commodity Underlying commodity (e.g., "CRUDE_OIL")
		 * @return this Builder for chaining
		 */
		public Builder commodity(String commodity) {

			this.commodity = commodity;
			return this;
		}

		/**
		 * Set the valuation date
		 * 
		 * @param valuationDate Date for which volatilities are valid
		 * @return this Builder for chaining
		 */

		public Builder valuationDate(LocalDate valuationDate) {

			this.valuationDate = valuationDate;
			return this;
		}

		/**
		 * Add a calibrated volatility point
		 * 
		 * @param strike     Strike price
		 * @param expiry     Expiry date
		 * @param volatility Implied volatility (annualized)
		 * @return this Builder for chaining
		 * @throws IllegalArgumentException if parameters are invalid
		 */

		public Builder addVolatility(double strike, LocalDate expiry, double volatility) {

			if (strike <= 0) {
				throw new IllegalArgumentException("Strike must be positive, got: " + strike);
			}
			if (expiry == null) {
				throw new IllegalArgumentException("Expiry cannot be null");
			}
			if (volatility < 0 || volatility > 10.0) {
				throw new IllegalArgumentException(
						"Volatility must be between 0 and 1000%, got: " + (volatility * 100) + "%");

			}
			calibratedPoints.put(new VolatilityPoint(strike, expiry), volatility);

			return this;
		}

		/**
		 * Add metadata
		 * 
		 * @param key   Metadata key (e.g., "source", "calibrationTime")
		 * @param value Metadata value
		 * @return this Builder for chaining
		 */
		public Builder metadata(String key, Object value) {

			this.metaData.put(key, value);

			return this;
		}

		/**
		 * Build the immutable VolatilitySurface
		 * 
		 * @return New VolatilitySurface instance
		 * @throws IllegalStateException if configuration is invalid
		 */

		public VolatilitySurface build() {

			// validation

			if (commodity == null || commodity.trim().isEmpty()) {

				throw new IllegalStateException("Commodity is required");
			}
			if (valuationDate == null) {

				throw new IllegalStateException("Valuation date is required");

			}
			if (calibratedPoints.isEmpty()) {

				throw new IllegalStateException("Must add at least one volatility point");
			}
			if (calibratedPoints.size() < 4) {

				throw new IllegalStateException(
						"Need at least 4 points for bilinear interpolation, got:  " + calibratedPoints.size());

			}
			// passing in new builder object
			return new VolatilitySurface(this);

		}
	}

	// =========================================================================
	// Legacy Methods(Deprecated)
	// =========================================================================

	/**
	 * Add a volatility point (legacy mutable API - deprecated)
	 * 
	 * @param strike     Strike price
	 * @param expiry     Expiry date
	 * @param volatility Implied volatility
	 * @deprecated Use Builder pattern instead: {@link #builder()}
	 * @throws UnsupportedOperationException if surface was created with Builder
	 *                                       (immutable)
	 */

	@Deprecated
	public void addVolatility1(double strike, LocalDate expiry, double volatility) {

		if (volatility < 0) {

			throw new IllegalArgumentException("Volatility cannot be negative");

		} else if (volatility >= 10.0) {

			throw new IllegalArgumentException("Volatility cannot be greater than 10.0(1000%)");

		}
		VolatilityPoint volPoint = new VolatilityPoint(strike, expiry);

		// add to volatility to map
		calibratedPoints.put(volPoint, volatility);

	}

	public void addAllPoints(Map<VolatilityPoint, Double> points) {

		calibratedPoints.putAll(points);

	}

	  // =========================================================================
    // Main Volatility Retrieval Method
    // =========================================================================
    
    /**
     * Get implied volatility at any (strike, expiry) point
     * 
     * Uses bilinear interpolation if point is not calibrated.
     * Falls back to nearest neighbor if interpolation is impossible.
     * 
     * @param strike Strike price
     * @param expiry Expiry date
     * @return Implied volatility (annualized)
     */

	public double getVolatility(double strike, LocalDate expiry) {

		VolatilityPoint volPoint = new VolatilityPoint(strike, expiry);

		// Exact match
		if (calibratedPoints.containsKey(volPoint)) {

			return calibratedPoints.get(volPoint);
		}

		
		return interpolate(strike, expiry);
		

	}

	// =========================================================================
	// Scenario Creation
	// =========================================================================

	/**
	 * Create a new Builder initialized with this surface's data
	 * 
	 * Useful for creating scenario variations
	 * 
	 * @return Builder with this surface's configuration
	 */

	public Builder toBuilder() {

		Builder builder = new Builder();

		builder.commodity = this.commodity;
		builder.valuationDate = this.valuationDate;
		builder.calibratedPoints = new HashMap<>(this.calibratedPoints);
		builder.metaData = new HashMap<>(this.metaData);
		return builder;

	}

	/**
	 * Create a surface with updated volatility at a specific point
	 * 
	 * @param strike        Strike price
	 * @param expiry        Expiry date
	 * @param newVolatility New implied volatility
	 * @return New surface with updated point
	 */

	public VolatilitySurface withVolatility(double strike, LocalDate expiry, double newVolatility) {

		return toBuilder().addVolatility(strike, expiry, newVolatility)
				.metadata("modification", "Updated volatility at (" + strike + ", " + expiry + ")")
				.build();

	}

	/**
	 * Get volatility using time to expiry in years
	 */

	public double getVolatility(double strike, double timeToExpiry) {

		LocalDate expiry = valuationDate.plusDays((long) (timeToExpiry * 365));

		return getVolatility(strike, expiry);
	}
	
	
	
	// =========================================================================
    // Object Methods
    // =========================================================================
	@Override 
	public String toString() {
		
		
		return String.format("VolatilitySurface[commodity=%s, valuationDate=%s, points=%d]",
                commodity, valuationDate, calibratedPoints.size());
	}
	
	 @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;
	        VolatilitySurface that = (VolatilitySurface) o;
	        return Objects.equals(commodity, that.commodity) &&
	               Objects.equals(valuationDate, that.valuationDate) &&
	               Objects.equals(calibratedPoints, that.calibratedPoints);
	    }
	    
	    @Override
	    public int hashCode() {
	        return Objects.hash(commodity, valuationDate, calibratedPoints);
	    }

	
	
	/**
	 * Key for volatility surface lookup
	 */
	
	
	public static class VolatilityPoint {

		final double strike;
		final LocalDate expiry;

		VolatilityPoint(double strike, LocalDate expiry) {

			if (strike <= 0) {

				throw new IllegalArgumentException("Strike cannot be 0 or negative");

			} else if (expiry == null) {

				throw new NullPointerException("expiry must be a valid object");
			}
			this.strike = strike;
			this.expiry = expiry;
		}
		
		
		 // =========================================================================
	    // Object Methods
	    // =========================================================================
	    
		@Override
		public boolean equals(Object o) {

			if (this == o)
				return true;

			if (!(o instanceof VolatilityPoint))
				return false;
			VolatilityPoint that = (VolatilityPoint) o;
			return Double.compare(that.strike, strike) == 0 && expiry.equals(that.expiry);

		}

		@Override
		public int hashCode() {

			return Objects.hash(strike, expiry);
		}

		public LocalDate getExpiry() {

			return expiry;
		}

		public double getStrike() {

			return strike;

		}

		@Override
		public String toString() {

			return String.format("VolatilityPoint[strike=%.2f, expiry=%s]", strike, expiry);

		}

	}

	/**
     * Bilinear interpolation in strike and time dimensions
     * 
     * Finds 4 corner points forming a rectangle around the target,
     * then interpolates first in time, then in strike.
     * 
     * @param strike Target strike
     * @param expiry Target expiry
     * @return Interpolated volatility
     */

	private double interpolate(double strike, LocalDate expiry) {

		VolatilityPoint lowerStrikeBefore = null;
		VolatilityPoint lowerStrikeAfter = null;
		VolatilityPoint higherStrikeBefore = null;
		VolatilityPoint higherStrikeAfter = null;

		for (VolatilityPoint point : calibratedPoints.keySet()) {

			// Lower strike points (≤ target)
			if (point.getStrike() <= strike) {

				// Before or AT target expiry - want Largest strike, latest expiry
				if (!point.getExpiry().isAfter(expiry)) {
					if (lowerStrikeBefore == null || point.getStrike() > lowerStrikeBefore.getStrike()
							|| (point.getStrike() == lowerStrikeBefore.getStrike()
									&& point.getExpiry().isAfter(lowerStrikeBefore.getExpiry()))) {
						lowerStrikeBefore = point;
						// System.out.println("lowerStrikeBefore" + point);

					}
				}

				// After or AT target expiry
				if (!point.getExpiry().isBefore(expiry)) {

					if (lowerStrikeAfter == null || point.getStrike() > lowerStrikeAfter.getStrike()
							|| (point.getStrike() == lowerStrikeAfter.getStrike()
									&& point.getExpiry().isAfter(lowerStrikeAfter.getExpiry()))) {
						lowerStrikeAfter = point;
						// System.out.println("lowerStrikeAfter" + point);

					}
				}
			}

			// Higher strike points (≥ target)
			if (point.getStrike() >= strike) {

				// Before or AT target expiry - want smallest strike, latest expiry
				if (!point.getExpiry().isAfter(expiry))
					if (higherStrikeBefore == null || point.getStrike() < higherStrikeBefore.getStrike()
							|| (point.getStrike() == higherStrikeBefore.getStrike()
									&& point.getExpiry().isAfter(higherStrikeBefore.getExpiry()))) {
						higherStrikeBefore = point;
						// System.out.println("higherStrikeBefore" + point);

					}
			}

			// After or AT target expiry
			if (!point.getExpiry().isBefore(expiry)) {
				if (higherStrikeAfter == null || point.getStrike() < higherStrikeAfter.getStrike()
						|| (point.getStrike() == higherStrikeAfter.getStrike()
								&& point.getExpiry().isAfter(higherStrikeAfter.getExpiry()))) {
					higherStrikeAfter = point;
					// System.out.println("higherStrikeAfter" + point);

				}
			}
		}

		// Interpolation not possible cases
		if (lowerStrikeBefore == null || lowerStrikeAfter == null || higherStrikeBefore == null
				|| higherStrikeAfter == null) {

			// need to implement findNearestVolatility
			return findNearestVolatility(strike, expiry);
		}

		// grabbing implied volatility at these points in the hashmap
		double vol11 = calibratedPoints.get(lowerStrikeBefore);
		double vol12 = calibratedPoints.get(lowerStrikeAfter);
		double vol21 = calibratedPoints.get(higherStrikeBefore);
		double vol22 = calibratedPoints.get(higherStrikeAfter);

		// get the strike for these points
		double k1 = lowerStrikeBefore.getStrike();
		double k2 = higherStrikeBefore.getStrike();
		// time for these points
		double t1 = ChronoUnit.DAYS.between(valuationDate, lowerStrikeBefore.getExpiry());
		double t2 = ChronoUnit.DAYS.between(valuationDate, lowerStrikeAfter.getExpiry());
		double t = ChronoUnit.DAYS.between(valuationDate, expiry);

		// ========================================
		// CRITICAL: Handle Edge Case
		// ========================================

		// Case 1: Exact strike match(k1 == k2)
		// All 4 corners at same strike -> only interpolate in time
		if (k1 == k2) {

			return vol11 + (vol12 - vol11) * (t - t1) / (double) (t2 - t1);
		}
		// Case 2: Exact expiry match(t1 == t2)
		// All 4 corners at same expiry → only interpolate in strike
		if (t1 == t2) {

			return vol11 + (vol21 - vol11) * (strike - k1) / (k2 - k1);
		}

		// ========================================
		// Normal bilinear interpolation
		// ========================================

		// Perform interpolation in both dimensions

		// interpolate in time

		// vol for lower strike
		double volLower = vol11 + (vol12 - vol11) * (t - t1) / (t2 - t1);

		// vol for higher strike
		double volHigher = vol21 + (vol22 - vol21) * (t - t1) / (t2 - t1);

		// interpolate in Strike dimension
		double finalVol = volLower + (volHigher - volLower) * (strike - k1) / (k2 - k1);

		return finalVol;

	}

	 /**
     * Find nearest calibrated volatility (fallback when interpolation impossible)
     * 
     * Uses Euclidean distance in normalized (strike %, time) space
     * 
     * @param strike Target strike
     * @param expiry Target expiry
     * @return Volatility from nearest point
     */

	private double findNearestVolatility(double strike, LocalDate expiry) {

		// placeholder values
		double minDistance = Double.MAX_VALUE;
		VolatilityPoint nearest = null;

		long targetDays = ChronoUnit.DAYS.between(valuationDate, expiry);

		for (VolatilityPoint point : calibratedPoints.keySet()) {

			long days = ChronoUnit.DAYS.between(valuationDate, point.getExpiry());

			// Calculate Euclidean distance(weighted)

			double strikeDiff = Math.abs(point.getStrike() - strike) / strike;// relative
			double timeDiff = Math.abs(days - targetDays) / 365.0; // in years

			double distance = Math.sqrt(strikeDiff * strikeDiff + timeDiff * timeDiff);

			if (distance < minDistance) {

				minDistance = distance;
				nearest = point;
			}

		}

		if (nearest == null) {

			return 0.25;
		}

		return calibratedPoints.get(nearest);
	}
	
	
	

	// === UTILITY METHODS ===

	public String getCommodity() {
		return commodity;
	}

	public LocalDate getValuationDate() {
		return valuationDate;
	}

	public Map<VolatilityPoint, Double> getCalibratedPoints() {
		return Collections.unmodifiableMap(calibratedPoints);
	}

	public int size() {
		return calibratedPoints.size();
	}

	
	
	/**
     * Get metadata value
     * 
     * @param <T> Expected type
     * @param key Metadata key
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
	
	public Map<String, Object> getAllMetadata(){
		
		
		return Collections.unmodifiableMap(metaData);
	}
	

}
