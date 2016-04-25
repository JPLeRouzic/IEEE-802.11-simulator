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

import java.util.ArrayList;
import java.util.List;

import zzInfra.kernel.JETime;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.ARC.JE802Station;

public abstract class JE802KmlGenerator {

	protected final double earthRadius = 6367444.25;

	// earths circumference
	private final double circumEarth = 2 * this.earthRadius * Math.PI;

	// height of the antenna model in meters
	public final static double antennaModelHeight = 20;

	public final static double userModelHeight = 2.0;

	public final static double antennaConeHeight = 2; // antennaConeWidth

	// document, needed for creating elements
	protected Document doc;

	protected List<JE802Station> theStations;

	public JE802KmlGenerator(final Document doc, final List<JE802Station> stations) {
		this.doc = doc;
		this.theStations = new ArrayList<JE802Station>(stations);
	}

	public abstract Element createDOM();

	/**
	 * 
	 * @param timeStep
	 *            number of current timestep
	 * @return KML TimeSpan element with <begin> and <end> tag
	 */
	protected Element createTimeSpan(final JETime timeStep1, final JETime timeStep2) {
		Element begin = this.doc.createElement("begin");
		Integer start = new Integer((int) timeStep1.getTimeMs());
		begin.appendChild(this.doc.createTextNode(start.toString()));
		Element end = this.doc.createElement("end");
		Integer endTime = new Integer((int) timeStep2.getTimeMs());
		end.appendChild(this.doc.createTextNode(endTime.toString()));
		Element timeSpan = this.doc.createElement("TimeSpan");
		timeSpan.appendChild(begin);
		timeSpan.appendChild(end);
		return timeSpan;
	}

	/**
	 * 
	 * @param element
	 *            single element which is then put into the folder
	 * @param name
	 *            name of the folder
	 * @param hideElements
	 *            indicates whether folder is expandable or not
	 * @return
	 */
	protected Element createFolder(final Element element, final String name, final boolean hideElements) {
		List<Element> list = new ArrayList<Element>();
		list.add(element);
		return createFolder(list, name, hideElements);
	}

	/**
	 * 
	 * @param elements
	 *            list of elements which should be put into the folder
	 * @param name
	 *            of the folder
	 * @param hideElements
	 *            indicates whether folder is expandable or not
	 * @return
	 */
	protected Element createFolder(final List<Element> elements, final String name, final boolean hideElements) {
		Element folder = this.doc.createElement("Folder");
		Element folderName = this.doc.createElement("name");
		folderName.appendChild(this.doc.createTextNode(name));
		folder.appendChild(folderName);

		Element open = this.doc.createElement("open");
		open.appendChild(this.doc.createTextNode("0"));

		Element visibility = this.doc.createElement("visibility");
		visibility.appendChild(this.doc.createTextNode("0"));
		folder.appendChild(visibility);
		folder.appendChild(open);
		for (Element elem : elements) {
			folder.appendChild(elem);
		}
		if (hideElements) {
			Element styleUrl = this.doc.createElement("styleUrl");
			styleUrl.appendChild(this.doc.createTextNode("hideChildren"));
			folder.appendChild(styleUrl);
		}
		return folder;
	}

	/**
	 * 
	 * @param meters
	 *            distance in meters from origin
	 * @return degrees from origin
	 */
	protected double meters2DegreesLongitude(final double meters, final double lat) {
		return 360.0 / (this.circumEarth * Math.cos(Math.toRadians(lat))) * meters;
	}

	/**
	 * 
	 * @param meters
	 *            distance in meters from origin
	 * @return degrees from origin
	 */
	protected double meters2DegreesLatitude(final double meters) {
		return 360.0 / this.circumEarth * meters;
	}

