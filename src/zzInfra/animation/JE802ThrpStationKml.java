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

import zzInfra.layer3_network.JE802HopInfo;
import zzInfra.layer5_application.JE802TrafficGen;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.ARC.JE802Station;

public class JE802ThrpStationKml extends JE802StationKmlGenerator {

	private final double maxTp;

	public JE802ThrpStationKml(Document doc, List<JE802Station> stations, String filename, double maxTp, boolean useGreenRed) {
		super(doc, stations, filename, useGreenRed);
		this.maxTp = maxTp;

		// put throughput data of each link into a separate station record,
		// which is then accounted to the correct Station
		for (JE802Station station : stations) {
			List<JE802TrafficGen> gens = station.getTrafficGenList();
			int source = station.getMacAddress();
			for (JE802TrafficGen gen : gens) {
				if (gen != null && gen.isEvaluatingThrp() && gen.is_active()) {
					ArrayList<JE802HopInfo> hops = gen.getHopAddresses();
					int start = gen.getEvaluationStartTimeStep();
					int stop = gen.getEvaluationStopTimeStep();
					// go though all hops of a route
					for (int i = 0; i < hops.size(); i++) {
						int destination = hops.get(i).getAddress();
						ArrayList<Double> data = new ArrayList<Double>();
						for (int j = start; j < stop; j++) {
							int numPackets = (Integer) gen.getThrpResults().get(i).getEvalList3().get(j - start);
							double avgPacketSize = (Double) gen.getThrpResults().get(i).getEvalList5().get(j - start);
							double factor = 1000.0 / gen.getStatEval().getEvaluationInterval().getTimeMs(); // samples
																											// per
																											// second
							double rateMb = (numPackets * avgPacketSize * factor) / 125000.0; // bytes
																								// to
																								// Mb/s
							data.add(rateMb);
						}
						// traffic on a route from hop x to hop x+1 is accounted
						// to station x
						JE802StationRecord link = new JE802StationRecord(source, start, stop, data);
						stationData.add(link);
						source = destination;
					}
				}
			}
		}
	}

	/**
	 * Creates Throughput icons of a all stations over time. Each station is in
	 * a separate folder
	 * 
	 * @return KML elements of all stations colored according to throughput
	 */
	private List<Element> createThrpIcons() {
		ArrayList<Element> placeMarks = new ArrayList<Element>();
		for (JE802Station station : theStations) {
			List<Element> stationFolder = new ArrayList<Element>();
			double aggregatedThrp[] = new double[station.getStatEval().getSampleCount()];
			for (JE802StationRecord record : stationData) {
				if (record.getSourceAddress() == station.getMacAddress()) {
					int start = record.getStartIndex();
					int stop = record.getStopIndex();
					for (int i = start; i < stop; i++) {
						aggregatedThrp[i] += record.getData().get(i - start);
					}
				}
			}
			boolean isMobile = station.isMobile();

			JETime currentTime = station.getStatEval().getEvaluationStarttime();
			JETime interval = station.getStatEval().getEvaluationInterval();

			for (int i = 0; i < aggregatedThrp.length; i++) {
				double thrp = aggregatedThrp[i];
				// placemark element
				Element placemark = doc.createElement("Placemark");

				double x = station.getXLocation(currentTime);
				double y = station.getYLocation(currentTime);
				double z = station.getZLocation(currentTime);
				double[] position = convertXYZtoLatLonAlt(x, y, z);
				// point element
				Element point = null;
				if (station.isMobile()) {
					point = createPoint(position[0], position[1], userModelHeight);
				} else {
					point = createPoint(position[0], position[1], antennaModelHeight);
				}

				placemark.appendChild(point);

				// style depending on throughput
				double gradient = thrp / maxTp;
				Element style = null;
				if (isMobile) {
					style = createStationStyle(gradient, mobileIconPath);
				} else {
					style = createStationStyle(gradient, antennaIconPath);
				}
				placemark.appendChild(style);

				Element timeSpan = createTimeSpan(currentTime, currentTime.plus(interval));
				currentTime = currentTime.plus(interval);
				placemark.appendChild(timeSpan);
				stationFolder.add(placemark);
			}
			Element folder = createFolder(stationFolder, "Station " + station.getMacAddress(), true);
			placeMarks.add(folder);
		}
		return placeMarks;
	}

	@Override
	public Element createDOM() {
		String folderName = "Throughput Stations ";
		if (useGreenRed) {
			folderName += "green-red(low-high)";
		} else {
			folderName += "red-blue(low-high)";
		}
		List<Element> thrpFolders = createThrpIcons();
		Element thrpStationFolder = createFolder(thrpFolders, folderName, false);
		return thrpStationFolder;
	}

}
