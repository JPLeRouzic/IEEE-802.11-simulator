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

import zzInfra.layer3_network.JE802HopInfo;
import zzInfra.layer5_application.JE802TrafficGen;
import zzInfra.kernel.JETime;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.ARC.JE802Station;

public class JE802DelayLinksKml extends JE802LinkKmlGenerator {

	private final double maxDelayMs;

	private final boolean useGreenRed;

	public JE802DelayLinksKml(Document doc, List<JE802Station> stations, double maxDelayMs, boolean useGreenRed) {
		super(doc, stations);
		this.maxDelayMs = maxDelayMs;
		this.useGreenRed = useGreenRed;
		for (JE802Station station : stations) {
			List<JE802TrafficGen> gens = station.getTrafficGenList();
			for (JE802TrafficGen gen : gens) {
				if (gen != null && gen.is_active() && gen.isEvaluatingThrp()) {
					int source = station.getMacAddress();
					ArrayList<JE802HopInfo> hops = gen.getHopAddresses();
					int start = gen.getEvaluationStartTimeStep();
					int stop = gen.getEvaluationStopTimeStep();
					for (int i = 0; i < hops.size(); i++) {
						int destination = hops.get(i).getAddress();
						ArrayList<Double> data = new ArrayList<Double>();
						for (int j = start; j < stop; j++) {
							double delay = (Double) gen.getDelayResults().get(i).getEvalList5().get(j - start);
							data.add(delay);
						}
						JE802LinkRecord link = new JE802LinkRecord(source, destination, start, stop, data);
						links.add(link);
						source = destination;
					}
				}
			}
		}
	}

	/**
	 * 
	 * @return List of Placemarks (lines) which represent delays of all links
	 *         over time
	 */
	private List<Element> createDelayLinks() {
		ArrayList<Element> linkFolders = new ArrayList<Element>();
		for (JE802Station station1 : theStations) {
			for (JE802Station station2 : theStations) {
				if (!station1.equals(station2)) {
					double aggregatedDelay[] = new double[station1.getStatEval().getSampleCount()];
					int senderCount[] = new int[station1.getStatEval().getSampleCount()];
					boolean hasLink = false;
					int start = 0;
					for (JE802LinkRecord link : links) {
						if (link.getSourceAddress() == station1.getMacAddress()
								&& link.getDestinationAddress() == station2.getMacAddress()) {
							hasLink = true;
							ArrayList<Double> linkData = link.getData();
							start = link.getStartIndex();
							for (int i = start; i < link.getStopIndex(); i++) {
								aggregatedDelay[i] += linkData.get(i - start);
								senderCount[i]++;
							}
						}
					}

					ArrayList<Element> linkFolder = new ArrayList<Element>();
					if (hasLink) {
						JETime startTime = station1.getStatEval().getEvaluationStarttime();
						JETime interval = station1.getStatEval().getEvaluationInterval();
						JETime currentTime = startTime;
						for (int i = 0; i < aggregatedDelay.length; i++) {
							double avgDelay = aggregatedDelay[i] / senderCount[i];
							double gradient = avgDelay / maxDelayMs;
							Color color;
							if (useGreenRed) {
								color = computeColorGreenRed(gradient);
							} else {
								color = computeColorBlueRed(gradient);
							}
							Element link = createLink(color, currentTime, currentTime.plus(interval), 0, 1, station1, station2);
							linkFolder.add(link);
							currentTime = currentTime.plus(interval);
						}
						Element folder = createFolder(linkFolder, station1.getMacAddress() + " to " + station2.getMacAddress(),
								true);
						linkFolders.add(folder);
					}
				}
			}
		}
		return linkFolders;
	}

	@Override
	public Element createDOM() {
		String folderName = "Delay Links ";
		if (useGreenRed) {
			folderName += "green-red(low-high)";
		} else {
			folderName += "red-blue(low-high)";
		}
		List<Element> delayLinks = createDelayLinks();
		Element delayLinkFolder = createFolder(delayLinks, folderName, false);
		return delayLinkFolder;
	}

}
