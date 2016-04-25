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
import zzInfra.statistics.JEStatEvalThrp;

public class JE802TrafficBlocksKml extends JE802StationModelGenerator {

	private final double blockHeight = 0.51;

	private final String redBlock = "redBlock.dae";

	private final String greyBlock = "greyBlock.dae";

	private final double factor;

	private final List<JE802StationRecord> throughputs = new ArrayList<JE802StationRecord>();

	private final boolean isVisible;

	public JE802TrafficBlocksKml(final Document doc,
			final List<JE802Station> stations, final String filename,
			final double blocksize, boolean visible) {
		super(doc, stations, filename, false);
		// split up the throughput of a route in separate links
		this.factor = 1 / blocksize;
		this.isVisible = visible;
		for (JE802Station station : stations) {
			List<JE802TrafficGen> gens = station.getTrafficGenList();
			int source = station.getMac().getMacAddress();
			for (JE802TrafficGen gen : gens) {
				if (gen != null && gen.isEvaluatingThrp()
						&& gen.isEvaluatingOffer() && gen.is_active()) {
					ArrayList<JE802HopInfo> hops = gen.getHopAddresses();
					// start and stop index of evaluation of this traffic gen
					int start = gen.getEvaluationStartTimeStep();
					int stop = gen.getEvaluationStopTimeStep();
					int lastHopIndex = hops.size() - 1;
					int destination = hops.get(lastHopIndex).getAddress();
					ArrayList<Double> offerData = new ArrayList<Double>();
					ArrayList<Double> thrpData = new ArrayList<Double>();
					for (int j = start; j < stop; j++) {
						List<JEStatEvalThrp> thrps = gen.getThrpResults();
						if (thrps != null && !thrps.isEmpty()) {
							int numPackets = (Integer) gen.getThrpResults()
									.get(lastHopIndex).getEvalList3()
									.get(j - start);
							double avgPacketSize = (Double) gen
									.getThrpResults().get(lastHopIndex)
									.getEvalList5().get(j - start);
							double aFactor = 1000.0 / gen.getStatEval()
									.getEvaluationInterval().getTimeMs(); // samples
																			// per
																			// second
							double thrp = numPackets * avgPacketSize * aFactor
									/ 125000.0; // bytes
												// to
												// Mb/s
							thrpData.add(thrp);
							numPackets = (Integer) gen.getOffer()
									.getEvalList3().get(j - start);
							avgPacketSize = (Double) gen.getOffer()
									.getEvalList5().get(j - start);
							aFactor = 1000.0 / gen.getStatEval()
									.getEvaluationInterval().getTimeMs(); // samples
																			// per
																			// second
							double offer = numPackets * avgPacketSize * aFactor
									/ 125000.0; // bytes
												// to
												// Mb/s
							offerData.add(offer);
						}
					}
					JE802StationRecord offer = new JE802StationRecord(source,
							start, stop, offerData);
					JE802StationRecord thrp = new JE802StationRecord(source,
							start, stop, thrpData);
					this.stationData.add(offer);
					this.throughputs.add(thrp);
					source = destination;
				}
			}
		}
	}

