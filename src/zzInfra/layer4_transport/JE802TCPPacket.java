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

import zzInfra.kernel.JETime;
import IEEE11ac.layer2.acHopInfo;

public class JE802TCPPacket {

	private final long seqNo;

	private final int length;

	private final int port;

	private JETime creationTime;

	private boolean ack;

	private final boolean isTCP;

	private final int sourceHandlerId;

	private ArrayList<acHopInfo> backRoute;

	public JE802TCPPacket(long seqNo, int length, int port, JETime creationTime, boolean isTCP, boolean isAck,
			int sourceHandlerId, ArrayList<acHopInfo> aBackRoute) {
		this.seqNo = seqNo;
		this.length = length;
		this.port = port;
		this.creationTime = creationTime;
		this.ack = isAck;
		this.backRoute = aBackRoute;
		this.isTCP = isTCP;
		this.sourceHandlerId = sourceHandlerId;
	}

	public JE802TCPPacket(JE802TCPPacket packet) {
		this.seqNo = packet.seqNo;
		this.length = packet.length;
		this.port = packet.port;
		if (packet.getCreationTime() != null) {
			this.creationTime = new JETime(packet.getCreationTime());
		} else {
			creationTime = null;
		}
		this.ack = packet.ack;
		if (packet.getBackRoute() != null) {
			this.backRoute = new ArrayList<acHopInfo>(packet.backRoute);
		} else {
			this.backRoute = null;
		}

		this.isTCP = packet.isTCP;
		this.sourceHandlerId = packet.getSourceHandlerId();
	}

	public int getSourceHandlerId() {
		return sourceHandlerId;
	}

	public long getSeqNo() {
		return seqNo;
	}

	public int getLength() {
		return length;
	}

	public int getPort() {
		return port;
	}

	public JETime getCreationTime() {
		return creationTime;
	}

	public ArrayList<acHopInfo> getBackRoute() {
		return backRoute;
	}

	public boolean isAck() {
		return ack;
	}

	public boolean isTCP() {
		return isTCP;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public void setCreationTime(JETime creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public String toString() {
		return "TCPPacket: seq: " + this.seqNo + " length: " + this.length + " port: " + this.port + " isack: " + this.ack;
	}

}
