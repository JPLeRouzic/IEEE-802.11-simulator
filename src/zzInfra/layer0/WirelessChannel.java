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

package zzInfra.layer0;

import zzInfra.kernel.JEmula;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Stefan
 * 
 */
public final class WirelessChannel extends JEmula implements Comparable<WirelessChannel> {

	private double dotCenterFreq_MHz;

	private double dotFreqBandWidth_MHz;

	private double dotMaximumTransmitPowerLevel_dBm;

	private int aChannelNumber;

	public WirelessChannel(Node aTopLevelNode) {
		Element channelElem = (Element) aTopLevelNode;
		if (channelElem.getNodeName().equals("aChannel")) {
			this.message("XML definition " + channelElem.getNodeName() + " found.", 1);
			this.aChannelNumber = new Integer(channelElem.getAttribute("aChannelNumber"));
			this.dotCenterFreq_MHz = new Double(channelElem.getAttribute("dot11CenterFreq_MHz"));
			this.dotFreqBandWidth_MHz = new Double(channelElem.getAttribute("dot11FreqBandWidth_MHz"));
			this.dotMaximumTransmitPowerLevel_dBm = new Double(channelElem.getAttribute("dot11MaximumTransmitPowerLevel_dBm"));
			this.setDebuglevel(0);
		} else {
			this.error("XML definition " + aTopLevelNode.getNodeName() + " found, but aChannel expected!");
		}
	}

	public int getChannelNumber() {
		return aChannelNumber;
	}

	public double getDot11CenterFreq_MHz() {
		return dotCenterFreq_MHz;
	}

	public double getDot11FreqBandWidth_MHz() {
		return dotFreqBandWidth_MHz;
	}

	public double getDot11MaximumTransmitPowerLevel_dBm() {
		return dotMaximumTransmitPowerLevel_dBm;
	}

	@Override
	public String toString() {
		return "JEWirelessChannel: " + this.getChannelNumber();
	}

	@Override
	public int compareTo(WirelessChannel o) {
		if (this.aChannelNumber < o.getChannelNumber()) {
			return -1;
		} else if (this.aChannelNumber > o.getChannelNumber()) {
			return 1;
		} else {
			return 0;
		}
	}
}
