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

import zzInfra.util.Vector3d;

public class JE802StationModelKml extends JE802KmlGenerator {

	private final String antennaModelName = "Antenna.dae";

	private final String smallAntennaModelName = "AntennaSmall.dae";

	private final String userModelName = "user.dae";

	public JE802StationModelKml(final Document doc, final List<JE802Station> stations, final String filename) {
		super(doc, stations);
	}

	/**
	 * creates KML model element for a station. Type of model depends on the
	 * fact whether a station is mobile or not
	 * 
	 * @return KML model element of a station
	 */
	private List<Element> createStationModels() {
		ArrayList<Element> models = new ArrayList<Element>();
		for (JE802Station station : this.theStations) {
			int positionCount;
			JETime currentTime = station.getStatEval().getEvaluationStarttime();
			JETime interval = station.getStatEval().getEvaluationInterval();
			if (station.isMobile()) {
				double end = station.getStatEval().getEvaluationEnd().getTimeMs();
				positionCount = (int) ((end - currentTime.getTimeMs()) / interval.getTimeMs());
			} else {
				positionCount = 1;
			}
			Double headingRotation = 0.0; // Math.random()*360.0;
			for (int i = 0; i < positionCount; i++) {

				Vector3d currentPosition = new Vector3d(station.getXLocation(currentTime), station.getYLocation(currentTime),
						station.getZLocation(currentTime));
				headingRotation = -station.getMobility().getTraceHeading(currentTime);

				// Location
				Element altitudeMode = this.doc.createElement("altitudeMode");
				altitudeMode.appendChild(this.doc.createTextNode("relativeToGround"));

				Element location = this.doc.createElement("Location");
				Element longitude = this.doc.createElement("longitude");
				longitude.appendChild(this.doc.createTextNode(Double.toString(currentPosition.getLon())));
				Element latitude = this.doc.createElement("latitude");
				latitude.appendChild(this.doc.createTextNode(Double.toString(currentPosition.getLat())));
				Element altitude = this.doc.createElement("altitude");
				if (station.isMobile()) {
					altitude.appendChild(this.doc.createTextNode(Double.toString(0)));
				} else {
					altitude.appendChild(this.doc.createTextNode(Double.toString(currentPosition.getAlt())));
				}

				location.appendChild(longitude);
				location.appendChild(latitude);
				location.appendChild(altitude);

				// scale
				boolean isMobile = station.isMobile();
				Element scale = this.doc.createElement("Scale");
				Element x = this.doc.createElement("x");
				String scaleFactor = null;
				if (isMobile) {
					scaleFactor = "1";
				} else {
					scaleFactor = "2";
				}

				x.appendChild(this.doc.createTextNode(scaleFactor));
				Element y = this.doc.createElement("y");
				y.appendChild(this.doc.createTextNode(scaleFactor));
				Element z = this.doc.createElement("z");
				z.appendChild(this.doc.createTextNode(scaleFactor));
				scale.appendChild(x);
				scale.appendChild(y);
				scale.appendChild(z);

				// orientation
				Element orientation = this.doc.createElement("Orientation");
				Element heading = this.doc.createElement("heading");
				heading.appendChild(this.doc.createTextNode(headingRotation.toString()));
				Element tilt = this.doc.createElement("tilt");
				tilt.appendChild(this.doc.createTextNode("0"));
				Element roll = this.doc.createElement("roll");
				roll.appendChild(this.doc.createTextNode("0"));
				orientation.appendChild(heading);
				orientation.appendChild(tilt);
				orientation.appendChild(roll);

				// modelfile
				Element link = this.doc.createElement("Link");
				Element href = this.doc.createElement("href");

				String path = "animation_files/";
				if (!isMobile) {
					if (currentPosition.getAlt() > 0.5) {
						path = path + this.smallAntennaModelName;
					} else {
						path = path + this.antennaModelName;
					}
				} else {
					path = path + this.userModelName;
				}
				href.appendChild(this.doc.createTextNode(path));
				link.appendChild(href);

				// assemble model
				Element model = this.doc.createElement("Model");
				model.appendChild(altitudeMode);
				model.appendChild(location);
				model.appendChild(scale);
				model.appendChild(orientation);
				model.appendChild(link);

				// placemark wrapping the model tag
				Element placemark = this.doc.createElement("Placemark");
				Element name = this.doc.createElement("name");
				Element timeSpan;
				if (station.isMobile()) {
					timeSpan = createTimeSpan(currentTime, currentTime.plus(interval));
				} else {
					timeSpan = createTimeSpan(station.getStatEval().getEvaluationStarttime(), station.getStatEval()
							.getEvaluationEnd());
				}
				currentTime = currentTime.plus(interval);
				name.appendChild(this.doc.createTextNode("Station " + station.getMac().getMacAddress()));
				placemark.appendChild(name);
				placemark.appendChild(model);
				placemark.appendChild(timeSpan);
				models.add(placemark);
			}
		}

		return models;
	}

	@Override
	public Element createDOM() {
		List<Element> models = createStationModels();
		String folderName = "802 Stations";
		Element modelFolder = createFolder(models, folderName, false);
		return modelFolder;
	}
}
