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
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import zzInfra.kernel.JETime;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import zzInfra.ARC.JE802Station;

public class JE802PowerOverlayKml extends JE802KmlGenerator {

	private final double maxTxdBm;

	private final double minTxdBm;

	private final int alpha = 255;

	private final String pathInKmz;

	private final String powerFileName = "power.png";

	private final String channelFileName = "channels.png";

	private final String addressFileName = "addresses.png";

	// attenuation factor of radio signal
	private final double attenuationFactor;

	// specifies whether mobile stations are taken into account when calculation
	// the power distributions
	private final boolean showMobile;

	// lenght of a pixel in the images in meters
	private final double pixelLength;

	private final double reuseDistance;
	
	private String resultPath;

	public JE802PowerOverlayKml(final Document doc, final List<JE802Station> stations, final String filename,
			final double pixelLength, final boolean showMobile, final double reuseDistance, final double maxTxdBm,
			final double minTxdBm, final double attenuationFactor, String path) {
		super(doc, stations);
		this.pixelLength = pixelLength;
		this.showMobile = showMobile;
		this.reuseDistance = reuseDistance;
		this.pathInKmz = "animation_files/";
		this.minTxdBm = minTxdBm;
		this.maxTxdBm = maxTxdBm;
		this.attenuationFactor = attenuationFactor;
		this.resultPath = path;
	}

	@Override
	public Element createDOM() {
		createPowerImages();
		Element power = createOverlay(this.powerFileName);
		Element channels = createOverlay(this.channelFileName);
		Element address = createOverlay(this.addressFileName);
		Element powerFolder = createFolder(power, "Power", true);
		Element channelFolder = createFolder(channels, "Channels", true);
		Element addressFolder = createFolder(address, "MAC Addresses", true);
		List<Element> powerOverlays = new ArrayList<Element>();
		powerOverlays.add(powerFolder);
		powerOverlays.add(channelFolder);
		powerOverlays.add(addressFolder);
		Element overlayFolder = createFolder(powerOverlays, "Power", false);
		return overlayFolder;
	}

	/**
	 * Creates images for Power, Channels and Mac Addresses This function does
	 * all in one because its faster than calculating the powerlevel of a point
	 * for each image separately.
	 */
	private void createPowerImages() {
		// calculate limits of coverage area
		double bounds[] = getBounds();
		double latMax = bounds[0];
		double latMin = bounds[1];
		double longMax = bounds[2];
		double longMin = bounds[3];

		double latSquareAngle = meters2DegreesLatitude(this.pixelLength);
		double longSquareAngle = meters2DegreesLongitude(this.pixelLength, latMax);
		double latDifference = latMax - latMin;
		double longDifference = longMax - longMin;

		int latPieces = (int) Math.round(latDifference / latSquareAngle);
		int longPieces = (int) Math.round(longDifference / longSquareAngle);

		Color[][] powerPixels = new Color[longPieces][latPieces];
		Color[][] channelPixels = new Color[longPieces][latPieces];
		Color[][] addressPixels = new Color[longPieces][latPieces];
		int maxAddress = maxMacAddress();
		for (int i = latPieces - 1; i >= 0; i--) {
			Double northEdge = latMax - i * latSquareAngle;
			Double southEdge = latMax - (i + 1) * latSquareAngle;
			for (int j = 0; j < longPieces; j++) {
				Double eastEdge = longMax - j * longSquareAngle;
				Double westEdge = longMax - (j + 1) * longSquareAngle;
				double centerLat = (northEdge + southEdge) / 2.0;
				double centerLong = (westEdge + eastEdge) / 2.0;
				double maxPower = Double.NEGATIVE_INFINITY;
				double nearest = Double.MAX_VALUE;
				JE802Station nearestStation = null;
				int size = this.theStations.size();
				double powerSum = 0.0;
				for (int k = 0; k < size; k++) {
					JE802Station station = this.theStations.get(k); // this is
																	// faster
																	// than list
																	// iterator
					JETime startTime = station.getStatEval().getEvaluationStarttime();
					if (this.showMobile || !station.isMobile()) {
						double dBm = station.getTransmitPowerLeveldBm();
						double[] position = convertXYZtoLatLonAlt(station.getXLocation(startTime),
								station.getYLocation(startTime), station.getZLocation(startTime));
						double distance = calculateDistance(centerLat, centerLong, position[0], position[1]);
						double currentPower = calculatePower_mW(dBm, distance);
						powerSum += currentPower;
						if (currentPower > maxPower) {
							maxPower = currentPower;
							nearest = distance;
							nearestStation = this.theStations.get(k);
						}
					}
				}
				// make transparent if not in reuse distance
				Color powerColor;
				Color channelColor;
				Color addressColor;
				if (nearest < this.reuseDistance) {
					powerColor = calculatePowerColor(powerSum, this.minTxdBm, this.maxTxdBm, this.alpha);
					int channel = nearestStation.getFixedChannel();
					channelColor = getChannelColor(channel);
					addressColor = calculateAddressColor(nearestStation.getMacAddress(), maxAddress);
				} else {
					powerColor = new Color(255, true);
					channelColor = new Color(255, true);
					addressColor = new Color(255, true);
				}
				powerPixels[longPieces - j - 1][i] = powerColor;
				channelPixels[longPieces - j - 1][i] = channelColor;
				addressPixels[longPieces - j - 1][i] = addressColor;
			}
		}
		writeImage(powerPixels, this.powerFileName);
		writeImage(channelPixels, this.channelFileName);
		writeImage(addressPixels, this.addressFileName);
	}

