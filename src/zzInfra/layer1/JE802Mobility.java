/*
 * 
 * This is Jemula.
 *
 *    Copyright (c) 2009 Stefan Mangold, Fabian Dreier, Stefan Schmid
 *    All rights reserved. Urheberrechtlich geschuetzt.
 *    
 *    Redistribution and use in source and binary forms, with or without modification,
 *    are permitted provided that the following conditions are met:
 *    
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer. 
 *    
 *      Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution. 
 *    
 *      Neither the name of any affiliation of Stefan Mangold nor the names of its contributors
 *      may be used to endorse or promote products derived from this software without
 *      specific prior written permission. 
 *    
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 *    EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *    IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 *    INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *    OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *    WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 *    OF SUCH DAMAGE.
 *    
 */

package zzInfra.layer1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPathConstants;

import zzInfra.kernel.JETime;
import zzInfra.kernel.JEmula;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import zzInfra.util.Vector3d;

public class JE802Mobility extends JEmula {

	private final double earthRadius = 6367444.25;

	public double xLocation;

	public double yLocation;

	public double zLocation;

	private boolean mobile;

	private JETime interpolationInterval_ms;

	private double minAccuracy;

	// sorted by time
	private List<JE802LocationInfo> locations;

	private JETime offsetTime_ms;
        
        private Double longitude;
        private Double latitude;

	public JE802Mobility(Node mobilityNode) {
		Element mobilityElem = (Element) mobilityNode;
		if (mobilityElem.getNodeName().equals("JE802Mobility")) {
			String xLocationStr = mobilityElem.getAttribute("xLocation");
			String yLocationStr = mobilityElem.getAttribute("yLocation");
			String zLocationStr = mobilityElem.getAttribute("zLocation");
			String isMobile = mobilityElem.getAttribute("isMobile");
			if (xLocationStr.isEmpty() || yLocationStr.isEmpty() || zLocationStr.isEmpty() || isMobile.isEmpty()) {
				this.message("WARNING: Mobility does not contain all attributes. Usage <JE802Mobility xLocation=\"0\" yLocation=\"0\" zLocation=\"0\" isMobile=\"false\">");
			}
			this.xLocation = new Double(xLocationStr);
			this.yLocation = new Double(yLocationStr);
			this.zLocation = new Double(zLocationStr);

                        this.longitude = new Double(mobilityElem.getAttribute("baseLongitude"));
			this.latitude = new Double(mobilityElem.getAttribute("baseLatitude"));
        
			this.mobile = new Boolean(isMobile);

			String interpolationIntervalStr = mobilityElem.getAttribute("interpolationInterval_ms");
			if (!interpolationIntervalStr.isEmpty()) {
				this.interpolationInterval_ms = new JETime(new Double(interpolationIntervalStr));
			} else {
				this.interpolationInterval_ms = new JETime(3000);
			}

			if (this.mobile == true) {
				String traceFile = mobilityElem.getAttribute("traceFile");
				if (!traceFile.isEmpty()) {
					String minAccuracyStr = mobilityElem.getAttribute("minAccuracy");
					if (!minAccuracyStr.isEmpty()) {
						this.minAccuracy = new Double(minAccuracyStr);
					} else {
						this.minAccuracy = 150;
					} // TODO:uncommetn this to restore to previous state
					// String interpolationIntervalStr =
					// mobilityElem.getAttribute("interpolationInterval_ms");
					// if(!interpolationIntervalStr.isEmpty()){
					// this.interpolationInterval_ms = new JETime(new
					// Double(interpolationIntervalStr));
					// } else{
					// this.interpolationInterval_ms = new JETime(3000);
					// }

					String traceStartOffset = mobilityElem.getAttribute("offsetTime_ms");
					if (!traceStartOffset.isEmpty()) {
						this.offsetTime_ms = new JETime(new Double(traceStartOffset));
					} else {
						this.offsetTime_ms = new JETime(0);
					}
					JE802TraceParser parser = new JE802MobilitySimTraceParser(traceFile, this.offsetTime_ms);
					// sorted by time

					locations = parser.parseFile();
					locations = computeInterpolations(locations);

				} else {
					// locations = new ArrayList<JE802LocationInfo>();
					this.interpolationInterval_ms = new JETime(1000);
				}
			}
		} else {
			this.error("Expected JE802Mobility node, found " + mobilityElem.getNodeName() + " instead");
		}

		this.message("Interpolation interval:" + this.interpolationInterval_ms);
	}

