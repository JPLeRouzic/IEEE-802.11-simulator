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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import zzInfra.emulator.JE802StatEval;

import zzInfra.kernel.JEEvent;
import zzInfra.kernel.JEEventHandler;
import zzInfra.kernel.JEEventScheduler;
import zzInfra.kernel.JETime;
import zzInfra.layer3_network.JE802IPPacket;
import zzInfra.layer3_network.JE802RouteManager;

/**
 * 
 * @author fdreier This implementation of TCP only handles congestion control.
 *         Other mechanisms as flow control or connection management are not
 *         implemented. The implemented version of TCP is TCP-RENO. The four
 *         implemented algorithms for flow control are: slow start, congestion
 *         avoidance, fast retransmit and fast recovery.<br>
 *         Slow start:<br>
 *         The algorithm starts with a contention window of 1. For each
 *         acknowledged packet, the contention window is increased by 1. This
 *         leads to an exponential grow of the contention window size. The slow
 *         start phase ends when one of the following two conditions is met: <br>
 *         a) The contention window size exceeds the slow start threshold value. <br>
 *         b) One of the packets times out. In this case, the contention window
 *         size is reset to 1<br>
 *         Congestion Avoidance:<br>
 *         When the congestion window size exceeds the slow start threshold, TCP
 *         switches into congestion avoidance mode. The contention window size
 *         is only increased by 1 when every packet of the current contention
 *         window was transmitted successfully. When a timeout of a packet
 *         occurs. The contention window size is reset to 1 and the slow start
 *         threshold is reduced to half of its value.<br>
 *         Fast-Retransmit: <br>
 *         If a sender receives duplicate acknowledgments (acknowledgments for
 *         the same sequence number) this is an indicator for a lost packet. It
 *         also indicates that some packets after the lost packet were received
 *         correctly, since the receiver sent the acknowledgment several times.
 *         After 3 duplicate Acks are received, fast-retransmit sends the lost
 *         packets and the packets after that directly without waiting for their
 *         timeout. When a fast-retransmit occurs, the contention window size is
 *         just reduced to half of its size and not reset to 1 as in the timeout
 *         case.<br>
 *         Fast-Recovery: <br>
 *         When a packet was lost but the following packets arrived
 *         successfully, the receiver can already buffer the successfully
 *         received packets and wait fort the retransmission of the lost packet.
 *         When the lost packet is received, then the receiver acknowledges all
 *         successfully received packets at one by sending an acknowledgment
 *         with the sequence number of the last successfully received
 *         packet(cumulative acknowledgment). On the sender side, when receiving
 *         such a cumulative ack, the contention window size is increased by the
 *         number of duplicate acks. This is called fast-recovery.<br>
 * 
 *         Estimation of round trip time is done according to rfc 2988. The
 *         algorithm uses a estimation of the round trip time based on the
 *         weighted average of the last few successfully transmitted packets.
 *         The timeout is set to to = rtt + 4*rttVar, where rttVar is variance
 *         of the estimated round trip time.
 * 
 * 
 * 
 * */

public class JE802TCPManager extends JEEventHandler {

	private final JE802RouteManager router;

	// maps ports to traficgens
	private final Map<Integer, Integer> portMap;

	// maps ports to sender instances
	private final Map<Integer, JE802TCPSender> senderMap;

	// maps ports to receiver instances
	private final Map<Integer, JE802TCPReceiver> receiverMap;

	private JETime now;

	private final int bulkAcks;

	private final int ackAC;

	private long nonTcpSeqNo;

	// buffer size in packets
	// optimal buffer size would be (bandwidth*rtt)/packetsize
	// IMPORTANT: Backoff entity queue size should be at least as big as this
	// buffersize
	private final int bufferSize;

	// list of trafficgen handler Id's for each AC
	private Vector<ArrayList<Integer>> acTrafficGens = new Vector<ArrayList<Integer>>(4);

	private final int slowStartThreshold;

	private final double minimumTimeout;

	private JE802StatEval statEval;

