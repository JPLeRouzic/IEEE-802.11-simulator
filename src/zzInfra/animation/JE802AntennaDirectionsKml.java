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
import java.util.List;

import zzInfra.kernel.JETime;
import zzInfra.layer1.JE802Mobility;
import zzInfra.layer1.JEAntenna;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.ARC.JE802Station;
import zzInfra.util.Vector3d;

public class JE802AntennaDirectionsKml extends JE802LinkKmlGenerator {

	public JE802AntennaDirectionsKml(Document doc, List<JE802Station> stations) {
		super(doc, stations);
	}

	/**
	 * Creates all links of stations colored according to their throughput
	 * 
	 * @return List of KML elements representing the links between all stations
	 */
	private List<Element> createAntennaDirections() {
		ArrayList<Element> antennaFolders = new ArrayList<Element>();
		for (JE802Station station : theStations) {
			JEAntenna antenna = station.getPhy().getAntenna();
			JE802Mobility mob = station.getMobility();
			if (!antenna.isDirectional()) {
				continue;
			}
			int positionCount;
			JETime currentTime = station.getStatEval().getEvaluationStarttime();
			JETime interval = station.getStatEval().getEvaluationInterval();
			if (station.isMobile()) {
				double end = station.getStatEval().getEvaluationEnd().getTimeMs();
				positionCount = (int) ((end - currentTime.getTimeMs()) / interval.getTimeMs());
			} else {
				positionCount = 1;
			}
			for (int i = 0; i < positionCount; i++) {
				Vector3d src = new Vector3d(mob.getXLocation(currentTime), mob.getYLocation(currentTime),
						mob.getZLocation(currentTime));

				Vector3d dir = antenna.getDirectionForLatLon(src.getLat(), src.getLon(), mob.getTraceHeading(currentTime))
						.normalize();

				// transform antenna direction vector to the (x,y,z) coordinate
				// system local to the stations position
				// dir = dir.rotate(90,2).rotate(-(src.getLat()-90),
				// 1).rotate(src.getLon(), 2);
				Vector3d dst = src.add(dir);

				Element timeSpan;
				if (station.isMobile()) {
					src.setAlt(src.getAlt() + userModelHeight);
					dst.setAlt(dst.getAlt() + userModelHeight);
					timeSpan = createTimeSpan(currentTime, currentTime.plus(interval));
				} else {
					src.setAlt(src.getAlt() + antennaModelHeight);
					dst.setAlt(dst.getAlt() + antennaModelHeight);
					timeSpan = createTimeSpan(station.getStatEval().getEvaluationStarttime(), station.getStatEval()
							.getEvaluationEnd());
				}

				ArrayList<Element> antennaFolder = new ArrayList<Element>();
				Element style = createLineStyle(Color.MAGENTA);
				Element line = createLine(src, dst);
				Element placemark = doc.createElement("Placemark");
				placemark.appendChild(line);
				placemark.appendChild(style);
				placemark.appendChild(timeSpan);
				antennaFolder.add(placemark);
				currentTime = currentTime.plus(interval);
				Element folder = createFolder(antennaFolder, "Station " + station.getMacAddress(), true);
				antennaFolders.add(folder);
			}
		}
		return antennaFolders;
	}

	@Override
	public Element createDOM() {
		String folderName = "Antenna Directions";
		List<Element> antennaElements = createAntennaDirections();
		Element linkFolder = createFolder(antennaElements, folderName, false);
		return linkFolder;
	}
}
