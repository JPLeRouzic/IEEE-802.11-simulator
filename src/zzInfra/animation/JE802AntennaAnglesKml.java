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
import zzInfra.layer1.JEAntenna;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.ARC.JE802Station;
import zzInfra.util.Vector3d;

public class JE802AntennaAnglesKml extends JE802KmlGenerator {

	private final String antennaConeName = "antennaCone.dae";
	private final String antennaSphereName = "sphere.dae";

	public JE802AntennaAnglesKml(final Document doc, final List<JE802Station> stations) {
		super(doc, stations);
	}

	private List<Element> createAntennaModels() {
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

			JEAntenna antenna = station.getPhy().getAntenna();
			String scaleFactorYZ;
			String scaleFactorX;
			String heading, tilt, roll;
			double headingRotation;
			String path;

			for (int i = 0; i < positionCount; i++) {

				headingRotation = -station.getMobility().getTraceHeading(currentTime);

				if (antenna.isDirectional()) {
					Vector3d antennaDir = antenna.getDirection();
					double angleDeg = antenna.getApertureAngle();
					double angle = Math.toRadians(angleDeg);
					double newWidth = Math.tan(angle) * JE802KmlGenerator.antennaConeHeight * 2;
					double yzScaleFactor = newWidth / JE802KmlGenerator.antennaConeHeight; // antennaConeHeight
																							// ==
																							// antennaConeWidth
					double oldConeRadius = Math.sqrt(5) * JE802KmlGenerator.antennaConeHeight / 2;
					double newConeRadius = (newWidth / 2) / Math.sin(angle);
					double overallScaleFactor = oldConeRadius / newConeRadius;
					scaleFactorYZ = String.valueOf(yzScaleFactor * overallScaleFactor);
					scaleFactorX = String.valueOf(overallScaleFactor);

					headingRotation -= antennaDir.getLon();
					if (headingRotation < -360) {
						headingRotation += 360;
					}
					if (headingRotation > 360) {
						headingRotation -= 360;
					}
					heading = new Double(headingRotation).toString();
					tilt = "0";
					roll = new Double(antennaDir.getLat()).toString();
					path = "animation_files/" + antennaConeName;
				} else {
					scaleFactorYZ = "1";
					scaleFactorX = "1";
					heading = "0";
					tilt = "0";
					roll = "0";
					path = "animation_files/" + antennaSphereName;
				}

				Vector3d pos = new Vector3d(station.getXLocation(currentTime), station.getYLocation(currentTime),
						station.getZLocation(currentTime));

				if (station.isMobile()) {
					pos.setAlt(pos.getAlt() + JE802KmlGenerator.userModelHeight);
				} else {
					pos.setAlt(pos.getAlt() + JE802KmlGenerator.antennaModelHeight);
				}

				// Location
				Element altitudeMode = this.doc.createElement("altitudeMode");
				altitudeMode.appendChild(this.doc.createTextNode("relativeToGround"));

				Element location = this.doc.createElement("Location");
				Element longitude = this.doc.createElement("longitude");
				longitude.appendChild(this.doc.createTextNode(Double.toString(pos.getLon())));
				Element latitude = this.doc.createElement("latitude");
				latitude.appendChild(this.doc.createTextNode(Double.toString(pos.getLat())));
				Element altitude = this.doc.createElement("altitude");
				altitude.appendChild(this.doc.createTextNode(Double.toString(pos.getAlt())));

				location.appendChild(longitude);
				location.appendChild(latitude);
				location.appendChild(altitude);

				Element scale = this.doc.createElement("Scale");
				Element x = this.doc.createElement("x");
				x.appendChild(this.doc.createTextNode(scaleFactorX));
				Element y = this.doc.createElement("y");
				y.appendChild(this.doc.createTextNode(scaleFactorYZ));
				Element z = this.doc.createElement("z");
				z.appendChild(this.doc.createTextNode(scaleFactorYZ));
				scale.appendChild(x);
				scale.appendChild(y);
				scale.appendChild(z);

				// orientation
				Element orientation = this.doc.createElement("Orientation");
				Element headingEl = this.doc.createElement("heading");
				headingEl.appendChild(this.doc.createTextNode(heading));
				Element tiltEl = this.doc.createElement("tilt");
				tiltEl.appendChild(this.doc.createTextNode(tilt));
				Element rollEl = this.doc.createElement("roll");
				rollEl.appendChild(this.doc.createTextNode(roll));
				orientation.appendChild(headingEl);
				orientation.appendChild(tiltEl);
				orientation.appendChild(rollEl);

				// modelfile
				Element link = this.doc.createElement("Link");
				Element href = this.doc.createElement("href");

				// old animation files path
				// String path = this.filename + ".kmz/animation_files/";

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
				name.appendChild(this.doc.createTextNode("Station " + station.getMacAddress()));
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
		List<Element> models = createAntennaModels();
		String folderName = "Antenna Angles";
		Element modelFolder = createFolder(models, folderName, false);
		return modelFolder;
	}
}