	public JE802TCPManager(JEEventScheduler aScheduler, Random aGenerator, Node tcpNode, JE802RouteManager aRouter,
			JE802StatEval statEval) {
		super(aScheduler, aGenerator);
		router = aRouter;

		if (tcpNode != null) {
			this.statEval = statEval;
			Element tcpElem = (Element) tcpNode;
			String buffer = tcpElem.getAttribute("bufferSizePackets");
			if (buffer.equals("")) {
				this.warning("No bufferSize attribute specified in xml at station " + router.getAddress());
				this.bufferSize = 10;
			} else {
				this.bufferSize = new Integer(buffer);
			}
			String slowStart = tcpElem.getAttribute("slowStartThreshold");
			if (slowStart.equals("")) {
				this.warning("No slowStartThreshold attribute specified in xml at station " + router.getAddress());
				this.slowStartThreshold = 10;
			} else {
				this.slowStartThreshold = new Integer(slowStart);
			}
			String timeOut = tcpElem.getAttribute("minimumTimeoutMs");
			if (timeOut.equals("")) {
				this.warning("No minimumTimeout attribute specified in xml at station " + router.getAddress());
				this.minimumTimeout = 1000;
			} else {
				this.minimumTimeout = new Double(timeOut);
			}
			String b = tcpElem.getAttribute("b");
			if (b.equals("")) {
				this.warning("No b (bulkAcks) attribute specified in xml at station " + router.getAddress());
				this.bulkAcks = 1;
			} else {
				this.bulkAcks = new Integer(b);
			}
			String ackACString = tcpElem.getAttribute("ackAC");
			if (ackACString.equals("")) {
				// if nothing is specified, 0 means that ACK packets are sent
				// with the same AC as the incoming packets
				this.ackAC = 0;
			} else {
				this.ackAC = new Integer(ackACString);
			}
		} else {
			// default value s
			this.bulkAcks = 1;
			this.bufferSize = 10;
			this.slowStartThreshold = 10;
			this.minimumTimeout = 1000;
			this.ackAC = 0;
			this.warning("No TCP element specified in xml at station " + router.getAddress() + ", using default values");
		}
		this.portMap = new HashMap<Integer, Integer>();
		acTrafficGens.setSize(4);
		for (int i = 0; i < acTrafficGens.capacity(); i++) {
			acTrafficGens.set(i, new ArrayList<Integer>());
		}
		this.senderMap = new HashMap<Integer, JE802TCPSender>();
		this.receiverMap = new HashMap<Integer, JE802TCPReceiver>();
		this.router.checkQueueSize(bufferSize);
		this.nonTcpSeqNo = 0;
	}

	@Override
	public void event_handler(JEEvent anEvent) {
		String anEventName = anEvent.getName();
		this.now = anEvent.getScheduledTime();
		this.parameterlist = anEvent.getParameterList();
		if (anEventName.equals("TCPDeliv_req")) {
			deliveryRequest(anEvent);
		} else if (anEventName.equals("hop_evaluation")) {
			hopEvaluation(anEvent);
		} else if (anEventName.equals("TCPPacket_delivered_ind")) {
			// nothing to do, this notification is just for first hop
		} else if (anEventName.equals("TCPPacket_discarded_ind")) {
			// nothing to do, this notification is just for first hop
		} else if (anEventName.equals("empty_queue_ind")) {
			emptyQueueIndicator(anEvent);
		} else if (anEventName.equals("packet_exiting_system_ind")) {
			JE802TCPPacket packet = (JE802TCPPacket) this.parameterlist.get(0);
			// packet is Acknowledgment forward to sender
			if (packet.isTCP()) {
				if (packet.isAck()) {
					JE802TCPSender tcpSender = senderMap.get(packet.getPort());
					this.send(new JEEvent("TCP_ACK", tcpSender.getHandlerId(), now, anEvent.getParameterList()));
					// on the receiver side
				} else {
					JE802TCPReceiver tcpReceiver = receiverMap.get(packet.getPort());
					if (tcpReceiver == null) {
						tcpReceiver = new JE802TCPReceiver(theUniqueEventScheduler, theUniqueRandomGenerator,
								this.router.getHandlerId(), bufferSize, this.bulkAcks, this.ackAC);
						receiverMap.put(packet.getPort(), tcpReceiver);
					}
					this.send(new JEEvent("TCPPacketReceived", tcpReceiver.getHandlerId(), now, this.parameterlist));
				}
				// return packet directly as an event to senders tcp layer
			} else {
				if (packet.isAck()) {
					int handlerId = portMap.get(packet.getPort());
					this.send(new JEEvent("packet_exiting_system_ind", handlerId, now, this.parameterlist));
					// on the receiver side, send Event with acknowledgment
					// packet
				} else {
					// create a new packet to prevent aliasing
					JE802TCPPacket newPacket = new JE802TCPPacket(packet);
					newPacket.setAck(true);
					this.parameterlist.clear();
					this.parameterlist.add(newPacket);
					this.send(new JEEvent("packet_exiting_system_ind", newPacket.getSourceHandlerId(), now, this.parameterlist));
				}
			}
		} else {
			this.error("undefined event '" + anEventName + "' in state " + this.theState.toString());
		}
	}

