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

package IEEE11ax.layer2;

import java.util.ArrayList;

import zzInfra.kernel.JETime;
import zzInfra.layer2.JE802Mpdu;
import zzInfra.layer3_network.JE802IPPacket;

public final class axMPDU extends JE802Mpdu {

	private ArrayList<axHopInfo> hopAddresses;

	private JETime theNAV;

	private final JE802IPPacket payload;

	private int theAC;

	private JE80211MpduType type;

	public enum JE80211MpduType {
		data, rts, cts, ack
	}

	public axMPDU(final int aSourceAddress, final int aDestAddress, final JE80211MpduType type, final long aSequenceNum,
			final int aFrameBodySize, final int aHeaderSize, final int anAC, final int aSourceHandler,
			final ArrayList<axHopInfo> hops, final JETime lastArrival, final JE802IPPacket payload) {
		// the generator that generated the payload
		super(aSourceAddress, aDestAddress, aSequenceNum, aFrameBodySize, aHeaderSize, aSourceHandler, lastArrival);
		this.theAC = anAC; // access category
		this.hopAddresses = hops;
		this.theNAV = new JETime(); // NAV value in unit of ms, default is 0
		this.type = type;
		this.payload = payload;
	}

	public axMPDU() {
		super();
		this.theAC = 0; // access category
		this.hopAddresses = new ArrayList<axHopInfo>();
		this.type = JE80211MpduType.data;
		this.theNAV = new JETime(); // NAV value in unit of ms, default is 0
		this.payload = null;
	}

	public void setAC(final Integer theAC) {
		this.theAC = theAC;
	}

	public void setNAV(final JETime theNAV) {
		this.theNAV = theNAV;
	}

	public JE80211MpduType getType() {
		return this.type;
	}

	public void setType(JE80211MpduType type) {
		this.type = type;
	}

	public JETime getNav() {
		return this.theNAV;
	}

	public ArrayList<axHopInfo> getHopAddresses() {
		return this.hopAddresses;
	}

	public int getAC() {
		return this.theAC;
	}

	public boolean isData() {
		return type == JE80211MpduType.data;
	}

	public boolean isRts() {
		return type == JE80211MpduType.rts;
	}

	public boolean isCts() {
		return type == JE80211MpduType.cts;
	}

	public boolean isAck() {
		return type == JE80211MpduType.ack;
	}

	public JE802IPPacket getPayload() {
		return this.payload;
	}

	@Override
	public void display_status() {

		System.out.println("=========== JEmula object (" + this.getClass().toString() + ") ==========");

		System.out.println("  - Type:            " + this.type);
		System.out.println("  - NAV:             " + this.theNAV.toString());
		super.display_status();

		System.out.println("=======================================================");
	}

	@Override
	public axMPDU clone() {
		axMPDU theCopy = new axMPDU(theSA, theDA, type, theSeqNo, theFrameBodySize, theHeaderSize, theAC,
				theSourceHandler, hopAddresses, lastArrivalTime, payload);
		theCopy.theTxTime = theTxTime;
		theCopy.thePhyMCS = thePhyMCS;
		theCopy.error = error;
		theCopy.theNAV = theNAV;
		return theCopy;
	}
}
