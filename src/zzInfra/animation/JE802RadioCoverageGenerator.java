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
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.ARC.JE802Station;
import zzInfra.kernel.JETime;


public class JE802RadioCoverageGenerator extends JE802KmlGenerator {

	private final String imageFilename;

	private final double areaSize;

	protected final boolean showMobile;

	private final String name;

	private final double reuseDistance;
 
	private final double attenuationFactor;

	private final boolean isVisible;

	public JE802RadioCoverageGenerator(Document doc, List<JE802Station> stations, String imageFileName, String name, double size,
			boolean showMobile, double reuseDistance, double attenuationFactor, boolean visible) {
		super(doc, stations);
		this.imageFilename = imageFileName;
		this.showMobile = showMobile;
		this.name = name;
		areaSize = size;
		this.reuseDistance = reuseDistance;
		this.attenuationFactor = attenuationFactor;
		this.isVisible = visible;
	}

	protected double[] getBounds() {
		Double latMax = Double.NEGATIVE_INFINITY;
		Double latMin = Double.POSITIVE_INFINITY;
		Double longMax = Double.NEGATIVE_INFINITY;
		Double longMin = Double.POSITIVE_INFINITY;
		for (JE802Station station : this.theStations) {
			double[] position = convertXYZtoLatLonAlt(station.getXLocation(new JETime()), station.getYLocation(new JETime()),
					station.getZLocation(new JETime()));
			double latHigh = position[0] + meters2DegreesLatitude(this.reuseDistance);
			if (latHigh > latMax) {
				latMax = latHigh;
			}
			double latLow = position[0] - meters2DegreesLatitude(this.reuseDistance);
			if (latLow < latMin) {
				latMin = latLow;
			}
			double lonHigh = position[1] + meters2DegreesLongitude(this.reuseDistance, position[0]);
			if (lonHigh > longMax) {
				longMax = lonHigh;
			}
			double lonLow = position[1] - meters2DegreesLongitude(this.reuseDistance, position[0]);
			if (lonLow < longMin) {
				longMin = lonLow;
			}
		}
		double result[] = { latMax, latMin, longMax, longMin };
		return result;
	}

	protected double calculatePower_dBm(double dBm, double distance) {
		// dBm to mWatt
		double mWatt = Math.pow(10.0, dBm / 10.0);
		double factor = 1.0;
		// attenuation over distance
		double co = Math.pow(distance, attenuationFactor);
		if (co > 1) {
			factor = 1 / (co);
		}
		mWatt = mWatt * factor;
		// mWatt to dBm
		double power = 10.0 * Math.log10(mWatt);
		return dBm - power;
	}

	protected Element createOverlay() {
		double bounds[] = getBounds();
		Double latMax = bounds[0];
		Double latMin = bounds[1];
		Double longMax = bounds[2];
		Double longMin = bounds[3];
		return createOverlay(latMax, latMin, longMax, longMin);
	}

	private Element createOverlay(Double latMax, Double latMin, Double longMax, Double longMin) {
		Element north = doc.createElement("north");
		north.appendChild(doc.createTextNode(latMax.toString()));

		Element south = doc.createElement("south");
		south.appendChild(doc.createTextNode(latMin.toString()));

		Element east = doc.createElement("east");
		east.appendChild(doc.createTextNode(longMax.toString()));

		Element west = doc.createElement("west");
		west.appendChild(doc.createTextNode(longMin.toString()));

		Element box = doc.createElement("LatLonBox");
		box.appendChild(north);
		box.appendChild(south);
		box.appendChild(east);
		box.appendChild(west);

		Element icon = doc.createElement("Icon");
		Element href = doc.createElement("href");
		href.appendChild(doc.createTextNode(imageFilename));
		icon.appendChild(href);
		Element overlay = doc.createElement("GroundOverlay");
		overlay.appendChild(box);
		overlay.appendChild(icon);
		return overlay;
	}

	protected void writeImage(Color data[][]) {
		int width = data.length;
		int height = data[0].length;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		WritableRaster wr = image.getRaster();
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Color col = data[i][j];
				int pixel[] = { col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha() };
				wr.setPixel(i, j, pixel);
			}
		}

		try {
			ImageIO.write(image, "PNG", new File(imageFilename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Element createDOM() {
		double[] bounds = getBounds();
		Double latMax = bounds[0] + meters2DegreesLatitude(areaSize / 2);
		Double latMin = bounds[1] - meters2DegreesLatitude(areaSize / 2);
		Double longMax = bounds[2] + meters2DegreesLongitude(areaSize / 2, bounds[0]);
		Double longMin = bounds[3] - meters2DegreesLongitude(areaSize / 2, bounds[0]);
		Element overlay = createOverlay(latMax, latMin, longMax, longMin);
		Element overlayFolder = createFolder(overlay, name, !this.isVisible);
		return overlayFolder;
	}
}