	protected Element redToBlueGradient(final double gradient, final int alpha) {
		StringBuilder colorBGR = new StringBuilder();
		/*
		 * if(alpha>=0 && alpha<=255){ //alpha and blue
		 * colorBGR.append(Integer.toHexString(alpha)); } else {
		 * colorBGR.append("ff"); }
		 */
		colorBGR.append("ff");
		Double b = 0.0;
		Double r = 0.0;
		Double g = 0.0;

		if (gradient < 1) {
			/*
			 * r = 255-gradient; b = gradient*255.0; g= gradient*128;
			 */
			if (gradient < 0.5) {
				r = 255.0;
			} else {
				g = 128.0;
				b = 255.0;
			}
			if (b < 16) {
				colorBGR.append("0");
			}
			colorBGR.append(Integer.toHexString(b.intValue()));
			if (g < 16) {
				colorBGR.append("0");
			}
			colorBGR.append(Integer.toHexString(g.intValue()));
			if (r < 16) {
				colorBGR.append("0");
			}
			colorBGR.append(Integer.toHexString(r.intValue()));
		} else if (Double.isNaN(gradient)) {
			colorBGR.append("0000ff"); // red if Nan
		} else {
			colorBGR.append("ff8000"); // blue if above
		}
		Element color = this.doc.createElement("color");
		color.appendChild(this.doc.createTextNode(colorBGR.toString()));
		return color;
	}

	/**
	 * gradient from green over yellow to red
	 * 
	 * @param gradient
	 *            percentage gradient value from 0.0 to 1.0, 0.0 is green, 1.0
	 *            is red
	 * @param alpha
	 *            value of transparency, 0 to 255.
	 * @return KML color element
	 */
	protected Element greenToRedGradient(final double gradient, final int alpha) {
		StringBuilder colorBGR = new StringBuilder();
		if (alpha >= 0 && alpha <= 255) {
			// alpha and blue
			colorBGR.append(Integer.toHexString(alpha) + "00");
		} else {
			colorBGR.append("ff00");
		}

		Double r = 0.0;
		Double g = 0.0;

		if (gradient < 1) {
			if (gradient < 0.5) {
				g = 255.0;
				r = gradient * 255.0 * 2.0;
			} else {
				r = 255.0;
				g = 255.0 - (gradient - 0.5) * 2.0 * 255.0;
			}
			if (g < 16) {
				colorBGR.append("0");
			}
			colorBGR.append(Integer.toHexString(g.intValue()));
			if (r < 16) {
				colorBGR.append("0");
			}
			colorBGR.append(Integer.toHexString(r.intValue()));
		} else if (Double.isNaN(gradient)) {
			colorBGR.append("ff00");
		} else {
			colorBGR.append("00ff");
		}
		Element color = this.doc.createElement("color");
		color.appendChild(this.doc.createTextNode(colorBGR.toString()));
		return color;
	}

	protected double[] convertXYZtoLatLonAlt(final double x, final double y, final double z) {
		double[] latLonAlt = new double[3];
		latLonAlt[0] = -Math.toDegrees(Math.acos(z / Math.sqrt(x * x + y * y + z * z))) + 90;
		latLonAlt[1] = Math.toDegrees(Math.atan2(y, x));
		latLonAlt[2] = Math.sqrt(x * x + y * y + z * z) - this.earthRadius;
		return latLonAlt;
	}

	/**
	 * calculates distance in meters of two points given in longitude and
	 * latitude
	 * 
	 * @param centerLat
	 * @param centerLong
	 * @param stationLat
	 * @param stationLong
	 * @return distance of the two points in meters
	 */
	protected double calculateDistance(final double centerLat, final double centerLong, final double stationLat,
			final double stationLong) {
		double factor = Math.PI / 180;
		double centerLatRad = centerLat * factor;
		double centerLongRad = centerLong * factor;
		double stationLatRad = stationLat * factor;
		double stationLongRad = stationLong * factor;
		double t1 = Math.sin(centerLatRad) * Math.sin(stationLatRad);
		double t2 = Math.cos(centerLatRad) * Math.cos(stationLatRad);
		double distance = Math.acos(t1 + t2 * Math.cos(stationLongRad - centerLongRad)) * this.earthRadius;
		return distance;
	}
}
