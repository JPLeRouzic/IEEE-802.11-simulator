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
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import zzInfra.emulator.JE802StatEval;

import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.kernel.JETimer;
import IEEE11ac.layer2.acHopInfo;
import zzInfra.layer3_network.JE802RouteManager;

public class JE802TCPSender extends JEEventHandler {

	// retransmission timeout
	private JETime rto = new JETime(1000.0);

	// contention window size
	private int cwSize;

	// index of contention window start in buffer
	private int cwMin = 0;

	private long seqNo = 0;

	// as in rfc 2581, number of outstanding packets in network
	private int flightSize = 0;

	// counter for
	private int avoidanceCount = 0;

	// seqNo of last Ack
	private long lastAcked = -1;

	// count of duplicate Acks for same seqNo
	private int lastAckedCount = 0;

	// round trip time variance
	private double rttVar = 0.0;

	// smoothed round trip time, calculated as in rfc 2988
	private double smoothedRTT = 0.0;

	// threshold for slowStart
	private int slowStartThreshold = 10;

	// indicator for a full buffer
	private boolean full = false;

	// in avoidance mode or slow start?
	private boolean inAvoidance = false;

	private final JE802TCPBufferRecord[] buffer;

	// coefficient for estimating Rtt
	private final double alpha = 1.0 / 4.0;

	// coefficient for estimating Rtt
	private final double beta = 1.0 / 8.0;

	// minimum timeout in ms
	private double minimumTimeout;

	// tcp header bytes
	private final int tcpHeaderSize = 20;

	// number of acknowledgments needed until fast retransmit is initiated
	private final int fastRetransmitAcks = 3;

	private JETime now;

	// handlerId of traffic gen
	private final int trafficHandlerId;

	private final JE802RouteManager router;

	private long lost = 0;

	private final int bulkAcks;

	private final int port;

	private final JE802StatEval statEval;

	private final int stationAddr;

	public JE802TCPSender(JEEventScheduler aScheduler, Random aGenerator, JE802RouteManager aRouter, int bufferSize,
			int aHandlerId, int port, int slowStartThreshold, double minimumTimeout, int bulkAcks, JE802StatEval statEval,
			int stationAddr) {
		super(aScheduler, aGenerator);
		buffer = new JE802TCPBufferRecord[bufferSize];
		this.router = aRouter;
		this.trafficHandlerId = aHandlerId;
		this.port = port;
		this.bulkAcks = bulkAcks;
		this.cwSize = bulkAcks;
		this.minimumTimeout = minimumTimeout;
		this.slowStartThreshold = slowStartThreshold;
		this.statEval = statEval;
		this.stationAddr = stationAddr;
	}

	@Override
	public void event_handler(JEEvent anEvent) {
		String anEventName = anEvent.getName();
		this.now = anEvent.getScheduledTime();
		this.parameterlist = anEvent.getParameterList();
		if (anEventName.equals("TCP_Deliv_req")) {
			deliveryRequest(anEvent);
		} else if (anEventName.equals("TCP_ACK")) {
			receiveAck(anEvent);
		} else if (anEventName.equals("packet_timeout")) {
			packetTimeout(anEvent);
		} else {
			this.error("undefined event '" + anEventName + "' in state " + this.theState.toString());
		}
	}

	private static int f = 0;

	@SuppressWarnings("unchecked")
	private void deliveryRequest(JEEvent anEvent) {
		int size = (Integer) anEvent.getParameterList().get(0);
		int AC = (Integer) anEvent.getParameterList().get(4);
		ArrayList<acHopInfo> hopAddresses = (ArrayList<acHopInfo>) anEvent.getParameterList().get(5);
		int packetPort = (Integer) anEvent.getParameterList().get(6);

		this.message(("Delivery Request for " + seqNo + ", Port: " + packetPort), 10);
		if (!full) {
			ArrayList<acHopInfo> returnRoute = new ArrayList<acHopInfo>(hopAddresses);
			Collections.reverse(returnRoute); // reverse Route, to be replaced
												// when routing is implemented
			int channel = returnRoute.remove(0).getChannel(); // remove first
																// station
																// because its
																// your own
																// address after
																// reversing
			// add yourself at end of reversed list, such that you are the final
			// destination of the reversed route
			returnRoute.add(new acHopInfo(this.router.getAddress(), channel));

			int lastChannel = channel;
			for (int i = 0; i < returnRoute.size(); i++) {
				acHopInfo hop = returnRoute.remove(i);
				channel = hop.getChannel();
				hop = new acHopInfo(hop.getAddress(), lastChannel);
				returnRoute.add(i, hop);
				lastChannel = channel;
			}

			JE802TCPPacket packet = new JE802TCPPacket(seqNo, size + tcpHeaderSize, packetPort, now, true, false,
					this.getHandlerId(), returnRoute);
			this.parameterlist.clear();
			this.parameterlist.add(seqNo);

			JEEvent timeOutEvent = new JEEvent("packet_timeout", this.getHandlerId(), null, this.parameterlist);
			JETimer timeOut = new JETimer(theUniqueEventScheduler, null, timeOutEvent, this.getHandlerId());

			JE802TCPBufferRecord record = new JE802TCPBufferRecord(timeOut, packet, AC, hopAddresses);
			if (buffer[(int) seqNo % buffer.length] != null) {
				f++;
				this.error("F" + f);
			}
			buffer[(int) seqNo % buffer.length] = record;

			// only transmit if seqNo is in contention window
			if (seqNo % buffer.length >= cwMin
					|| (seqNo % buffer.length < (cwMin + cwSize) % buffer.length || seqNo % buffer.length < cwMin + cwSize)) {
				flightSize++;
				transmit(record);
				record.setSent(true);
				this.message(("Handed " + seqNo + " to Mac, Port: " + packetPort + " TO: " + rto), 10);
			}
			seqNo++;
			// check if buffer is full
			if (isBufferFull()) {
				full = true;
				this.send(new JEEvent("TCPBufferFull", trafficHandlerId, now));
			} else {
				this.send(new JEEvent("TCPBufferReady", trafficHandlerId, now));
				full = false;
			}
		} else {
			this.error("Buffer full");
		}
	}

