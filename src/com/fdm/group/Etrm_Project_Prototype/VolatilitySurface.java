package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a simplified implied volatility surface for option pricing.
 * Volatility varies by strike price and time to expiration.
 */

public class VolatilitySurface {

	private final String commodity;
	private final LocalDate valuationDate;
	private final Map<VolatilityPoint, Double> calibratedPoints;

	public VolatilitySurface(String commodity, LocalDate valuationDate) {

		this.commodity = commodity;
		this.valuationDate = valuationDate;
		calibratedPoints = new HashMap<>();

	}

	/**
	 * Add volatility data point
	 */

	public void addVolatility(double strike, LocalDate expiry, double volatility) {

		if (volatility < 0) {

			throw new IllegalArgumentException("Volatility cannot be negative");

		}
		VolatilityPoint volPoint = new VolatilityPoint(strike, expiry);

		// add to volatility to map
		calibratedPoints.put(volPoint, volatility);

	}

	public void addAllPoints(Map<VolatilityPoint, Double> points) {

		calibratedPoints.putAll(points);

	}

	// === RETRIEVE VOLATILITY ===

	/**
	 * Get volatility for exact strike and expiry
	 */

	public double getVolatility(double strike, LocalDate expiry) {

		VolatilityPoint volPoint = new VolatilityPoint(strike, expiry);

		// Exact match
		if (calibratedPoints.containsKey(volPoint)) {

			return calibratedPoints.get(volPoint);
		}

		///20% default
		///interpolation needs to be implemented
		return interpolate(strike, expiry);
		//return calibratedPoints.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.20);

	}

	/**
	 * Get volatility using time to expiry in years
	 */

	public double getVolatility(double strike, double timeToExpiry) {

		LocalDate expiry = valuationDate.plusDays((long) (timeToExpiry * 365));

		return getVolatility(strike, expiry);
	}

	/**
	 * Key for volatility surface lookup
	 */

	private static class VolatilityPoint {

		final double strike;
		final LocalDate expiry;

		VolatilityPoint(double strike, LocalDate expiry) {

			this.strike = strike;
			this.expiry = expiry;
		}

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

	}

	// === INTERPOLATION ===

	/**
	 * Bilinear interpolation in strike and time dimensions
	 */

	private double interpolate(double strike, LocalDate expiry) {

		VolatilityPoint lowerStrikeBefore = null;
		VolatilityPoint lowerStrikeAfter = null;
		VolatilityPoint higherStrikeBefore = null;
		VolatilityPoint higherStrikeAfter = null;

		for (VolatilityPoint point : calibratedPoints.keySet()) {
			// find the strikes below target
			if (point.getStrike() <= strike) {
				if (point.getExpiry().isBefore(expiry) || point.getExpiry().equals(expiry)) {

					if (lowerStrikeBefore == null || point.getStrike() > lowerStrikeAfter.getStrike()) {

						lowerStrikeAfter = point;
					}

				}

				if (point.getExpiry().isAfter(expiry) || point.getExpiry().equals(expiry)) {

					if (lowerStrikeAfter == null || point.getStrike() > lowerStrikeAfter.getStrike()) {

						lowerStrikeAfter = point;
					}
				}
				
				

			}else {
				
				if(point.getExpiry().isBefore(expiry) || point.getExpiry().equals(expiry)){
					
					
					
				}
				
				
			}

		}
		return strike;

	}

}
