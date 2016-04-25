package zzInfra.layer3_network;

import java.util.ArrayList;
import java.util.List;

import zzInfra.layer3_network.JE802HopInfo;;

public class JE802RREQPacket extends JE802IPPacket {

	private int hopCount;

	private final long destinationSeqNo;

	private final long originatorSeqNo;

	private final long rreqId;

	private final List<JE802MCRHopRecord> metricList;

	// not part of the rfc 3561
	private double mcrCost;

	private double lastHopSwitchingCost;

	private boolean unknownSequenceNumber;

	public JE802RREQPacket(JE802HopInfo sourceAddress, JE802HopInfo destinationAddress, int sourceHandler, long rreqId,
			long originSeqNo, long destSeqNo) {
		super(sourceAddress, destinationAddress, null, sourceHandler, 1);
		this.rreqId = rreqId;
		this.originatorSeqNo = originSeqNo;
		this.destinationSeqNo = destSeqNo;
		this.metricList = new ArrayList<JE802MCRHopRecord>();
		this.lastHopSwitchingCost = 0;
	}

	public JE802RREQPacket(JE802RREQPacket toCopy) {
		super(toCopy);
		this.rreqId = toCopy.getRreqId();
		this.destinationSeqNo = toCopy.getDestSeqNo();
		this.originatorSeqNo = toCopy.getOriginSeqNo();
		this.unknownSequenceNumber = toCopy.isUnknownSequenceNumber();
		this.hopCount = toCopy.getHopCount();
		this.mcrCost = toCopy.getMcrCost();
		this.lastHopFixedChannel = toCopy.getLastHopFixedChannel();
		this.metricList = new ArrayList<JE802MCRHopRecord>(toCopy.getMetricList());
	}

	public void addMetricRecord(JE802MCRHopRecord record) {
		metricList.add(record);
	}

	public long getDestSeqNo() {
		return destinationSeqNo;
	}

	public int getHopCount() {
		return hopCount;
	}

	@Override
	public int getLength() {
		// see (5.1)
		return super.getLength() + 24;
	}

	public double getMcrCost() {
		return mcrCost;
	}

	public List<JE802MCRHopRecord> getMetricList() {
		return metricList;
	}

	public long getOriginSeqNo() {
		return originatorSeqNo;
	}

	@Override
	public boolean isControlPacket() {
		return true;
	}

	public long getRreqId() {
		return rreqId;
	}

	public void incrementHopCount() {
		hopCount++;
	}

	public boolean isUnknownSequenceNumber() {
		return unknownSequenceNumber;
	}

	public void setMcrCost(double mcrCost) {
		this.mcrCost = mcrCost;
	}

	public void setUnknownSequenceNumber(boolean unknownSequenceNumber) {
		this.unknownSequenceNumber = unknownSequenceNumber;
	}

	@Override
	public String toString() {
		return "RREQID:" + rreqId + " Origin: " + this.sourceAddress + " Dest: " + this.destinationAddress + " Hops: " + hopCount;
	}

	public double getLastHopSwitchingCost() {
		return lastHopSwitchingCost;
	}

	public void setLastHopSwitchingCost(double lastHopSwitchingCost) {
		this.lastHopSwitchingCost = lastHopSwitchingCost;
	}
}