	/**
	 * Color gradient is red to yellow to green to cyan to blue to magenta,
	 * looks fancy
	 * 
	 * @param macAddress
	 *            MACAddress of current station
	 * @param maxAddress
	 *            the biggest MACaddress of all stations
	 * @return Color of current stations MACAddress
	 */
	private Color calculateAddressColor(final int macAddress, final int maxAddress) {
		double gradient = (double) macAddress / maxAddress;
		Double r = 0.0;
		Double g = 0.0;
		Double b = 0.0;
		if (gradient >= 0) {
			if (gradient < 0.2) {
				r = 255.0;
				g = gradient * 5.0 * 255.0;
			} else if (gradient < 0.4) {
				g = 255.0;
				r = 255.0 - (gradient - 0.2) * 5.0 * 255.0;
			} else if (gradient < 0.6) {
				g = 255.0;
				b = (gradient - 0.4) * 5.0 * 255.0;
			} else if (gradient < 0.8) {
				b = 255.0;
				g = 255.0 - (gradient - 0.6) * 5.0 * 255.0;
			} else if (gradient <= 1) {
				b = 255.0;
				r = (gradient - 0.8) * 5.0 * 255.0;
			}
		}
		return new Color(r.intValue(), g.intValue(), b.intValue(), this.alpha);
	}

	/**
	 * Returns color according to currentLevel of power in dBm Creates a color
	 * gradient from blue over green to yellow (yellow is highest power)
	 * 
	 * @param currentdBm
	 *            currentPower level in dBm
	 * @param mindBm
	 *            dBm value which is blue
	 * @param maxdBm
	 *            dBm value which is yellow
	 * @param alphaValue
	 *            alpha value of color
	 * @return Color of current power level
	 */
	private Color calculatePowerColor(final double current_mW, final double mindBm, final double maxdBm, final int alphaValue) {
		double current_dBm = 10 * Math.log10(1000 * current_mW);
		double gradient = (current_dBm - mindBm) / (maxdBm - mindBm);
		Double r = 0.0;
		Double g = 0.0;
		Double b = 0.0;

		if (gradient > 0) {
			if (gradient < 1.0 / 3.0) {
				b = 255.0;
				g = gradient * 3.0 * 255.0;
			} else if (gradient < 2.0 / 3.0) {
				g = 255.0;
				b = 255.0 - (gradient - 1.0 / 3.0) * 3.0 * 255.0;
			} else if (gradient < 1) {
				g = 255.0;
				r = (gradient - 2.0 / 3.0) * 3.0 * 255.0;
			} else {
				r = 255.0;
				g = 255.0;
			}
		} else {
			b = 255.0;
		}
		Color color = new Color(r.intValue(), g.intValue(), b.intValue(), alphaValue);
		return color;
	}