	public boolean isBufferEmpty(int port) {
		JE802TCPSender sender = senderMap.get(port);
		if (sender != null) {
			return sender.isBufferEmpty();
		} else {
			return false;
		}
	}

	public boolean isBufferFull(int port) {
		JE802TCPSender sender = senderMap.get(port);
		if (sender != null) {
			return sender.isBufferFull();
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private void deliveryRequest(JEEvent anEvent) {
		boolean isTcpStream = (Boolean) parameterlist.get(7);
		int ac = (Integer) parameterlist.get(4);
		int trafficHandlerId = (Integer) parameterlist.get(3);
		int port = (Integer) parameterlist.get(6);
		portMap.put(port, trafficHandlerId);
		if (!acTrafficGens.get(ac - 1).contains(trafficHandlerId)) {
			acTrafficGens.get(ac - 1).add(trafficHandlerId);
		}
		if (isTcpStream) {
			JE802TCPSender tcpSender = senderMap.get(port);
			if (tcpSender == null) {
				tcpSender = new JE802TCPSender(theUniqueEventScheduler, theUniqueRandomGenerator, router, bufferSize,
						trafficHandlerId, port, slowStartThreshold, minimumTimeout, this.bulkAcks, statEval, router.getAddress());
				senderMap.put(port, tcpSender);
			}
			this.send(new JEEvent("TCP_Deliv_req", tcpSender.getHandlerId(), now, anEvent.getParameterList()));
		} else {
			// normal UDP traffic, hand over packet to IpLayer
			int size = (Integer) parameterlist.get(0);
			ArrayList<Integer> hopAddresses = (ArrayList<Integer>) this.parameterlist.get(5);
			JE802TCPPacket packet = new JE802TCPPacket(nonTcpSeqNo++, size, port, now, false, false, trafficHandlerId, null);
			this.parameterlist.clear();
			this.parameterlist.add(hopAddresses.get(0)); // destination; TCP
															// only knows
															// Integer DAs, no
															// "(address,channel)"
															// like on lower
															// layers
			this.parameterlist.add(ac);
			this.parameterlist.add(hopAddresses);
			this.parameterlist.add(packet);
			this.send(new JEEvent("TCP_Deliv_req", this.router.getHandlerId(), now, this.parameterlist));
		}
	}

	private void hopEvaluation(JEEvent anEvent) {
		// forward Event to traffic gen
		JE802IPPacket ipPacket = (JE802IPPacket) anEvent.getParameterList().get(0);
		if (!ipPacket.getPayload().isAck()) {
			int port = ipPacket.getPayload().getPort();
			Integer handlerId = this.portMap.get(port);
			if (handlerId != null) {
				this.send(new JEEvent("hop_evaluation", handlerId, now, anEvent.getParameterList()));
			} else {
				message("Port" + port);
			}
		}
	}

	private void emptyQueueIndicator(JEEvent anEvent) {
		int anAC = (Integer) anEvent.getParameterList().get(0);
		ArrayList<Integer> trafficGens = acTrafficGens.get(anAC - 1);
		// forward event to all traffic gens of this access category
		for (Integer tgenHandlerId : trafficGens) {
			this.send(new JEEvent("empty_queue_ind", tgenHandlerId, now, anEvent.getParameterList()));
		}

	}

	public long getTransmittedPackets() {
		long sum = 0;
		for (JE802TCPSender sender : senderMap.values()) {
			sum += sender.getTransmittedPackets();
		}
		return sum;
	}

	public long getLostPackets() {
		long sum = 0;
		for (JE802TCPSender sender : senderMap.values()) {
			sum += sender.getLostPackets();
		}
		return sum;
	}

	public void retransmissionRate() {
		for (int port : portMap.keySet()) {
			JE802TCPSender sender = senderMap.get(port);
			if (sender != null) {
				long transmitted = sender.getTransmittedPackets();
				long lost = sender.getLostPackets();
				double retransmissionRate = ((double) lost / transmitted);
				this.message("Station " + router.getAddress() + " TG at Port: " + sender.getPort() + " Lost " + lost
						+ " Transmitted " + transmitted + " Lossrate " + retransmissionRate);
			}
		}
		double overallRetransRate = ((double) this.getLostPackets() / this.getTransmittedPackets());
		if (!Double.isNaN(overallRetransRate)) {
			this.message("Station " + router.getAddress() + " OverallLossrate: " + overallRetransRate);
		}
	}

	@Override
	public String toString() {
		return "TCPLayerAddress at Station " + this.router.getAddress();
	}
}
