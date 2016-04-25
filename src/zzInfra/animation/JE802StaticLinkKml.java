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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zzInfra.layer1.JE802Mobility;
import zzInfra.layer3_network.JE802HopInfo;
import zzInfra.layer5_application.JE802TrafficGen;
import zzInfra.ARC.JE802Station;
import zzInfra.kernel.JETime;
import zzInfra.util.Vector3d;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class JE802StaticLinkKml extends JE802LinkKmlGenerator {

	private Color linkColor;

	private String colorName;

	private Map<Integer, JE802Station> stationMap = new HashMap<Integer, JE802Station>();

	public JE802StaticLinkKml(Document doc, List<JE802Station> stations, Color aLinkColor, String colorName) {
		super(doc, stations);
		this.linkColor = aLinkColor;
		this.colorName = colorName;
		for (JE802Station station : stations) {
			stationMap.put(station.getMacAddress(), station);
			List<JE802TrafficGen> gens = station.getTrafficGenList();
			for (JE802TrafficGen gen : gens) {
				if (gen != null && gen.is_active()) {
					int source = station.getMacAddress();
					ArrayList<JE802HopInfo> hops = gen.getHopAddresses();
					// start and stop index of evaluation of this traffic gen
					for (int i = 0; i < hops.size(); i++) {
						int destination = hops.get(i).getAddress();
						JE802LinkRecord link = new JE802LinkRecord(source, destination, 0, 0, null);
						links.add(link);
						source = destination;
					}
				}
			}
		}
	}

	private List<Element> createStaticLinks(Color color) {
		List<Element> linkList = new ArrayList<Element>();
		for (JE802LinkRecord record : links) {
			JE802Station station1 = stationMap.get(record.getSourceAddress());
			JE802Station station2 = stationMap.get(record.getDestinationAddress());
			if (station1 != null && station2 != null) {

				JE802Mobility mob1 = station1.getMobility();
				JE802Mobility mob2 = station2.getMobility();
				JETime time = new JETime();
				Vector3d src = new Vector3d(mob1.getXLocation(), mob1.getYLocation(time), mob1.getZLocation(time));
				Vector3d dst = new Vector3d(mob2.getXLocation(time), mob2.getYLocation(time), mob2.getZLocation(time));

				Element style = createColoredLine(color);
				Element line = createLine(src, dst);
				Element placemark = doc.createElement("Placemark");
				placemark.appendChild(line);
				placemark.appendChild(style);
				Element stationFolder = createFolder(placemark, station1.getMacAddress() + " to " + station2.getMacAddress(),
						true);
				linkList.add(stationFolder);
			}
		}
		return linkList;
	}

	private Element createColoredLine(Color color) {
		Element style = doc.createElement("Style");
		Element lineStyle = doc.createElement("LineStyle");
		style.appendChild(lineStyle);
		Element colorElem = createColorElement(color);
		lineStyle.appendChild(colorElem);
		Element with = doc.createElement("width");
		with.appendChild(doc.createTextNode("3"));
		lineStyle.appendChild(with);
		return style;
	}

	@Override
	public Element createDOM() {
		List<Element> linkFolders = createStaticLinks(this.linkColor);
		Element coloredLinksFolder = createFolder(linkFolders, "Links " + colorName, false);
		return coloredLinksFolder;
	}
}
