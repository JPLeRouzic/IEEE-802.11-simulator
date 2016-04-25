package zzInfra.util;

/**
 * A helper class with vector operations in both the Cartesian and the
 * geographical coordinate system
 * 
 * @author friggr
 * 
 */
public class Vector3d {
	public static final double earthRadius = 6367444.25;

	private double x, y, z;

	public Vector3d() {
	}

	public Vector3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3d clone() {
		return new Vector3d(x, y, z);
	}

	public Vector3d add(Vector3d otherVector) {
		Vector3d result = this.clone();
		result.x += otherVector.getX();
		result.y += otherVector.getY();
		result.z += otherVector.getZ();
		return result;
	}

	public Vector3d sub(Vector3d otherVector) {
		Vector3d result = this.clone();
		result.x -= otherVector.getX();
		result.y -= otherVector.getY();
		result.z -= otherVector.getZ();
		return result;
	}

	public double dot(Vector3d otherVector) {
		return x * otherVector.getX() + y * otherVector.getY() + z * otherVector.getZ();
	}

	public Vector3d cross(Vector3d otherVector) {
		return new Vector3d(y * otherVector.getZ() - z * otherVector.getY(), z * otherVector.getX() - x * otherVector.getZ(), x
				* otherVector.getY() - y * otherVector.getX());
	}

	public Vector3d scale(double factor) {
		return new Vector3d(x * factor, y * factor, z * factor);
	}

	public Vector3d reflect() {
		return new Vector3d(-x, -y, -z);
	}

	public double getDistanceTo(Vector3d otherVector) {
		return otherVector.sub(this).getLength();
	}

	public double getAngleTo(Vector3d otherVector) {
		return Math.acos(this.normalize().dot(otherVector.normalize()));
	}

	public Vector3d normalize() {
		if (this.getLength() == 0) {
			return this;
		}
		return this.scale(1 / this.getLength());
	}

	public Vector3d rotate(double angleDegrees, int axis) {
		Vector3d res = this.clone();
		double cosAngle = Math.cos(Math.toRadians(angleDegrees));
		double sinAngle = Math.sin(Math.toRadians(angleDegrees));
		switch (axis) {
		case 0:
			res.setY(y * cosAngle + z * -sinAngle);
			res.setZ(y * sinAngle + z * cosAngle);
			return res;
		case 1:
			res.setX(x * cosAngle + z * sinAngle);
			res.setZ(x * -sinAngle + z * cosAngle);
			return res;
		case 2:
			res.setX(x * cosAngle + y * -sinAngle);
			res.setY(x * sinAngle + y * cosAngle);
			return res;
		default:
			return res;
		}
	}

	public double getLength() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}

	public double getLat() {
		double length = getLength();
		if (length != 0) {
			return Math.toDegrees(Math.asin(z / getLength()));
		} else {
			return 0;
		}
	}

	public void setLat(double lat) {
		double currentLat = getLat();
		double sinFactor = Math.sin(Math.toRadians(lat)) / Math.sin(Math.toRadians(currentLat));
		double cosFactor = Math.cos(Math.toRadians(lat)) / Math.cos(Math.toRadians(currentLat));
		x = x * sinFactor;
		y = y * sinFactor;
		z = z * cosFactor;
	}

	public double getLon() {
		return Math.toDegrees(Math.atan2(y, x));
	}

	public void setLon(double lon) {
		double currentLon = getLon();
		x = x * (Math.cos(Math.toRadians(lon)) / Math.cos(Math.toRadians(currentLon)));
		y = y * (Math.sin(Math.toRadians(lon)) / Math.sin(Math.toRadians(currentLon)));
	}

	public double getAlt() {
		return getLength() - earthRadius;
	}

	public void setAlt(double alt) {
		/*
		 * double oldLength = getLength(); double newLength = alt + earthRadius;
		 * x = x/oldLength*newLength; y = y/oldLength*newLength; z =
		 * z/oldLength*newLength;
		 */
		double oldLength = getLength();
		double newLength = alt + earthRadius;
		if (oldLength != 0) {
			x = x / oldLength * newLength;
			y = y / oldLength * newLength;
			z = z / oldLength * newLength;
		} else {
			z = alt + earthRadius;
		}
	}

	@Override
	public String toString() {
		return "X: " + x + " Y: " + y + " Z:" + z;
	}

	public String toStringGeo() {
		return "Lat: " + getLat() + "Lon: " + getLon() + "Alt: " + getAlt();
	}
}
