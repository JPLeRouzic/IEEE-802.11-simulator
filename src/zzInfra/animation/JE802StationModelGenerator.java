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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.ARC.JE802Station;

public abstract class JE802StationModelGenerator extends JE802KmlGenerator {

	protected final String mobileIconPath;

	protected final String antennaIconPath;

	protected List<JE802StationRecord> stationData = new ArrayList<JE802StationRecord>();

	protected final boolean useGreenRed;

	public JE802StationModelGenerator(Document doc, List<JE802Station> stations, String filename, boolean useGreenRed) {
		super(doc, stations);
		this.useGreenRed = useGreenRed;
		this.mobileIconPath = "animation_files/user.jpg";
		this.antennaIconPath = "animation_files/Antenna.png";
	}

	/**
	 * 
	 * @param value
	 *            value of throughput or delay
	 * @param isThrp
	 *            true if value is throughput
	 * @return styleElement with color
	 */
	protected Element createStationStyle(double gradient, String iconPath) {
		Element href = doc.createElement("href");
		href.appendChild(doc.createTextNode(iconPath));
		Element icon = doc.createElement("Icon");
		icon.appendChild(href);
		Element color;
		if (useGreenRed) {
			color = greenToRedGradient(gradient, 255);
		} else {
			color = redToBlueGradient(gradient, 255);
		}
		Element iconStyle = doc.createElement("IconStyle");
		iconStyle.appendChild(color);
		iconStyle.appendChild(icon);
		Element scale = doc.createElement("scale");
		scale.appendChild(doc.createTextNode("0.5"));
		iconStyle.appendChild(scale);
		Element style = doc.createElement("Style");
		style.appendChild(iconStyle);
		return style;
	}

	/**
	 * Creates KML element of a point
	 * 
	 * @param latitude
	 * @param longitude
	 * @param zMeters
	 *            meters on z-axis above ground
	 * @return KML Point element
	 */
	protected Element createPoint(double lat, double lon, double alt) {

		// calculate position of station relative to baseCoordinates
		Element coordinates = doc.createElement("coordinates");
		coordinates.appendChild(doc.createTextNode(lon + "," + lat + "," + alt));
		Element altitudeMode = doc.createElement("altitudeMode");
		altitudeMode.appendChild(doc.createTextNode("relativeToGround"));
		Element point = doc.createElement("Point");
		point.appendChild(altitudeMode);
		point.appendChild(coordinates);
		return point;
	}

	protected class JE802StationRecord {
		private int sourceAddress;
		private ArrayList<Double> data;
		private int startIndex;
		private int stopIndex;

		public JE802StationRecord(int SA, int startIndex, int stopIndex, ArrayList<Double> data) {
			this.sourceAddress = SA;
			this.data = data;
			this.startIndex = startIndex;
			this.stopIndex = stopIndex;
		}

		public ArrayList<Double> getData() {
			return data;
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
