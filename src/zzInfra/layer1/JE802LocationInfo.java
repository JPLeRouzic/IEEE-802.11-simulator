package zzInfra.layer1;

import zzInfra.kernel.JETime;
import zzInfra.kernel.JEmula;

public class JE802LocationInfo extends JEmula implements Comparable<JE802LocationInfo> {

	private double xLocation;

	private double yLocation;

	private double zLocation;

	private JETime time;

	private final double accuracy;

	public JE802LocationInfo(final double x, final double y, final double z, final JETime time, final double accuracy) {
		this.xLocation = x;
		this.yLocation = y;
		this.zLocation = z;
		this.time = time;
		this.accuracy = accuracy;
	}

	public double getXLocation() {
		return this.xLocation;
	}

	public double getYLocation() {
		return this.yLocation;
	}

	public double getZLocation() {
		return this.zLocation;
	}

	public double getAccuracy() {
		return this.accuracy;
	}

	public void setTime(final JETime time) {
		this.time = time;
	}

	public JETime getTime() {
		return this.time;
	}

	@Override
	public int compareTo(JE802LocationInfo o) {
		if (o.getTime().isEarlierThan(time)) {
			return 1;
		} else if (o.getTime().isLaterThan(time)) {
			return -1;
		} else {
			return 0;
		}
	}

	public void setxLocation(double xLocation) {
		this.xLocation = xLocation;
	}

	public void setyLocation(double yLocation) {
		this.yLocation = yLocation;
	}

	public void setzLocation(double zLocation) {
		this.zLocation = zLocation;
	}

	@Override
	public String toString() {
		return "x: " + xLocation + " y: " + yLocation + " z: " + zLocation + " Time: " + time;
	}
}
