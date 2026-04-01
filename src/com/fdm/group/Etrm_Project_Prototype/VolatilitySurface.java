package com.fdm.group.Etrm_Project_Prototype;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

	private final String commodity;
	// reference point = todays date
	private final LocalDate valuationDate;
	private final Map<VolatilityPoint, Double> calibratedPoints;
	//private final Map<String, Object> metadata;

	public VolatilitySurface(String commodity, LocalDate valuationDate) {

		if (commodity == null) {

			throw new NullPointerException("commodity cannot be null");

		} else if (valuationDate == null) {

			throw new NullPointerException("valuationDate must be a valid date");
		}
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
		// return
		// calibratedPoints.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.20);

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

			// Lower strike points (≤ target)
			if (point.getStrike() <= strike) {

				// Before or AT target expiry - want Largest strike, latest expiry
				if (!point.getExpiry().isAfter(expiry)) {
					if (lowerStrikeBefore == null || point.getStrike() > lowerStrikeBefore.getStrike()
							|| (point.getStrike() == lowerStrikeBefore.getStrike()
									&& point.getExpiry().isAfter(lowerStrikeBefore.getExpiry()))) {
						lowerStrikeBefore = point;
						//System.out.println("lowerStrikeBefore" + point);

					}
				}

				// After or AT target expiry
				if (!point.getExpiry().isBefore(expiry)) { 
					
					if (lowerStrikeAfter == null || point.getStrike() > lowerStrikeAfter.getStrike()
							|| (point.getStrike() == lowerStrikeAfter.getStrike()
									&& point.getExpiry().isAfter(lowerStrikeAfter.getExpiry()))) {
						lowerStrikeAfter = point;
						//System.out.println("lowerStrikeAfter" + point);

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
						//System.out.println("higherStrikeBefore" + point);

					}
				}

				// After or AT target expiry
				if (!point.getExpiry().isBefore(expiry)) { 
					if (higherStrikeAfter == null || point.getStrike() < higherStrikeAfter.getStrike()
							|| (point.getStrike() == higherStrikeAfter.getStrike()
									&& point.getExpiry().isAfter(higherStrikeAfter.getExpiry()))) {
						higherStrikeAfter = point;
						//System.out.println("higherStrikeAfter" + point);

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
		
		//Case 1: Exact strike match(k1 == k2)
		//All 4 corners at same strike -> only interpolate in time
		if(k1 == k2) {
			
			return vol11 + (vol12 -vol11) * (t-t1) / (double)(t2 -t1);
		}
		//Case 2: Exact expiry match(t1 == t2)
		// All 4 corners at same expiry → only interpolate in strike
		if(t1 == t2) {
			
			return vol11 + (vol21 - vol11) * (strike - k1)/(k2 - k1);
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
	 * Find nearest calibrated volatility when interpolation is not possible
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

	public Set<VolatilityPoint> getCalibrationPoints() {
		return new HashSet<>(calibratedPoints.keySet());
	}

	public int size() {
		return calibratedPoints.size();
	}

	@Override
	public String toString() {
		return String.format("VolatilitySurface[%s, %d points]", commodity, calibratedPoints.size());
	}

}
