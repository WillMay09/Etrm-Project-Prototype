package com.fdm.group.Etrm_Project_Prototype;

public class NormalDistribution {
	
	private static final double SQRT_2PI = Math.sqrt(2.0 * Math.PI);
	
	
	/**
     * Standard normal probability density function
     * 
     * @param x Value to evaluate
     * @return Probability density at x
     */
	
	
	public static double pdf(double x) {
		
		
		return (1.0/SQRT_2PI) * Math.exp(-0.5 * x *x);
	}
	
	
	
	/**
     * General normal PDF
     * 
     * @param x Value to evaluate
     * @param mean Mean of distribution
     * @param stdDev Standard deviation
     * @return Probability density at x
     */
	
	
	public static double pdf(double x, double mean, double stdDev) {
		
		double z = (x - mean)/ stdDev;
		return pdf(z)/ stdDev;
		
	}
	
	
	
	/**
     * Example: Visualize the bell curve
     */
    public static void main(String[] args) {
        System.out.println("Standard Normal PDF:");
        System.out.println("x\t\tf(x)");
        
        for (double x = -3.0; x <= 3.0; x += 0.5) {
            double density = pdf(x);
            System.out.printf("%.1f\t\t%.4f%n", x, density);
        }
        
        /* Output:
        x       f(x)
        -3.0    0.0044
        -2.5    0.0175
        -2.0    0.0540
        -1.5    0.1295
        -1.0    0.2420
        -0.5    0.3521
        0.0     0.3989  ← Peak
        0.5     0.3521
        1.0     0.2420
        1.5     0.1295
        2.0     0.0540
        2.5     0.0175
        3.0     0.0044
        */
    }

}