	private List<Element> createOfferBlocks() {
		ArrayList<Element> allBlocks = new ArrayList<Element>();
		for (JE802Station station : this.theStations) {
			ArrayList<Element> stationBlocks = new ArrayList<Element>();
			if (!station.getTrafficGenList().isEmpty()
					&& station.getTrafficGenList().get(0).getDA() != 0) {
				for (int k = 0; k < this.stationData.size(); k++) {
					JE802StationRecord offerRecord = this.stationData.get(k);
					JE802StationRecord thrpRecord = this.throughputs.get(k);
					if (station.getMac().getMacAddress() == offerRecord
							.getSourceAddress()) {
						int start = offerRecord.getStartIndex();
						JETime interval = station.getStatEval()
								.getEvaluationInterval();
						JETime currentTime = station.getStatEval()
								.getEvaluationStarttime();

						for (int j = start; j < offerRecord.getStopIndex(); j++) {
							int blockCount = new Double(offerRecord.getData()
									.get(j - start) * this.factor).intValue();
							double[] position = convertXYZtoLatLonAlt(
									station.getXLocation(currentTime),
									station.getYLocation(currentTime),
									station.getZLocation(currentTime));
							for (int i = 0; i < blockCount; i++) {
								boolean isMobile = station.isMobile();
								Double scaleFactor = 0.0;
								if (isMobile) {
									scaleFactor = 0.4;
								} else {
									scaleFactor = 1.1;
								}

								Element altitudeMode = this.doc
										.createElement("altitudeMode");
								altitudeMode.appendChild(this.doc
										.createTextNode("relativeToGround"));

								// Location
								Element location = this.doc
										.createElement("Location");
								Element longitude = this.doc
										.createElement("longitude");
								longitude.appendChild(this.doc
										.createTextNode(new Double(position[1])
												.toString()));
								Element latitude = this.doc
										.createElement("latitude");
								latitude.appendChild(this.doc
										.createTextNode(new Double(position[0])
												.toString()));
								Element altitude = this.doc
										.createElement("altitude");
								Double height = 0.2
										+ i
										* (this.blockHeight * scaleFactor + 0.5
												* this.blockHeight
												* scaleFactor);
								if (isMobile) {
									height += JE802KmlGenerator.userModelHeight;
								} else {
									height += JE802KmlGenerator.antennaModelHeight;
								}
								altitude.appendChild(this.doc.createTextNode(height.toString()));
								location.appendChild(longitude);
								location.appendChild(latitude);
								location.appendChild(altitude);

								// scale
								Element scale = this.doc.createElement("Scale");
								Element x = this.doc.createElement("x");

								x.appendChild(this.doc
										.createTextNode(scaleFactor.toString()));
								Element y = this.doc.createElement("y");
								y.appendChild(this.doc
										.createTextNode(scaleFactor.toString()));
								Element z = this.doc.createElement("z");
								z.appendChild(this.doc
										.createTextNode(scaleFactor.toString()));
								scale.appendChild(x);
								scale.appendChild(y);
								scale.appendChild(z);

								// orientation
								Element orientation = this.doc
										.createElement("Orientation");

								Double headingRotation = 0.0;
								Element heading = this.doc
										.createElement("heading");
								heading.appendChild(this.doc
										.createTextNode(headingRotation
												.toString()));
								Element tilt = this.doc.createElement("tilt");
								tilt.appendChild(this.doc.createTextNode("0"));
								Element roll = this.doc.createElement("roll");
								roll.appendChild(this.doc.createTextNode("0"));
								orientation.appendChild(heading);
								orientation.appendChild(tilt);
								orientation.appendChild(roll);

								// modelfile
								Element linkElem = this.doc
										.createElement("Link");
								Element href = this.doc.createElement("href");
								String path = "animation_files/";
								int thrp = new Double(thrpRecord.getData().get(
										j - start)
										* this.factor).intValue();
								if (i + 1 <= blockCount - thrp) {
									path = path + this.redBlock;
								} else {
									path = path + this.greyBlock;
								}
								href.appendChild(this.doc.createTextNode(path));
								linkElem.appendChild(href);

								// assemble model
								Element model = this.doc.createElement("Model");
								model.appendChild(altitudeMode);
								model.appendChild(location);
								model.appendChild(scale);
								model.appendChild(orientation);
								model.appendChild(linkElem);

								// placemark wrapping the model tag
								Element placemark = this.doc
										.createElement("Placemark");
								Element name = this.doc.createElement("name");
								name.appendChild(this.doc
										.createTextNode("Station "
												+ station.getMac().getMacAddress()));
								Element timeSpan = createTimeSpan(currentTime,
										currentTime.plus(interval));

								placemark.appendChild(name);
								placemark.appendChild(timeSpan);
								placemark.appendChild(model);

								stationBlocks.add(placemark);
							}
							currentTime = currentTime.plus(interval);
						}
					}
				}
			}
			if (!stationBlocks.isEmpty()) {
				Element stationFolder = createFolder(stationBlocks, "Station "
						+ station.getMac().getMacAddress(), !this.isVisible);
				allBlocks.add(stationFolder);
			}
		}
		return allBlocks;
	}

	@Override
	public Element createDOM() {
		List<Element> models = createOfferBlocks();
		return createFolder(models,
				"Traffic (" + 1 / this.factor + "Mb/block)", !this.isVisible);
	}

}
