package com.fdm.group.Etrm_Project_Prototype;



/**
 * A signed quantity of an instrument.
 *
 * Positive = long (you own it, you benefit if price rises)
 * Negative = short (you owe it, you benefit if price falls)
 *
 * Keeping quantity separate from the raw double prevents the
 * common bug of forgetting the sign convention in aggregation.
 *
 * Unit: number of lots (each lot = contractSize physical units)
 *   10 lots of crude oil = 10 × 1000 = 10,000 barrels
 */

public final class Quantity {
	
	
	private final double value;
	private final String unit;
	
	
	
	private Quantity(double value, String unit) {
		
		if(value ==0 ) {
			
			throw new IllegalArgumentException("Quantity cannot be 0");
			
			
		}
		if(unit == null) {
			
			throw new IllegalArgumentException("Unit cannot be null");
		}
		
		this.value = value;
		this.unit = unit;
		
	}
	
	public static Quantity ofLots(double lots) {
		
		
		return new Quantity(lots, "LOTS");
	}
	
	
	public static Quantity longLots(double lots) {
		
		if (lots <=0) {
			
			throw new IllegalArgumentException("use longLots() for short positions");
			
			
		}
		return new Quantity(lots,"LOTS");
	}
	
	public static Quantity shortLots(double lots) {
		
		if (lots <=0) {
			
			throw new IllegalArgumentException("Provide a positive number, sign applied internally");
			
			
		}
		return new Quantity(-lots,"LOTS");
	}
	
	
	  public double getValue()        { return value; }
	    public String getUnit()         { return unit; }
	    public boolean isLong()         { return value > 0; }
	    public boolean isShort()        { return value < 0; }
	    public double  absoluteValue()  { return Math.abs(value); }

	    
	    @Override
	    public String toString() {
	        return String.format("%s%.1f %s", value > 0 ? "Long " : "Short ", Math.abs(value), unit);
	    }

}
