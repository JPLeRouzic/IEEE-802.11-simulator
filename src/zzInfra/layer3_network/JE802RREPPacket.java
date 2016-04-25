package zzInfra.layer3_network;

import zzInfra.kernel.JETime;
import zzInfra.layer3_network.JE802HopInfo;;

public class JE802RREPPacket extends JE802IPPacket {

	// The destination sequence number associated to the route.
	private final long destinationSeqNo;

	// The time in milliseconds for which nodes receiving the RREP consider the
	// route to be valid.
	private JETime lifetime;

	// The number of hops from the Originator IP Address to the Destination IP
	// Address.
	private int hopCount;

	private double pathMcr;

	private final JE802HopInfo rreqDestAddr;

	private final JE802HopInfo rreqOriginAddr;

	private boolean intermediateRrep;

	public JE802RREPPacket(final JE802HopInfo sourceAddress, final JE802HopInfo destAddr, final JE802HopInfo rreqOriginAddr,
			final JE802HopInfo rreqDestAddr, final long destSeqNo, final int sourceHandler, final int hopCount, final double mcr) {
		super(sourceAddress, destAddr, null, sourceHandler, 1);
		this.destinationSeqNo = destSeqNo;
		this.rreqOriginAddr = rreqOriginAddr;
		this.rreqDestAddr = rreqDestAddr;
		this.hopCount = hopCount;
		this.pathMcr = mcr;
	}

	public JE802RREPPacket(final JE802RREPPacket toCopy) {
		super(toCopy);
		this.destinationSeqNo = toCopy.getDestSeqNo();
		this.rreqOriginAddr = toCopy.getRreqOriginAddr();
		this.rreqDestAddr = toCopy.getRreqDestAddr();
		this.hopCount = toCopy.getHopCount();
		this.pathMcr = toCopy.getPathMcr();
		this.lastHopFixedChannel = toCopy.getLastHopFixedChannel();
		this.intermediateRrep = toCopy.isIntermediateRrep();
	}

	public long getDestSeqNo() {
		return this.destinationSeqNo;
	}

	public int getHopCount() {
		return this.hopCount;
	}

	@Override
	public int getLength() {
		// see (5.2)
		return super.getLength() + 20;
	}

	public JETime getLifetime() {
		return this.lifetime;
	}

	public double getPathMcr() {
		return this.pathMcr;
	}

	public JE802HopInfo getRreqDestAddr() {
		return this.rreqDestAddr;
	}

	public JE802HopInfo getRreqOriginAddr() {
		return this.rreqOriginAddr;
	}

	public void incrementHopCount() {
		this.hopCount++;
	}

	public boolean isIntermediateRrep() {
		return this.intermediateRrep;
	}

	public void setIntermediateRrep(final boolean intermediateRrep) {
		this.intermediateRrep = intermediateRrep;
	}

	public void setPathMcr(final double pathMcr) {
		this.pathMcr = pathMcr;
	}

	public void setTTL(final int newTTL) {
		this.ttl = newTTL;
	}

	@Override
	public boolean isControlPacket() {
		return true;
	}
}
