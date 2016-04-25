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

package zzInfra.animation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zzInfra.kernel.JETime;
import zzInfra.layer1.JE802Mobility;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.ARC.JE802Station;
import zzInfra.util.Vector3d;

public abstract class JE802LinkKmlGenerator extends JE802KmlGenerator {

	protected List<JE802LinkRecord> links = new ArrayList<JE802LinkRecord>();

	protected final double linkDistance_m = 0.1;

	public JE802LinkKmlGenerator(Document doc, List<JE802Station> stations) {
		super(doc, stations);
	}

	/**
	 * 
	 * @param numOtherLinks
	 * @param index
	 * @param gradient
	 *            specifies color of Link
	 * @param currentTime
	 *            time in simulation
	 * @param station1
	 *            origin
	 * @param station2
	 *            destination
	 * @return KML element of the Link with appropiate coloring an timeSpan
	 */
	protected Element createLink(Color color, JETime startTime, JETime endTime, int index, int numLinks, JE802Station station1,
			JE802Station station2) {
		Element style = createLineStyle(color);
		JE802Mobility mob1 = station1.getMobility();
		JE802Mobility mob2 = station2.getMobility();
		Vector3d src = new Vector3d(mob1.getXLocation(startTime), mob1.getYLocation(startTime), mob1.getZLocation(startTime));
		Vector3d dst = new Vector3d(mob2.getXLocation(startTime), mob2.getYLocation(startTime), mob2.getZLocation(startTime));

		if (station1.isMobile()) {
			src.setAlt(userModelHeight);
		} else {
			if (src.getAlt() <= 0.5) {
				src.setAlt(antennaModelHeight);
			}
		}

		if (station2.isMobile()) {
			dst.setAlt(userModelHeight);
		} else {
			if (dst.getAlt() <= 0.5) {
				dst.setAlt(antennaModelHeight);
			}
		}

		Element line = createLine(src, dst);
		Element timeSpan = createTimeSpan(startTime, endTime);
		Element placemark = doc.createElement("Placemark");
		placemark.appendChild(line);
		placemark.appendChild(timeSpan);
		placemark.appendChild(style);
		return placemark;
	}

	/**
	 * creates a line between station 1 and station2
	 * 
	 * @param station1
	 * @param station2
	 * @param numOtherLinks
	 * @param index
	 * @return KML element which represents a line (Placemark) between station 1
	 *         and station2
	 */
	protected Element createLine(Vector3d src, Vector3d dst) {

		// End points of line
		Element coordinates = doc.createElement("coordinates");

		/*
		 * double[] sta1Pos = {station1.getXLocation(time),
		 * station1.getYLocation(time), station1.getZLocation(time)}; double[]
		 * sta2Pos = {station2.getXLocation(time), station2.getYLocation(time),
		 * station2.getZLocation(time)};
		 * 
		 * if(numLinks>1){ double[] sta1Vert = {station1.getXLocation(time),
		 * station1.getYLocation(time), station1.getZLocation(time)+1}; double[]
		 * v1 = vectorDifference(sta1Pos, sta2Pos); double[] u1 =
		 * vectorDifference(sta1Pos, sta1Vert); double[] e1 =
		 * crossProductNormed(v1, u1);
		 * 
		 * double normalLength = linkDistance_m*numLinks;
		 * 
		 * double[] start1 = vectorDifference(sta1Pos,scaleVector(e1,
		 * normalLength/2)); sta1Pos =
		 * vectorSum(start1,scaleVector(e1,index*linkDistance_m));
		 * 
		 * double[] start2 = vectorDifference(sta2Pos, scaleVector(e1,
		 * normalLength/2)); sta2Pos =
		 * vectorSum(start2,scaleVector(e1,index*linkDistance_m)); }
		 */

		double[] srcVec = { src.getLat(), src.getLon(), src.getAlt() };
		double[] dstVec = { dst.getLat(), dst.getLon(), dst.getAlt() };

		StringBuilder coordinateStr = new StringBuilder();

		// fix for jumping line in animation, stations should not have same
		// latitude
		if (srcVec[0] == dstVec[0]) {
			srcVec[0] += 10E-14;
		}

		// fix for jumping line in animation, stations should not have same
		// longitude
		if (srcVec[1] == dstVec[1]) {
			srcVec[1] += 10E-14;
		}

		coordinateStr.append(srcVec[1] + "," + srcVec[0] + "," + srcVec[2] + " ");
		coordinateStr.append(dstVec[1] + "," + dstVec[0] + "," + dstVec[2] + " ");

		coordinates.appendChild(doc.createTextNode(coordinateStr.toString()));
		Element altitudeMode = doc.createElement("altitudeMode");
		altitudeMode.appendChild(doc.createTextNode("relativeToGround"));
		Element line = doc.createElement("LineString");
		line.appendChild(coordinates);
		line.appendChild(altitudeMode);
		return line;
	}