	/**
	 * Get bounding box of all stations +- reuseDistance
	 * 
	 * @return bounding coordinates for all stations
	 *         {latitudeMaximum,latitudeMinimum
	 *         ,longitudeMaximum,longitudeMinimuim}
	 */
	private double[] getBounds() {
		Double latMax = Double.NEGATIVE_INFINITY;
		Double latMin = Double.POSITIVE_INFINITY;
		Double longMax = Double.NEGATIVE_INFINITY;
		Double longMin = Double.POSITIVE_INFINITY;
		for (JE802Station station : this.theStations) {
			JETime startTime = station.getStatEval().getEvaluationStarttime();
			double[] position = convertXYZtoLatLonAlt(station.getXLocation(startTime), station.getYLocation(startTime),
					station.getZLocation(startTime));
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

	/**
	 * Gets maximum Mac adress of all stations
	 * 
	 * @return maximum mac address over all stations
	 */
	private int maxMacAddress() {
		int maxAddress = Integer.MIN_VALUE;
		for (JE802Station station : this.theStations) {
			if (station.getMacAddress() > maxAddress) {
				maxAddress = station.getMacAddress();
			}
		}
		return maxAddress;
	}

	/**
	 * Helper function to calculate power
	 * 
	 * @param dBm
	 *            Power level in dBm at current location
	 * @param distance
	 *            distance in meters from current location
	 * @return dBm Power level at distance from current location
	 */
	private double calculatePower_mW(final double dBm, final double distance) {
		// dBm to mWatt
		double watts = Math.pow(10.0, (dBm - 30) / 10.0);
		double factor = 1.0;
		// attenuation over distance
		double co = Math.pow(distance, this.attenuationFactor);
		if (co > 1) {
			factor = 1 / co;
		}
		watts = watts * factor;
		return watts;
	}

	/**
	 * Creates a KML Overlay element with the overlay image specified in
	 * filename
	 * 
	 * @param fileName
	 *            filename of the overlay image
	 * @return KML Overlay element
	 */
	private Element createOverlay(final String fileName) {
		double bounds[] = getBounds();
		Double latMax = bounds[0];
		Double latMin = bounds[1];
		Double longMax = bounds[2];
		Double longMin = bounds[3];
		Element north = this.doc.createElement("north");
		north.appendChild(this.doc.createTextNode(latMax.toString()));

		Element south = this.doc.createElement("south");
		south.appendChild(this.doc.createTextNode(latMin.toString()));

		Element east = this.doc.createElement("east");
		east.appendChild(this.doc.createTextNode(longMax.toString()));

		Element west = this.doc.createElement("west");
		west.appendChild(this.doc.createTextNode(longMin.toString()));

		Element box = this.doc.createElement("LatLonBox");
		box.appendChild(north);
		box.appendChild(south);
		box.appendChild(east);
		box.appendChild(west);

		Element icon = this.doc.createElement("Icon");
		Element href = this.doc.createElement("href");
		href.appendChild(this.doc.createTextNode(this.pathInKmz + fileName));
		icon.appendChild(href);
		Element overlay = this.doc.createElement("GroundOverlay");
		overlay.appendChild(box);
		overlay.appendChild(icon);
		return overlay;
	}

	/**
	 * 
	 * @param data
	 *            Matrix of color values of the image
	 * @param fileName
	 *            filename of the image to be written
	 */
	private void writeImage(final Color data[][], final String fileName) {
		File filesFolder = new File(this.resultPath+"/"+this.pathInKmz);
		if (!filesFolder.exists()) {
			filesFolder.mkdir();
		}
		int width = data.length;

		if (width > 0) {
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
				ImageIO.write(image, "PNG", new File(this.resultPath + "/" +this.pathInKmz + fileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param channel
	 *            number of the wireless channel
	 * @return Color of the according channel
	 */
	private Color getChannelColor(final int channel) {
		switch (channel) {
		case 1:
			return Color.GREEN;
		case 2:
			return Color.BLACK;
		case 3:
			return Color.CYAN;
		case 4:
			return Color.BLUE;
		case 5:
			return Color.YELLOW;
		case 6:
			float[] hsb = Color.RGBtoHSB(200, 240, 180, null);
			return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
		case 7:
			return new Color(0, 255, 255);
		case 8:
			return new Color(128, 0, 0);
		case 9:
			return new Color(0, 128, 0);
		case 10:
			return new Color(0, 0, 128);
		case 11:
			return Color.RED;
		case 12:
			return new Color(128, 0, 128);
		default:
			break;
		}
		return new Color(255, 255, 255);
	}
}