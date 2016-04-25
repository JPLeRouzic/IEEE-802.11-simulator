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
import zzInfra.kernel.JETime;

public class JE802ReuseOverlay extends JE802KmlGenerator {

	// path to the reuse overlay image
	private final String coverageImagePath = "circle.png";

	// show reuse distance of mobile stations? yes/no
	private final boolean showMobile;

	private final double reuseDistance;

	private final String filename;

	public JE802ReuseOverlay(Document doc, List<JE802Station> stations, String filename, boolean showMobile, double reuseDistance) {
		super(doc, stations);
		this.showMobile = showMobile;
		this.reuseDistance = reuseDistance;
		this.filename = filename;
	}

	/**
	 * 
	 * @return KML Overlay elements for reuse distances of stations
	 */
	private List<Element> createReuseDistanceOverlays() {
		ArrayList<Element> allReuseOverlays = new ArrayList<Element>();
		for (JE802Station station : theStations) {
			int positionCount;
			JETime currentTime = station.getStatEval().getEvaluationStarttime();
			JETime interval = station.getStatEval().getEvaluationInterval();
			if (station.isMobile()) {
				double endTime = station.getStatEval().getEvaluationEnd().getTimeMs();
				positionCount = (int) ((endTime - currentTime.getTimeMs()) / interval.getTimeMs());
			} else {
				positionCount = 1;
			}
			if (showMobile || !station.isMobile()) {
				for (int i = 0; i < positionCount; i++) {
					double[] position = convertXYZtoLatLonAlt(station.getXLocation(currentTime),
							station.getYLocation(currentTime), station.getZLocation(currentTime));

					Double northEdge = position[0] + meters2DegreesLatitude(reuseDistance);
					Element north = doc.createElement("north");
					north.appendChild(doc.createTextNode(northEdge.toString()));

					Double southEdge = position[0] - meters2DegreesLatitude(reuseDistance);
					Element south = doc.createElement("south");
					south.appendChild(doc.createTextNode(southEdge.toString()));

					Double eastEdge = position[1] + meters2DegreesLongitude(reuseDistance, position[0]);
					Element east = doc.createElement("east");
					east.appendChild(doc.createTextNode(eastEdge.toString()));

					Double westEdge = position[1] - meters2DegreesLongitude(reuseDistance, position[0]);
					Element west = doc.createElement("west");
					west.appendChild(doc.createTextNode(westEdge.toString()));

					Element box = doc.createElement("LatLonBox");
					box.appendChild(north);
					box.appendChild(south);
					box.appendChild(east);
					box.appendChild(west);

					Element icon = doc.createElement("Icon");
					Element href = doc.createElement("href");
					href.appendChild(doc.createTextNode("animation_files/" + coverageImagePath));
					icon.appendChild(href);

					Element timeSpan = createTimeSpan(currentTime, currentTime.plus(interval));
					currentTime = currentTime.plus(interval);
					Element overlay = doc.createElement("GroundOverlay");
					overlay.appendChild(box);
					overlay.appendChild(icon);
					overlay.appendChild(timeSpan);

					Element stationFolder = createFolder(overlay, "Station " + station.getMacAddress(), true);
					allReuseOverlays.add(stationFolder);
				}
			}

		}
		return allReuseOverlays;
	}

	@Override
	public Element createDOM() {
		List<Element> overlays = createReuseDistanceOverlays();
		Element overlayFolder = createFolder(overlays, "ReuseDistance", false);
		return overlayFolder;
	}

}