	/*
	 * private double[] vectorDifference(double[] u, double[] v) { double[]
	 * result = new double[3]; result[0] = u[0]-v[0]; result[1] = u[1]-v[1];
	 * result[2] = u[2]-v[2]; return result; }
	 * 
	 * private double[] vectorSum(double[] v, double[] u) { double[] result =
	 * new double[3]; result[0] = u[0]+v[0]; result[1] = u[1]+v[1]; result[2] =
	 * u[2]+v[2]; return result; }
	 * 
	 * private double[] scaleVector(double[] vektor, double factor){ double[]
	 * result = new double[3]; result[0] = vektor[0]*factor; result[1] =
	 * vektor[1]*factor; result[2] = vektor[2]*factor; return result; }
	 * 
	 * private double[] crossProductNormed(double[] v, double[] u){ double
	 * result[] = new double[3]; result[0] = u[1]*v[2]-u[2]*v[1]; result[1] =
	 * u[2]*v[0]-u[0]*v[2]; result[2] = u[0]*v[1]-u[1]*v[0]; double norm =
	 * Math.sqrt(result[0]*result[0]+ result[1]*result[1]+result[2]*result[2]);
	 * result[0] = result[0]/norm; result[1] = result[1]/norm; result[2] =
	 * result[2]/norm; return result; }
	 */

	/**
	 * creates a Style element for a lineString.
	 * 
	 * @param gradient
	 *            specifies color value. 0.0 is green 1.0 is red, 0.5 is yellow
	 * @return KML Style element for a line
	 */
	protected Element createLineStyle(Color gradient) {
		// Color of line
		Element style = doc.createElement("Style");
		Element lineStyle = doc.createElement("LineStyle");
		style.appendChild(lineStyle);
		Element color = createColorElement(gradient);
		lineStyle.appendChild(color);
		Element with = doc.createElement("width");
		with.appendChild(doc.createTextNode("3"));
		lineStyle.appendChild(with);
		return style;
	}

	protected Element createColorElement(Color color) {
		StringBuilder colorABGR = new StringBuilder();
		int alpha = color.getAlpha();
		if (alpha < 10) {
			colorABGR.append("0");
		}
		colorABGR.append(Integer.toHexString(alpha));
		int blue = color.getBlue();
		if (blue < 10) {
			colorABGR.append("0");
		}
		colorABGR.append(Integer.toHexString(blue));
		int green = color.getGreen();
		if (green < 10) {
			colorABGR.append("0");
		}
		colorABGR.append(Integer.toHexString(green));
		int red = color.getRed();
		if (red < 10) {
			colorABGR.append("0");
		}
		colorABGR.append(Integer.toHexString(red));
		Element colorElem = doc.createElement("color");
		colorElem.appendChild(doc.createTextNode(colorABGR.toString()));
		return colorElem;
	}

	protected Color computeColorBlueRed(double gradient) {
		double b = 0.0;
		double r = 0.0;
		double g = 0.0;

		if (gradient < 1) {
			if (gradient < 0.1) {
				r = 255.0;
			} else {
				g = 128.0;
				b = 255.0;
			}
		} else if (Double.isNaN(gradient)) {
			r = 255;
		} else {
			b = 255;
			g = 128;
		}
		return new Color((int) r, (int) g, (int) b, 255);
	}

	protected Color computeColorGreenRed(double gradient) {
		double r = 0.0;
		double g = 0.0;
		double b = 0.0;
		if (gradient < 1) {
			if (gradient < 0.5) {
				g = 255.0;
				r = gradient * 255.0 * 2.0;
			} else {
				r = 255.0;
				g = 255.0 - (gradient - 0.5) * 2.0 * 255.0;
			}

		} else if (Double.isNaN(gradient)) {
			b = 255;
		} else {
			g = 255;
		}
		return new Color((int) r, (int) g, (int) b, 255);
	}

