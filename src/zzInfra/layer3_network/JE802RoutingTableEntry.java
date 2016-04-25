package zzInfra.layer3_network;

import java.util.ArrayList;
import java.util.List;

import zzInfra.kernel.JETime;
import zzInfra.layer3_network.JE802HopInfo;;

public class JE802RoutingTableEntry {

	// time at which route expires
	private JETime expiryTime;

	// address of next hop
	private JE802HopInfo nextHop;

	private int hopCount;

	// this is part of the MCR addon to the rfc 3561 AODV specification
	private double mcrCost = Double.MAX_VALUE;

	// destination of route
	private JE802HopInfo destinationAddr;

	private long destinationSeqNo;

	private boolean valid = false;

	private boolean validDestSeqNo = false;

	private boolean repairing = false;

	private boolean repairable = true;

	private int linkLostCount = 0;

	// precursor list
	private List<JE802HopInfo> precursors;

	public JE802RoutingTableEntry(JE802HopInfo destinationAddr, long destSeqNo, JE802HopInfo nextHop, JETime expiryTime,
			int hopCount) {
		this.destinationAddr = destinationAddr;
		this.nextHop = nextHop;
		this.expiryTime = expiryTime;
		this.hopCount = hopCount;
		this.precursors = new ArrayList<JE802HopInfo>();
		this.destinationSeqNo = destSeqNo;
	}

	public void addPrecursor(JE802HopInfo precursorAddr) {
		if (!precursors.contains(precursorAddr)) {
			precursors.add(precursorAddr);
		}
	}

	public JE802HopInfo getDA() {
		return destinationAddr;
	}

	public long getDestinationSeqNo() {
		return destinationSeqNo;
	}

	public JETime getExpiryTime() {
		return expiryTime;
	}

	public int getHopCount() {
		return hopCount;
	}

	public double getMcrCost() {
		return mcrCost;
	}

	public JE802HopInfo getNextHop() {
		return nextHop;
	}

	public List<JE802HopInfo> getPrecursorList() {
		return precursors;
	}

	public boolean increaseLinkLostCount() {
		this.linkLostCount++;
		if (linkLostCount >= JE802RoutingConstants.LINK_BREAK_AFTER_LOSS) {
			return true;
		}
		return false;
	}

	public boolean isRepairable() {
		return repairable;
	}

	public boolean isRepairing() {
		return repairing;
	}

	public boolean isValid() {
		return valid;
	}

	public boolean isValidDestSeqNo() {
		return validDestSeqNo;
	}

	public void resetLinkLostCount() {
		this.linkLostCount = 0;
	}

	public void setDestinationChannel(int channel) {
		this.destinationAddr = new JE802HopInfo(this.destinationAddr.getAddress(), channel);
	}

	public void setDestinationSeqNo(long destinationSeqNo) {
		this.destinationSeqNo = destinationSeqNo;
	}

	public void setHopCount(int hopCount) {
		this.hopCount = hopCount;
	}

	public void setMcrCost(double mcrCost) {
		this.mcrCost = mcrCost;
	}

	public void setNextHop(JE802HopInfo nextHop) {
		this.nextHop = nextHop;
	}

	public void setRepairable(boolean repairable) {
		this.repairable = repairable;
	}

	public void setRepairing(boolean repairing) {
		this.repairing = repairing;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public void setValidDestSeqNo(boolean validDestSeqNo) {
		this.validDestSeqNo = validDestSeqNo;
	}

	@Override
	public String toString() {
		return "Dest:" + destinationAddr + " Hops:" + hopCount + " NextHop:" + nextHop + " Valid: " + valid + " ValidDestSeq: "
				+ validDestSeqNo + " DestSeqNo:" + destinationSeqNo + " Expires:" + expiryTime + " Mcr:" + mcrCost;
	}

	public void updateExpiryTime(JETime newExpiryTime) {
		if (newExpiryTime.isLaterThan(expiryTime)) {
			this.expiryTime = newExpiryTime;
		}
	}

}