	private void receiveAck(JEEvent anEvent) {
		JE802TCPPacket ack = (JE802TCPPacket) anEvent.getParameterList().get(0);
		long sequenceNo = ack.getSeqNo();
		this.message("Ack for " + sequenceNo + "received, Port: " + ack.getPort(), 10);
		if (sequenceNo == lastAcked) { // we received a duplicate Ack, packet
										// was lost
			this.message("Duplicate ACK for " + sequenceNo + "Count: " + lastAckedCount + " Port " + ack.getPort(), 10);
			lastAckedCount++;
			flightSize--;
			if (buffer[(int) ((sequenceNo + 1) % buffer.length)] != null) {
				buffer[(int) ((sequenceNo + 1) % buffer.length)].setSent(false);
			}

			// fast retransmit
			if (lastAckedCount >= fastRetransmitAcks && lastAckedCount % fastRetransmitAcks == 0) {
				fastRetransmit();
			}
			lost++;
			// this.message("Lost " + lost + " Lossrate " +
			// (double)lost/lastAcked*100 + "%",55);

			// packet delivered successfully, notify traffic gen
		} else if (sequenceNo > lastAcked) {
			int cumulCount = (int) (sequenceNo - lastAcked - 1); // cumulative
																	// acks
			flightSize -= cumulCount;
			// fast recovery, when cumulative Acks arrive, cwSize is increased
			// by number of cumulative Acks
			if (lastAckedCount > 0) {
				cwSize = cwSize + cumulCount;
			}
			if (cumulCount > 1 && lastAckedCount > 0) {
				lost -= cumulCount;
			}

			// contention window shall never be larger than buffer
			if (cwSize >= buffer.length) {
				cwSize = buffer.length - 1;
			}

			for (int i = 0; i < buffer.length; i++) {
				// on arrival of cumulative ack, inform higher layer of
				// successful delivery of all packets which where successfully
				// delivered
				JE802TCPBufferRecord currentRecord = buffer[i % buffer.length];
				if (currentRecord != null && currentRecord.getPacket().getSeqNo() <= sequenceNo) {
					currentRecord.packetDelivered();
					statEval.tcpPacketAcked(stationAddr, port);
					JE802TCPPacket packet = currentRecord.getPacket();
					double rtt = theUniqueEventScheduler.now().getTimeMs() - packet.getCreationTime().getTimeMs();
					adjustSmoothedRTT(rtt);
					this.parameterlist.clear();
					this.parameterlist.add(packet);
					this.send(new JEEvent("packet_exiting_system_ind", trafficHandlerId, now, this.parameterlist));
					this.message("exiting " + packet.getSeqNo(), 10);
					buffer[i % buffer.length] = null;
				}
			}
			lastAcked = sequenceNo;
			lastAckedCount = 0;
			cwMin = (int) (sequenceNo + 1) % buffer.length;
			// adjust contention window
			if (cwSize < slowStartThreshold) {
				// still in Slow Start
				avoidanceCount = 0;
				cwSize++;
				inAvoidance = false;
			} else {
				// in congestion avoidance mode
				avoidanceCount += bulkAcks;
				inAvoidance = true;
				if (avoidanceCount >= cwSize) {
					avoidanceCount = 0;
					cwSize++;
				}
			}
			if (cwSize > buffer.length) {
				cwSize = buffer.length;
				this.warning("buffer too small");
			}
			// cwMin = (cwMin+1) % buffer.length; //contention window one higher
			this.message("cwSize: " + cwSize + "avc: " + avoidanceCount + " Port " + ack.getPort(), 10);
			// send more packets
			for (int i = cwMin; i < cwMin + cwSize; i++) {
				JE802TCPBufferRecord currentRecord = buffer[i % buffer.length];
				if (currentRecord != null && !currentRecord.isSent()) {
					flightSize++;
					transmit(currentRecord);
					currentRecord.setSent(true);
					this.message(("Handed " + currentRecord.getPacket().getSeqNo() + " to RoutingLayer, CWmin: " + cwMin
							+ " cwSize:" + cwSize + " Port " + ack.getPort() + " TO: " + rto), 70);
				}
			}
		}
		int nullCount = 0;
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] == null) {
				nullCount++;
			}
		}
		if (nullCount > 0) {
			this.send(new JEEvent("TCPBufferReady", trafficHandlerId, now));
			full = false;
		}
	}

	// when timeout occurs, packet is repeated
	private void packetTimeout(JEEvent anEvent) {
		// TODO: there is a nasty bug, sometimes a wrong parameter list is
		// added, this is just an ugly workaround
		long sequenceNo;
		if (anEvent.getParameterList().size() < 2) {
			sequenceNo = (Long) this.parameterlist.get(0);
		} else {
			sequenceNo = (Integer) this.parameterlist.get(1);
		}
		if (buffer[(int) sequenceNo % buffer.length] != null) {
			acHopInfo DA = buffer[(int) sequenceNo % buffer.length].getHopAddresses().get(0);
			Vector<Object> parameterList = new Vector<Object>();
			parameterList.add(DA);
			// this.send(new JEEvent("TCP_Timeout", this.router.getHandlerId(),
			// now, parameterList));

			if (inAvoidance) {
				slowStartThreshold = Math.max(flightSize / 2, 2 * cwSize);
			}
			for (int i = 0; i < buffer.length; i++) {
				if (buffer[i] != null) {
					buffer[i].setSent(false);
				}
			}
			flightSize = 1;
			cwSize = bulkAcks;
			lost += cwSize;
			this.message("timeout packet" + sequenceNo, 10);
			for (int i = cwMin; i < cwMin + cwSize; i++) {
				if (sequenceNo > lastAcked && sequenceNo % buffer.length >= cwMin
						&& (sequenceNo % buffer.length < (cwMin + cwSize) % buffer.length)
						|| sequenceNo % buffer.length < cwMin + cwSize) {
					JE802TCPBufferRecord record = buffer[i % buffer.length];
					if (record != null) {
						transmit(record);
						this.message(("retransmit packet" + sequenceNo), 10);
					}
				}
			}
		}

	}

	// calculate weighted adjustment of rtt as in rfc 2988
	private void adjustSmoothedRTT(double rtt) {
		if (smoothedRTT == 0.0) {
			smoothedRTT = rtt;
			rttVar = 0.5 * rtt;
			double newRto = rtt + 4 * rttVar;
			if (newRto < minimumTimeout) {
				rto = new JETime(minimumTimeout);
			} else {
				rto = new JETime(newRto);
			}

		} else {
			smoothedRTT = (1 - alpha) * smoothedRTT + alpha * rtt;
			rttVar = (1 - beta) * rttVar + beta * (Math.abs(smoothedRTT - rtt));
			double newRto = smoothedRTT + 4 * rttVar;
			if (newRto < minimumTimeout) {
				rto = new JETime(minimumTimeout);
			} else {
				rto = new JETime(newRto);
			}
		}
		// this.message("RTT: " + smoothedRTT + "ms RttVar: " + rttVar +
		// "ms timeout: " + rto, 10);
	}

	private void transmit(JE802TCPBufferRecord record) {
		statEval.tcpPacketSent(stationAddr, port);
		record.setTimer(rto);
		record.getPacket().setCreationTime(now);// for having accurate rtt
		this.parameterlist.clear();
		this.parameterlist.add(record.getHopAddresses().get(0)); // Destination
		this.parameterlist.add(record.getAC());
		this.parameterlist.add(new ArrayList<acHopInfo>(record.getHopAddresses()));
		this.parameterlist.add(record.getPacket());
		this.send(new JEEvent("TCP_Deliv_req", this.router.getHandlerId(), now, this.parameterlist));
	}

	private void fastRetransmit() {
		cwSize = cwSize / 2;
		if (cwSize < bulkAcks) {
			cwSize = bulkAcks;
		}
		for (int i = (int) (lastAcked + 1) % buffer.length; i < cwMin + cwSize; i++) {
			JE802TCPBufferRecord currentRecord = buffer[i % buffer.length];
			if (currentRecord != null) {
				transmit(currentRecord);
				flightSize++;
				lost++;

				this.message(("Fast retransmit of Packet " + currentRecord.getPacket().getSeqNo() + " Port " + currentRecord
						.getPacket().getPort()), 10);
			}
		}
	}

	@Override
	public String toString() {
		return "TCPSender at port" + port;
	}

	public long getTransmittedPackets() {
		return lastAcked;
	}

	public long getLostPackets() {
		return lost;
	}

	public int getPort() {
		return this.port;
	}

	public boolean isBufferFull() {
		int nullCount = 0;
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] == null) {
				nullCount++;
			}
		}
		return nullCount == 0;
	}

	public boolean isBufferEmpty() {
		int nullCount = 0;
		for (int i = 0; i < buffer.length; i++) {
			if (buffer[i] == null) {
				nullCount++;
			}
		}
		return nullCount == buffer.length;
	}
}