	// returns a map which is keyed by hopSource /hop destination and
	protected Map<Integer, Map<Integer, List<JE802RouteHopRecord>>> computeDuplicateLinks(List<JE802RouteHopRecord> linkRecords) {
		// comparator to sort links according to start time
		Comparator<JE802RouteHopRecord> comp = new Comparator<JE802RouteHopRecord>() {

			@Override
			public int compare(JE802RouteHopRecord o1, JE802RouteHopRecord o2) {
				return o1.getStartTime().compareTo(o2.getStartTime());
			}
		};
		Collections.sort(linkRecords, comp);
		Map<Integer, Map<Integer, List<JE802RouteHopRecord>>> hopLinkMap = new HashMap<Integer, Map<Integer, List<JE802RouteHopRecord>>>();
		for (JE802RouteHopRecord r1 : linkRecords) {
			// init to 1, because 1 link is certainly going over that hop, this
			// is our own link
			int counter = 1;
			for (JE802RouteHopRecord r2 : linkRecords) {
				if (r2 != r1) {
					if ((r1.getHopSource() == r2.getHopSource() && r1.getHopDestination() == r2.getHopDestination())
							|| (r1.getHopDestination() == r2.getHopSource() && r1.getHopSource() == r2.getHopDestination())) {
						double r1Start = r1.getStartTime().getTimeMs();
						double r1Stop = r1.getStopTime().getTimeMs();
						double r2Start = r2.getStartTime().getTimeMs();
						double r2Stop = r2.getStopTime().getTimeMs();
						// condition that two links overlap in time
						if ((r1Start <= r2Start && r2Start <= r1Stop) || (r1Start <= r2Stop && r2Stop <= r1Stop)
								|| (r1Start <= r2Start && r2Stop <= r1Stop)) {
							counter++;
						}
					}
				}
			}

			// set number of neighbors and index of the neighbors
			int index = 0;
			for (JE802RouteHopRecord r2 : linkRecords) {
				if (r2 != r1) {
					if (r1.getHopSource() == r2.getHopSource() && r1.getHopDestination() == r2.getHopDestination()) {
						double r1Start = r1.getStartTime().getTimeMs();
						double r1Stop = r1.getStopTime().getTimeMs();
						double r2Start = r2.getStartTime().getTimeMs();
						double r2Stop = r2.getStopTime().getTimeMs();
						if ((r1Start <= r2Start && r2Start <= r1Stop) || (r1Start <= r2Stop && r2Stop <= r1Stop)
								|| (r1Start <= r2Start && r2Stop <= r1Stop)) {
							index++;
							r2.setNeighborCount(counter);
							r2.setIndexInNeighbors(index);
						}
					}
				}
			}
			assert (index == counter - 1);
			Map<Integer, List<JE802RouteHopRecord>> destinationMap = hopLinkMap.get(r1.getHopSource());
			if (destinationMap == null) {
				destinationMap = new HashMap<Integer, List<JE802RouteHopRecord>>();
				hopLinkMap.put(r1.getHopSource(), destinationMap);
			}
			List<JE802RouteHopRecord> linksOverTime = destinationMap.get(r1.getHopDestination());
			if (linksOverTime == null) {
				linksOverTime = new ArrayList<JE802RouteHopRecord>();
				destinationMap.put(r1.getHopDestination(), linksOverTime);
			}
			linksOverTime.add(r1);
		}
		return hopLinkMap;
	}

	protected class JE802RouteHopRecord {

		private int hopSource;

		private int hopDestination;

		private JETime startTime;

		private JETime stopTime;

		private Color color;

		private int routeSource;

		private int routeDestination;

		// number of links going over same edge at same time
		private int neighborCount;

		// all links going over the same link are ordered by an increasing
		// number
		private int indexInNeighbors;

		public JE802RouteHopRecord(int hopSource, int hopDestination, JETime startTime, JETime stopTime, Color color,
				int routeSource, int routeDestination) {
			this.hopSource = hopSource;
			this.hopDestination = hopDestination;
			this.startTime = startTime;
			this.stopTime = stopTime;
			this.color = color;
			this.routeSource = routeSource;
			this.routeDestination = routeDestination;
		}

		public Color getColor() {
			return color;
		}

		public void setIndexInNeighbors(int indexInNeighbors) {
			this.indexInNeighbors = indexInNeighbors;
		}

		public void setNeighborCount(int neighborCount) {
			this.neighborCount = neighborCount;
		}

		public int getIndexInNeighbors() {
			return indexInNeighbors;
		}

		public int getNeighborCount() {
			return neighborCount;
		}

		public int getHopDestination() {
			return hopDestination;
		}

		public int getHopSource() {
			return hopSource;
		}

		public JETime getStartTime() {
			return startTime;
		}

		public JETime getStopTime() {
			return stopTime;
		}

		public int getRouteSource() {
			return routeSource;
		}

		public int getRouteDestination() {
			return routeDestination;
		}
	}

	protected class JE802LinkRecord {

		private int sourceAddress;
		private int destinationAddress;
		private ArrayList<Double> data;
		private int startIndex;
		private int stopIndex;

		public JE802LinkRecord(int SA, int DA, int startIndex, int stopIndex, ArrayList<Double> data) {
			this.sourceAddress = SA;
			this.destinationAddress = DA;
			this.data = data;
			this.startIndex = startIndex;
			this.stopIndex = stopIndex;
		}

		public ArrayList<Double> getData() {
			return data;
		}

		public int getDestinationAddress() {
			return destinationAddress;
		}

		public int getSourceAddress() {
			return sourceAddress;
		}

		public int getStartIndex() {
			return startIndex;
		}

		public int getStopIndex() {
			return stopIndex;
		}
	}
}
