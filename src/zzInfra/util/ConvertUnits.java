/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package zzInfra.util;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class ConvertUnits 
    {
    	// conversion from dBm to milliwatt
	public static double dBmtomW(final double dBm) {
		return Math.pow(10, (dBm - 30) / 10);
	}
        
	public static double mWtodBm(final double mW) 
         { return 10*Math.log10(mW/1000)+30; }
        
	public static double dBToFactor(final double dB) {
		return Math.pow(10, dB / 10);
	}        
}
