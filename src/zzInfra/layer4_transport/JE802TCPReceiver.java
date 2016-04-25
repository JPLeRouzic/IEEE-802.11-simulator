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

package zzInfra.layer4_transport;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import IEEE11ac.layer2.acHopInfo;

public class JE802TCPReceiver extends JEEventHandler {

	// sequence number of last received packet
	private long lastReceived;

	// window on the receiver side
	private long[] receiverWindow;

	private JETime now;

	private final int ipHandlerId;

	private final int bulkAcks;

	private final int ackAC;

	// tcp header bytes
	private final int tcpHeaderSize = 20;

	public JE802TCPReceiver(JEEventScheduler aScheduler, Random aGenerator, int aMacHandlerId, int bufferSize, int bulkAcks,
			int ackAC) {
		super(aScheduler, aGenerator);
		this.ipHandlerId = aMacHandlerId;
		this.receiverWindow = new long[bufferSize];
		this.bulkAcks = bulkAcks;
		this.ackAC = ackAC;
	}

	@Override
	public void event_handler(JEEvent anEvent) {
		String anEventName = anEvent.getName();
		this.now = anEvent.getScheduledTime();
		if (anEventName.equals("TCPPacketReceived")) {
			tcpPacketReceived(anEvent);
		} else if (anEventName.equals("hop_evaluation")) {
			// do nothing, we do not evaluate acks
		} else {
			this.error("undefined event '" + anEventName + "' in state " + this.theState.toString());
		}
	}

	private void tcpPacketReceived(JEEvent anEvent) {
		JE802TCPPacket packet = (JE802TCPPacket) anEvent.getParameterList().get(0);
		long seqNo = packet.getSeqNo();
		int ac = (Integer) anEvent.getParameterList().get(2);

		this.message(("received " + seqNo), 30);
		if (seqNo < lastReceived + receiverWindow.length && seqNo > lastReceived) {
			receiverWindow[(int) seqNo % receiverWindow.length] = packet.getSeqNo();
		} else {
			receiverWindow[(int) seqNo % receiverWindow.length] = -1;
		}

		// search for next contiguous arrived sequence in buffer
		int i = (int) (lastReceived + 1) % receiverWindow.length;
		// search for seqNo should be acked next, cumulative ack
		while (receiverWindow[i] != -1) {
			if (lastReceived == 0) {
				receiverWindow[0] = -1;
			}
			lastReceived = receiverWindow[i];
			receiverWindow[i] = -1;
			i++;
			// buffer wrap around
			if (i == receiverWindow.length) {
				i = 0;
			}
		}
		// when sending an ack, we don't set a timeout for it, if it doesn't
		// arrive: bad luck
		if (seqNo % bulkAcks == 0 || seqNo < (lastReceived - receiverWindow.length)) {
			this.message("Sent ack for " + lastReceived, 30);
			JE802TCPPacket ack = new JE802TCPPacket(lastReceived, tcpHeaderSize, packet.getPort(), this.now, true, true, 0, null);
			this.parameterlist = new Vector<Object>();
			this.parameterlist.add(packet.getBackRoute().get(0)); // Destination
			if (ackAC == 0) {
				this.parameterlist.add(ac);
			} else {
				this.parameterlist.add(this.ackAC);
			}
			this.parameterlist.add(new ArrayList<acHopInfo>(packet.getBackRoute()));
			this.parameterlist.add(ack);
			this.send(new JEEvent("TCP_Deliv_req", ipHandlerId, now, this.parameterlist));
		}
	}
}