	public JETime getTraceStartTime() {
		if (mobile && !locations.isEmpty()) {
			if (locations.get(0).getTime().getTimeMs() < 0) {
				return new JETime(0);
			} else {
				return locations.get(0).getTime();
			}

		} else {
			return new JETime();
		}
	}

	public JETime getTraceEndTime() {
		if (mobile && !locations.isEmpty()) {
			return locations.get(locations.size() - 1).getTime();
		} else {
			return new JETime(Double.MAX_VALUE);
		}
	}

	public double getXLocation(JETime atTime) {

		if (!mobile || locations.isEmpty()) {
			return xLocation;
		}
		int index = (int) (atTime.getTimeMs() / interpolationInterval_ms.getTimeMs());
		if (index < 0) {
			return locations.get(0).getXLocation();
		} else if (index >= locations.size()) {
			return locations.get(locations.size() - 1).getXLocation();
		} else {
			return locations.get(index).getXLocation();
		}
	}

	public double getYLocation(JETime atTime) {

		if (!mobile || locations.isEmpty()) {
			return yLocation;
		}
		int index = (int) (atTime.getTimeMs() / interpolationInterval_ms.getTimeMs());
		if (index < 0) {
			return locations.get(0).getYLocation();
		} else if (index >= locations.size()) {
			return locations.get(locations.size() - 1).getYLocation();
		} else {
			return locations.get(index).getYLocation();
		}
	}

	public double getZLocation(JETime atTime) {
		if (!mobile || locations.isEmpty()) {
			return zLocation;
		}
		int index = (int) (atTime.getTimeMs() / interpolationInterval_ms.getTimeMs());
		if (index < 0) {
			return locations.get(0).getZLocation();
		} else if (index >= locations.size()) {
			return locations.get(locations.size() - 1).getZLocation();
		} else {
			return locations.get(index).getZLocation();
		}
	}

	public double getTraceHeading(JETime atTime) {
		if (!mobile) {
			return 0.0;
		}
		Vector3d currentPosition = new Vector3d(getXLocation(atTime), getYLocation(atTime), getZLocation(atTime));
		Vector3d nextPosition = new Vector3d(getXLocation(atTime.plus(interpolationInterval_ms)),
				getYLocation(atTime.plus(interpolationInterval_ms)), getZLocation(atTime.plus(interpolationInterval_ms)));
		double headingRotation = nextPosition.sub(currentPosition).normalize().getLon();
		if (headingRotation < 0) {
			headingRotation = 360 + headingRotation;
		}
		return headingRotation;
	}

	public double getXLocation() {
		return xLocation;
	}

	public double getYLocation() {
		return yLocation;
	}

	public double getZLocation() {
		return zLocation;
	}

	public boolean isMobile() {
		return mobile;
	}

	public List<JE802LocationInfo> getLocations() {
		return this.locations;
	}

	public void setXLocation(double xLocation) {
		this.xLocation = xLocation;
	}

	public void setYLocation(double yLocation) {
		this.yLocation = yLocation;
	}

	public void setZLocation(double zLocation) {
		this.zLocation = zLocation;
	}

	public JETime getInterpolationInterval_ms() {
		return interpolationInterval_ms;
	}

	@Override
	public String toString() {
		return "X: " + xLocation + " Y: " + yLocation + " Z:" + zLocation;
	}

	private double meters2DegreesLongitude(final double meters, final double lat) {
		return 360.0 / (2 * this.earthRadius * Math.PI * Math.cos(Math.toRadians(lat))) * meters;
	}

