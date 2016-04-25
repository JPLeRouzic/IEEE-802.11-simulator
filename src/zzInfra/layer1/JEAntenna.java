package zzInfra.layer1;

import zzInfra.util.Vector3d;

/**
 * Models an antenna that can either be omnidirectional or directional depending
 * on the choice of the apertureAngle
 * 
 * @author friggr
 * 
 */
public class JEAntenna {

	/**
	 * the direction of the antenna as a 3d vector
	 */
	private Vector3d direction;

	/**
	 * the gain of the antenna into its direction [dBi]
	 */
	private double gain_dBi;

	/**
	 * the gain of the antenna into all the other directions [dBi]
	 */
	private double gainLow_dBi;

	/**
	 * the opening angle of the aperture in degrees (between 0 and 360)
	 */
	private double apertureAngle;

	/**
	 * Creates an JEAntenna given a direction (x,y,z), an apertureAngle and a
	 * gain into this direction with this aperture.
	 * 
	 * @param x
	 *            the x-direction of the antenna
	 * @param y
	 *            the y-direction of the antenna
	 * @param z
	 *            the z-direction of the antenna
	 * @param gain
	 *            the gain into the focus direction of the antenna in dBi
	 *            (positive)
	 * @param angle
	 *            the aperture angle for the direction with high gain
	 *            (horizontally and vertically)
	 */
	public JEAntenna(double x, double y, double z, double gain_dBi, double angle) {

		this.gain_dBi = gain_dBi;

		double highFactor = getGainAsFactor();
		double angleRad = Math.toRadians(angle);
		double aSinA = angleRad * Math.sin(angleRad);
		double lowFactor = (Math.PI - highFactor * aSinA) / (Math.PI - aSinA);

		this.apertureAngle = angle;
		if (lowFactor > 0) {
			gainLow_dBi = 10 * Math.log10(lowFactor);
		} else {
			gainLow_dBi = -100;
		}

		/*
		 * //compute angle if (gain_dBi <= 0 || gainLow_dBi >= 0) { //if gain is
		 * not positive or gainLow is not negative use omnidirectional antenna
		 * apertureAngle = 180; this.gain_dBi = 0; this.gainLow_dBi = 0; } else
		 * { this.gain_dBi = gain_dBi; this.gainLow_dBi = gainLow_dBi; double
		 * highFactor = getGainAsFactor(); double lowFactor =
		 * getGainLowAsFactor(); apertureAngle =
		 * 180*Math.sqrt(1-lowFactor)/Math.sqrt(highFactor - lowFactor); }
		 */

		this.direction = new Vector3d(x, y, z).normalize();
	}

	/**
	 * @return the gain in dB
	 */
	public double getGain_dBi() {
		return gain_dBi;
	}

	/**
	 * the gain as a scalar factor
	 * 
	 * @return
	 */
	public double getGainAsFactor() {
		return Math.pow(10, gain_dBi / 10);
	}

	/**
	 * the gainLow as a scalar factor
	 * 
	 * @return
	 */
	public double getGainLowAsFactor() {
		return Math.pow(10, gainLow_dBi / 10);
	}

	/**
	 * Computes the gain for a given direction lat and lon are needed to
	 * transform the antenna direction into the local coordinate system.
	 * 
	 * @param pathDirection
	 *            the direction of the path for which the gain is computed
	 * @param lat
	 *            the current latitude of the station that owns this antenna
	 * @param lon
	 *            the current longitude of the station that owns this antenna
	 * @return the gain in dB into the given direction
	 */
	public double getGainIndBForDirection(Vector3d pathDirection, double lat, double lon, double heading) {
		double angle = Math.toDegrees(getDirectionForLatLon(lat, lon, heading).getAngleTo(pathDirection));
		if (angle <= apertureAngle) {
			return gain_dBi;
		} else {
			return gainLow_dBi;
		}
	}

	/**
	 * @return the direction (in the ECEF coordinate system, not relative to the
	 *         station which owns the antenna)
	 */
	public Vector3d getDirection() {
		return direction;
	}

	/**
	 * @param lat
	 *            the latitude of the position to which the direction of the
	 *            antenna should be related to
	 * @param lon
	 *            the longitude of the position to which the direction of the
	 *            antenna should be related to
	 * @return the direction of the antenna relative to the local coordinate
	 *         system of the given position (lat,lon)
	 */
	public Vector3d getDirectionForLatLon(double lat, double lon, double heading) {
		return direction.rotate(90 + heading, 2).rotate(-(lat - 90), 1).rotate(lon, 2);
	}

	/**
	 * Sets the direction to (X,Y,Z).
	 * 
	 * @param x
	 *            the new x direction
	 * @param y
	 *            the new y direction
	 * @param z
	 *            the new z direction
	 */
	public void setDirection(double x, double y, double z) {
		direction.setX(x);
		direction.setY(y);
		direction.setZ(z);
	}

	/**
	 * @return the aperture angle of the antenna in degrees
	 */
	public double getApertureAngle() {
		return apertureAngle;
	}

	/**
	 * Sets a new apertureAngle for the antenna
	 * 
	 * @param angle
	 *            the new angle in degrees
	 */
	public void setApertureAngle(double angle) {
		apertureAngle = angle;
	}

	/**
	 * @return wether the antenna is directional
	 */
	public boolean isDirectional() {
		return (apertureAngle < 180);
	}
}
