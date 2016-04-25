package zzInfra.layer3_network;

import java.util.ArrayList;
import java.util.List;

import zzInfra.layer3_network.JE802HopInfo;;

//The RERR message is sent whenever a link break causes one or more destinations to become unreachable from some of the node's neighbors.

public class JE802RERRPacket extends JE802IPPacket {

	// The number of unreachable destinations included in the message; MUST be
	// at least 1.
	private int destCount;

	// Unreachable Destination IP Address The IP address of the destination that
	// has become unreachable due to a link break.
	private List<JE802HopInfo> unreachDestAddr;

	// The sequence number in the route table entry for the destination listed
	// in the previous Unreachable Destination IP Address field.
	private List<Long> unreachDestSeqNo;

	public JE802RERRPacket(JE802HopInfo sourceAddr, JE802HopInfo destAddr, int sourceHandler) {
		super(sourceAddr, destAddr, null, sourceHandler, 1);
		this.unreachDestAddr = new ArrayList<JE802HopInfo>();
		this.unreachDestSeqNo = new ArrayList<Long>();
	}

	public JE802RERRPacket(JE802RERRPacket toCopy) {
		super(toCopy);
		this.destCount = toCopy.getDestCount();
		this.unreachDestSeqNo = new ArrayList<Long>(toCopy.getUnreachDestSeqNo());
		this.unreachDestAddr = new ArrayList<JE802HopInfo>(toCopy.getUnreachDestAddr());
	}

	public void addUnreachableDestination(JE802HopInfo addr, long destSeqNo) {
		this.unreachDestAddr.add(addr);
		this.unreachDestSeqNo.add(destSeqNo);
		this.destCount++;
	}

	public int getDestCount() {
		return destCount;
	}

	@Override
	public int getLength() {
		// see (5.3)
		return super.getLength() + 4 + destCount * 8;
	}

	public List<JE802HopInfo> getUnreachDestAddr() {
		return unreachDestAddr;
	}

	@Override
	public boolean isControlPacket() {
		return true;
	}

	public List<Long> getUnreachDestSeqNo() {
		return unreachDestSeqNo;
	}
}