	/**
	 * 
	 * @param meters
	 *            distance in meters from origin
	 * @return degrees from origin
	 */
	private double meters2DegreesLatitude(final double meters) {
		return 360.0 / (2 * this.earthRadius * Math.PI) * meters;
	}

	protected List<JE802LocationInfo> computeInterpolations(List<JE802LocationInfo> infos) {
		convertToCartesian(infos);
		List<JE802LocationInfo> interpolations = new ArrayList<JE802LocationInfo>();
		if (infos.size() > 1) {
			JE802LocationInfo firstInfo = infos.get(0);
			JE802LocationInfo lastInfo = infos.get(infos.size() - 1);
			double startTime = firstInfo.getTime().getTimeMs();
			double deltaTime = (lastInfo.getTime().getTimeMs() - startTime);
			double interPolationInterval = interpolationInterval_ms.getTimeMs();
			int interpolationPoints = (int) (deltaTime / interPolationInterval);

			for (int i = 0; i <= interpolationPoints; i++) {
				JE802LocationInfo infoAtPoint = computePointAtTime(startTime + i * interPolationInterval, infos);
				interpolations.add(infoAtPoint);
			}
		}
		return interpolations;
	}

	protected JE802LocationInfo computePointAtTime(double time, List<JE802LocationInfo> infos) {

		double currentTime = infos.get(0).getTime().getTimeMs();
		// if time is before trace time, return location of first point of trace
		if (time <= currentTime) {
			return infos.get(0);
		}
		// seek for the the element that has smaller time than time, for this
		// create dummy element as search key.
		JE802LocationInfo toSearch = new JE802LocationInfo(0, 0, 0, new JETime(time), 0);
		int index = Collections.binarySearch(infos, toSearch);
		if (index < 0) {
			index = Math.abs(index) - 2;
		}
		JE802LocationInfo locT1 = infos.get(index);
		// if time is later than the time of the last point in the trace, return
		// last position of the trace
		if (index + 1 >= infos.size()) {
			return infos.get(infos.size() - 1);
		} else {
			JE802LocationInfo locT2 = infos.get(index + 1);
			JE802LocationInfo pointAtTime = interpolate(locT1, time, locT2);
			return pointAtTime;
		}
	}

	protected JE802LocationInfo interpolate(JE802LocationInfo locT1, double time, JE802LocationInfo locT2) {

		double t1 = locT1.getTime().getTimeMs();
		double t2 = locT2.getTime().getTimeMs();
		if (t1 < time && time < t2) {
			double deltaLocTime = t2 - t1;
			double deltaTime = time - t1;
			double factor = deltaTime / deltaLocTime;
			double x1 = locT1.getXLocation();
			double y1 = locT1.getYLocation();
			double z1 = locT1.getZLocation();
			double x2 = locT2.getXLocation();
			double y2 = locT2.getYLocation();
			double z2 = locT2.getZLocation();
			double dx = x2 - x1;
			double dy = y2 - y1;
			double dz = z2 - z1;
			double xAtTime = x1 + factor * dx;
			double yAtTime = y1 + factor * dy;
			double zAtTime = z1 + factor * dz;
			return new JE802LocationInfo(xAtTime, yAtTime, zAtTime, new JETime(time),
					(locT1.getAccuracy() + locT2.getAccuracy()) / 2);
		} else if (time <= t1) {
			return locT1;
		} else {
			return locT2;
		}
	}

	protected void convertToCartesian(List<JE802LocationInfo> toConvert) {
		for (JE802LocationInfo info : toConvert) {
			polarToCartesian(info);
		}
	}

	protected void polarToCartesian(JE802LocationInfo info) {
		double radius = earthRadius + info.getZLocation();
		double theta = Math.toRadians(90 - info.getXLocation());
		double phi = Math.toRadians(info.getYLocation());
		double x = radius * Math.sin(theta) * Math.cos(phi);
		double y = radius * Math.sin(theta) * Math.sin(phi);
		double z = radius * Math.cos(theta);
		info.setxLocation(x);
		info.setyLocation(y);
		info.setzLocation(z);
	}
}